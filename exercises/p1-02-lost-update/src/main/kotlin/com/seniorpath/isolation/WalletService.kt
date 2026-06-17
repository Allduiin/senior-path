package com.seniorpath.isolation

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * p1-02 — Lost update under READ COMMITTED; optimistic vs pessimistic locking (targets Q2).
 *
 * SCENARIO
 * --------
 * Many concurrent requests withdraw from the SAME wallet. [withdraw] performs a textbook
 * read-modify-write:
 *
 *     1. read   the current balance into the application
 *     2. modify it in memory (balance - amount)
 *     3. write  the new balance back
 *
 * THE BUG (a correctness bug, not a crash — do NOT "fix" it by deleting the window)
 * ---------------------------------------------------------------------------------
 * PostgreSQL's DEFAULT isolation level is READ COMMITTED. Each transaction takes a fresh
 * snapshot per statement, so two concurrent withdrawals can BOTH read the same balance B,
 * both compute B - amount, and both write it back. The second commit overwrites the first:
 * one withdrawal is silently LOST. This is the classic *lost update* anomaly — the
 * read-modify-write is not atomic with respect to other transactions.
 *
 * The integration test fires N concurrent withdrawals and asserts the final balance equals
 * INITIAL - N * amount. With this naive path the balance is far higher (updates were lost),
 * so the test FAILS BY DESIGN (red start).
 *
 * YOUR TASKS (see SPEC.md)
 * ------------------------
 *  1. In SPEC.md, explain the mechanism: why READ COMMITTED permits lost updates, how
 *     PostgreSQL MVCC snapshots interact with the read-modify-write window, and contrast
 *     with REPEATABLE READ / SERIALIZABLE (and InnoDB's REPEATABLE READ).
 *  2. Make the test pass WITHOUT removing [COMPUTE_WINDOW_MILLIS] and WITHOUT serialising
 *     the test itself. Pick ONE approach and note the tradeoffs of all three in SPEC.md:
 *       (a) OPTIMISTIC — add @Version to [Wallet], catch the optimistic-lock failure and
 *           retry the whole read-modify-write (bounded attempts, ideally jittered backoff);
 *       (b) PESSIMISTIC — read the row `... FOR UPDATE` (LockModeType.PESSIMISTIC_WRITE) so
 *           concurrent writers serialise on the row;
 *       (c) ATOMIC — replace the read-modify-write with a single `UPDATE ... SET balance =
 *           balance - :amount` and let the database do it under its row lock.
 *  3. Correctness must hold because the lost update is genuinely PREVENTED — not because you
 *     shrank the window, throttled the test, or weakened the assertion.
 */
@Service
class WalletService(
    private val wallets: WalletRepository,
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
    @Transactional
    fun withdraw(walletId: Long, amount: Long) {
        // 1. READ
        val wallet = wallets.findById(walletId).orElseThrow()

        // The read-modify-write WINDOW. Real handlers spend time here (revalidate limits,
        // fraud checks, fee math). DO NOT REMOVE THIS — a correct solution must tolerate a
        // non-zero window. Removing it only hides the race; it does not fix it.
        hold(COMPUTE_WINDOW_MILLIS)

        // 2. MODIFY
        wallet.balance -= amount

        // 3. WRITE (flush on tx commit)
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
    }
}
