package com.seniorpath.tx

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * Integration test for p1-01. Proves that REQUIRES_NEW is NOT applied while the inner
 * @Transactional method is reached via self-invocation.
 *
 * Expected once the call is correctly routed through the proxy:
 *   - payments.count() == 1  (outer tx commits; best-effort audit failure is swallowed)
 *   - audits.count()   == 0  (inner REQUIRES_NEW tx rolls back independently on throw)
 *
 * RED START: with the shipped self-invocation bug, the audit insert runs in the outer
 * committed transaction, so audits.count() == 1 and the final assertion fails by design.
 */
@Testcontainers
@SpringBootTest
class SelfInvocationRollbackTest @Autowired constructor(
    private val service: PaymentLedgerService,
    private val payments: PaymentRepository,
    private val audits: AuditRepository,
) {

    @BeforeEach
    fun reset() {
        audits.deleteAll()
        payments.deleteAll()
    }

    @Test
    fun `audit rolls back independently while the payment still commits`() {
        service.processPayment("PAY-0001")

        assertThat(payments.count())
            .describedAs("payment must commit; a best-effort audit failure must not roll the outer tx back")
            .isEqualTo(1)

        assertThat(audits.count())
            .describedAs("audit row must roll back independently — REQUIRES_NEW genuinely applied via the proxy")
            .isZero()
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
    }
}
