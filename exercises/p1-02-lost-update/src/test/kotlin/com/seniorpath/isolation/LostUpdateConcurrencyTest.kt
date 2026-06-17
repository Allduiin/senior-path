package com.seniorpath.isolation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.Collections
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Integration test for p1-02. Fires [CONCURRENCY] withdrawals at the SAME wallet, released
 * together by a barrier so they overlap inside the read-modify-write window.
 *
 * Expected once concurrent withdrawals no longer lose updates:
 *   - final balance == INITIAL_BALANCE - CONCURRENCY * WITHDRAWAL   (every withdrawal applied once)
 *   - no withdrawal failed unhandled (an optimistic fix must RETRY, not surface the conflict)
 *
 * RED START: the naive read-modify-write under PostgreSQL READ COMMITTED loses updates, so
 * the final balance is far higher than expected and the assertion fails by design.
 */
@Testcontainers
@SpringBootTest
class LostUpdateConcurrencyTest @Autowired constructor(
    private val wallets: WalletService,
) {

    @Test
    fun `concurrent withdrawals must all apply exactly once`() {
        val walletId = wallets.openWallet(INITIAL_BALANCE)

        val barrier = CyclicBarrier(CONCURRENCY)
        val pool = Executors.newFixedThreadPool(CONCURRENCY)
        val failures = Collections.synchronizedList(mutableListOf<Throwable>())

        try {
            val tasks = (1..CONCURRENCY).map {
                java.util.concurrent.Callable {
                    try {
                        barrier.await(10, TimeUnit.SECONDS)
                        wallets.withdraw(walletId, WITHDRAWAL)
                    } catch (t: Throwable) {
                        // A correct solution either serialises (no error) or retries the
                        // optimistic conflict internally — so this list must stay EMPTY.
                        failures.add(t)
                    }
                }
            }
            pool.invokeAll(tasks)
        } finally {
            pool.shutdownNow()
        }

        val expected = INITIAL_BALANCE - CONCURRENCY * WITHDRAWAL
        assertThat(wallets.balanceOf(walletId))
            .describedAs(
                "all %d concurrent withdrawals must apply exactly once (no lost updates); unhandled failures=%s",
                CONCURRENCY, failures,
            )
            .isEqualTo(expected)
    }

    companion object {
        private const val INITIAL_BALANCE = 1_000_00L   // €1000.00 in minor units
        private const val WITHDRAWAL = 1_00L            // €1.00
        private const val CONCURRENCY = 16

        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
    }
}
