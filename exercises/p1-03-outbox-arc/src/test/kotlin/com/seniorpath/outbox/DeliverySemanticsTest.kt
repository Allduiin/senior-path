package com.seniorpath.outbox

import com.seniorpath.outbox.entity.NotificationLog
import com.seniorpath.outbox.repository.NotificationLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.UUID

@Testcontainers
@SpringBootTest
class DeliverySemanticsTest @Autowired constructor(
    private val paymentService: PaymentService,
    private val notificationLog: NotificationLogRepository,
    private val crashPoint: CrashPoint,
) {

    @Test
    fun `a captured payment notifies the merchant exactly once`() {
        val orderId = uniqueOrderId()

        paymentService.capture(orderId, AMOUNT_MINOR)

        val entries = settledNotifications(orderId, DELIVERY_PATIENCE)
        assertThat(entries)
            .describedAs(
                "exactly one notification must be recorded for %s — none means the consumer is " +
                    "not wired, two means it is not idempotent",
                orderId,
            )
            .hasSize(1)
        assertThat(entries.single().amountMinor)
            .describedAs("the notification must carry the captured amount")
            .isEqualTo(AMOUNT_MINOR)
    }

    @Test
    fun `a consumer crash mid-handle must not lose the notification`() {
        val orderId = uniqueOrderId()
        crashPoint.arm(CrashPoint.AFTER_CLAIM_BEFORE_NOTIFY, orderId)

        paymentService.capture(orderId, AMOUNT_MINOR)

        assertThat(settledNotifications(orderId, REDELIVERY_PATIENCE))
            .describedAs(
                "LOST NOTIFICATION: the consumer crashed mid-handle for %s and the message never " +
                    "came back. That is at-most-once — the ack sits BEFORE the effect (the broker " +
                    "considers the message consumed the moment it is dispatched), so a crash " +
                    "consumes it forever. The ack must not precede the effect's commit",
                orderId,
            )
            .hasSize(1)
    }

    @Test
    fun `a republished event must not notify the merchant twice`() {
        val orderId = uniqueOrderId()
        crashPoint.arm(CrashPoint.AFTER_PUBLISH_BEFORE_MARK, orderId)

        paymentService.capture(orderId, AMOUNT_MINOR)

        assertThat(settledNotifications(orderId, DELIVERY_PATIENCE))
            .describedAs(
                "DOUBLE NOTIFICATION: the relay republished the event for %s and this consumer " +
                    "recorded it twice. At-least-once delivery is the correct transport choice — " +
                    "but it demands an idempotent effect on THIS consumer too, not only on the " +
                    "ledger one",
                orderId,
            )
            .hasSize(1)
    }

    private fun settledNotifications(orderId: String, atMost: Duration): List<NotificationLog> {
        awaitQuietly(atMost) { notificationLog.findByOrderId(orderId).isNotEmpty() }
        awaitQuietly(SETTLE_WINDOW) { notificationLog.findByOrderId(orderId).size > 1 }
        return notificationLog.findByOrderId(orderId)
    }

    private fun awaitQuietly(atMost: Duration, condition: () -> Boolean) {
        try {
            Awaitility.await()
                .atMost(atMost)
                .pollInterval(POLL_INTERVAL)
                .until(condition)
        } catch (timeout: ConditionTimeoutException) {
            return
        }
    }

    private fun uniqueOrderId(): String = "order-${UUID.randomUUID()}"

    companion object {
        private const val AMOUNT_MINOR = 500_00L
        private val POLL_INTERVAL: Duration = Duration.ofMillis(250)
        private val DELIVERY_PATIENCE: Duration = Duration.ofSeconds(10)
        private val REDELIVERY_PATIENCE: Duration = Duration.ofSeconds(15)
        private val SETTLE_WINDOW: Duration = Duration.ofSeconds(6)

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
