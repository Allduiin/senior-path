package com.seniorpath.outbox

import com.seniorpath.outbox.repository.PaymentRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.dao.DataAccessException
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.UUID

@Testcontainers
@SpringBootTest
class DualWriteLostEventTest @Autowired constructor(
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,
    private val rabbit: RabbitTemplate,
    private val crashPoint: CrashPoint,
) {

    @Test
    fun `capture persists the payment and publishes payment-captured`() {
        val orderId = uniqueOrderId()

        val paymentId = paymentService.capture(orderId, AMOUNT_MINOR)

        assertThat(paymentRepository.findById(paymentId)).isPresent
        assertThat(awaitMessageFor(orderId, RELAY_PATIENCE))
            .describedAs("a payment.captured event for %s must reach the broker", orderId)
            .isNotNull()
            .contains(orderId)
    }

    @Test
    fun `crash between DB commit and publish must not lose the event`() {
        val orderId = uniqueOrderId()
        crashPoint.arm(CrashPoint.AFTER_COMMIT_BEFORE_PUBLISH)

        assertThatThrownBy { paymentService.capture(orderId, AMOUNT_MINOR) }
            .describedAs("the armed crash point models the process dying after commit, before publish")
            .isInstanceOf(SimulatedCrashException::class.java)

        assertThat(paymentRepository.existsByOrderId(orderId))
            .describedAs("the DB commit happened before the crash — the payment must exist")
            .isTrue()

        assertThat(awaitMessageFor(orderId, RELAY_PATIENCE))
            .describedAs(
                "LOST EVENT: payment %s is committed but its event never reached the broker. " +
                    "The event must survive a crash between commit and publish (transactional outbox + relay)",
                orderId,
            )
            .isNotNull()
            .contains(orderId)
    }

    @Test
    fun `failed duplicate capture must never publish a ghost event`() {
        val orderId = uniqueOrderId()
        paymentService.capture(orderId, AMOUNT_MINOR)

        assertThatThrownBy { paymentService.capture(orderId, AMOUNT_MINOR) }
            .describedAs("second capture of the same orderId must fail on the unique constraint")
            .isInstanceOf(DataAccessException::class.java)

        assertThat(collectMessagesFor(orderId, GHOST_WATCH_WINDOW))
            .describedAs(
                "GHOST EVENT: the rolled-back duplicate must publish nothing — exactly one event for %s",
                orderId,
            )
            .hasSize(1)
    }

    private fun awaitMessageFor(orderId: String, atMost: Duration): String? {
        val deadline = System.nanoTime() + atMost.toNanos()
        while (System.nanoTime() < deadline) {
            val message = rabbit.receive(PaymentEvents.CAPTURED_QUEUE, POLL_MILLIS) ?: continue
            val body = String(message.body)
            if (body.contains(orderId)) {
                return body
            }
        }
        return null
    }

    private fun collectMessagesFor(orderId: String, window: Duration): List<String> {
        val deadline = System.nanoTime() + window.toNanos()
        val bodies = mutableListOf<String>()
        while (System.nanoTime() < deadline) {
            val message = rabbit.receive(PaymentEvents.CAPTURED_QUEUE, POLL_MILLIS) ?: continue
            val body = String(message.body)
            if (body.contains(orderId)) {
                bodies += body
            }
        }
        return bodies
    }

    private fun uniqueOrderId(): String = "order-${UUID.randomUUID()}"

    companion object {
        private const val AMOUNT_MINOR = 500_00L
        private const val POLL_MILLIS = 250L
        private val RELAY_PATIENCE: Duration = Duration.ofSeconds(10)
        private val GHOST_WATCH_WINDOW: Duration = Duration.ofSeconds(6)

        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))

        @Container
        @ServiceConnection
        @JvmStatic
        val rabbitmq = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"))
    }
}
