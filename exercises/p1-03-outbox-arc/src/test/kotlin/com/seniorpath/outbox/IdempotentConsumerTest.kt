package com.seniorpath.outbox

import com.seniorpath.outbox.entity.PayoutEntry
import com.seniorpath.outbox.repository.PayoutLedgerRepository
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
class IdempotentConsumerTest @Autowired constructor(
    private val paymentService: PaymentService,
    private val payoutLedger: PayoutLedgerRepository,
    private val crashPoint: CrashPoint,
) {

    @Test
    fun `captured payment is credited to the payout ledger exactly once`() {
        val orderId = uniqueOrderId()

        paymentService.capture(orderId, AMOUNT_MINOR)

        val entries = awaitFirstCredit(orderId, DELIVERY_PATIENCE)
        assertThat(entries)
            .describedAs(
                "no consumer credited the payout ledger for %s — the at-least-once pipeline built in " +
                    "stage A needs a consumer on %s",
                orderId,
                PaymentEvents.LEDGER_QUEUE,
            )
            .hasSize(1)
        assertThat(entries.single().amountMinor)
            .describedAs("the credit must carry the captured amount")
            .isEqualTo(AMOUNT_MINOR)
    }

    @Test
    fun `a republished event must not double-credit the ledger`() {
        val orderId = uniqueOrderId()
        crashPoint.arm(CrashPoint.AFTER_PUBLISH_BEFORE_MARK, orderId)

        paymentService.capture(orderId, AMOUNT_MINOR)

        assertThat(settledCredits(orderId, DELIVERY_PATIENCE))
            .describedAs(
                "DOUBLE CREDIT: the relay crashed after publishing %s and before marking it sent, so " +
                    "the next pass republished the same event. Two deliveries of one event must " +
                    "produce exactly one ledger credit",
                orderId,
            )
            .hasSize(1)
    }

    @Test
    fun `a crash between claiming the key and crediting must not lose the credit`() {
        val orderId = uniqueOrderId()
        crashPoint.arm(CrashPoint.AFTER_CLAIM_BEFORE_EFFECT, orderId)

        paymentService.capture(orderId, AMOUNT_MINOR)

        assertThat(settledCredits(orderId, REDELIVERY_PATIENCE))
            .describedAs(
                "LOST CREDIT: the first attempt at %s crashed after claiming the idempotency key and " +
                    "before crediting the ledger. If the claim was committed in a transaction of its " +
                    "own it survives that crash, the redelivery is skipped as 'already processed', " +
                    "and the credit never happens. Claim and side effect must share ONE local " +
                    "transaction — exactly one entry must exist",
                orderId,
            )
            .hasSize(1)
    }

    private fun awaitFirstCredit(orderId: String, atMost: Duration): List<PayoutEntry> {
        awaitQuietly(atMost) { payoutLedger.findByOrderId(orderId).isNotEmpty() }
        return payoutLedger.findByOrderId(orderId)
    }

    private fun settledCredits(orderId: String, atMost: Duration): List<PayoutEntry> {
        awaitQuietly(atMost) { payoutLedger.findByOrderId(orderId).isNotEmpty() }
        awaitQuietly(SETTLE_WINDOW) { payoutLedger.findByOrderId(orderId).size > 1 }
        return payoutLedger.findByOrderId(orderId)
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
