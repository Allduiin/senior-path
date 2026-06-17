package com.seniorpath.isolation

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.context.annotation.Lazy
import org.springframework.dao.OptimisticLockingFailureException
import java.util.concurrent.ThreadLocalRandom

/**
 * p1-02 — Lost update under READ COMMITTED; optimistic vs pessimistic vs atomic (targets Q2).
 *
 * SCENARIO
 * --------
 * Many concurrent requests withdraw from the SAME wallet. [withdraw] performs the textbook
 * read-modify-write:
 *
 *     1. READ   the current balance into the application
 *     2. MODIFY it in memory (balance - amount)
 *     3. WRITE  the new balance back
 *
 * THE BUG (a correctness bug, not a crash — do NOT "fix" it by deleting the window)
 * ---------------------------------------------------------------------------------
 * PostgreSQL's DEFAULT isolation is READ COMMITTED: each statement sees a fresh snapshot of
 * committed data, with NO promise the row is unchanged between your read and your write. Two
 * concurrent withdrawals can BOTH read balance B, BOTH compute B - amount, and BOTH write it
 * back. The second commit overwrites the first — one withdrawal is silently LOST. This is the
 * classic *lost update* anomaly: the read-modify-write is not atomic across transactions.
 *
 * The integration test fires N concurrent withdrawals and asserts the final balance equals
 * INITIAL - N * amount. With this naive path the balance is far higher (updates were lost),
 * so the test FAILS BY DESIGN (red start).
 *
 * YOUR TASKS (see SPEC.md)
 * ------------------------
 *  1. In SPEC.md (Analysis), explain the mechanism: why READ COMMITTED permits the lost update
 *     for an application-level RMW, why an atomic `balance = balance - :amount` would NOT lose
 *     it even at RC (current read under a row lock vs the per-statement snapshot), and contrast
 *     REPEATABLE READ / SERIALIZABLE and PostgreSQL SI vs InnoDB's REPEATABLE READ.
 *  2. Make the test pass WITHOUT removing [COMPUTE_WINDOW_MILLIS] and WITHOUT serialising the
 *     test itself. Pick ONE fix and document the tradeoffs of all three in SPEC.md:
 *       (a) OPTIMISTIC  — add @Version to [Wallet], catch the optimistic-lock failure and
 *           RETRY the whole read-modify-write (bounded attempts, ideally jittered backoff);
 *       (b) PESSIMISTIC — read the row `... FOR UPDATE` (LockModeType.PESSIMISTIC_WRITE) so
 *           concurrent writers serialise on the row;
 *       (c) ATOMIC      — replace the read-modify-write with a single `UPDATE ... SET balance =
 *           balance - :amount` and let the database apply it under its own row lock.
 *  3. Correctness must hold because the lost update is genuinely PREVENTED — not because you
 *     shrank the window, throttled the test, or weakened the assertion.
 */
@Service
class WalletService(
    private val wallets: WalletRepository,
    @Lazy private val self: WalletService
) {

    /** Opens a wallet seeded with [initialBalance] minor units and returns its id. */
    @Transactional
    fun openWallet(initialBalance: Long): Long =
        wallets.save(Wallet(balance = initialBalance)).id!!

    @Transactional(readOnly = true)
    fun balanceOf(walletId: Long): Long =
        wallets.findById(walletId).orElseThrow().balance

    /**
     * Withdraw [amount] (minor units) from [walletId].
     *
     * BUG: naive read-modify-write — not atomic across concurrent transactions.
     * TODO(task 2): make this lose ZERO updates under concurrency, via (a), (b) or (c).
     */
    fun withdraw(walletId: Long, amount: Long) {
        var attempt = 0

        while (true) {
            try {
                self.withdrawAttempt(walletId, amount)
                break
            } catch (e: OptimisticLockingFailureException) {
                if (++attempt >= MAX_ATTEMPTS) {
                    throw e
                } else {
                    Thread.sleep(BASE_BACKOFF_MS + ThreadLocalRandom.current().nextLong(JITTER_MS))
                }
            }
        }
    }

    @Transactional
    public fun withdrawAttempt(walletId: Long, amount: Long) {
        // 1. READ
        val wallet = wallets.findById(walletId).orElseThrow()

        // The read-modify-write WINDOW. Real handlers spend time here (revalidate limits,
        // fraud checks, fee math). DO NOT REMOVE THIS — a correct solution must tolerate a
        // non-zero window. Removing it only hides the race; it does not fix it.
        hold(COMPUTE_WINDOW_MILLIS)

        // 2. MODIFY
        wallet.balance -= amount

        // 3. WRITE (flushed on tx commit)
        wallets.save(wallet)
    }

    private fun hold(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        /** Width of the read-modify-write window, in milliseconds. Part of the scenario. */
        const val COMPUTE_WINDOW_MILLIS = 25L
        const val MAX_ATTEMPTS = 20
        const val BASE_BACKOFF_MS = 100L
        const val JITTER_MS = 200L
    }
}
