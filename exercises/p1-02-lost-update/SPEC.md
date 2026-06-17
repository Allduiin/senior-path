# p1-02 — Lost update under READ COMMITTED

| | |
|---|---|
| **Phase** | 1 — Distributed systems & transactional correctness |
| **Targets diagnostic** | **Q2** (isolation levels & MVCC; optimistic vs pessimistic locking) |
| **Start state** | RED — tests fail by design |
| **Done state** | GREEN — every concurrent withdrawal applies exactly once |

## Objective
Build the reflex for the **lost-update anomaly**: see *why* an application-level
read-modify-write over a row is not atomic under PostgreSQL's default `READ COMMITTED`, and fix
it with the correct concurrency control (optimistic `@Version`, pessimistic row lock, or a single
atomic statement) rather than by escalating the global isolation level or shrinking the window.

## Scenario
`WalletService.withdraw(walletId, amount)` does the textbook three steps: **read** the balance,
**modify** it in memory, **write** it back. `WalletService.COMPUTE_WINDOW_MILLIS` models the real
work a handler does between read and write (limit revalidation, fee math, fraud checks).

The test fires **16** concurrent withdrawals of €1.00 at one wallet seeded with €1000.00,
released together by a barrier so they overlap inside the window. Correct end-state: balance
**€984.00** (16 withdrawals applied). Buggy end-state: balance is far higher — most withdrawals
read the same stale balance and overwrite each other, so they are **lost**.

## Tasks
1. **Diagnose (Analysis section).** Explain the mechanism: how `READ COMMITTED`'s per-statement
   snapshots let two transactions both read balance `B`, both write `B - amount`,
   last-writer-wins. Explain why a single atomic `SET balance = balance - :amount` would NOT lose
   the update *even at RC* (current read under a row lock vs. the per-statement snapshot). Contrast
   `REPEATABLE READ` and `SERIALIZABLE`, and PostgreSQL snapshot isolation vs InnoDB's
   `REPEATABLE READ` (abort-and-retry vs block), saying where the lost update is stopped in each.
2. **Fix `WalletService.withdraw`.** Pick exactly ONE of:
   - **(a) Optimistic** — add `@Version` to `Wallet`; on the optimistic-lock failure, **retry**
     the whole read-modify-write (bounded attempts; jittered backoff is better). Do not let the
     conflict escape to the caller.
   - **(b) Pessimistic** — read the row with `LockModeType.PESSIMISTIC_WRITE` (`SELECT … FOR
     UPDATE`) so concurrent writers serialise on the row.
   - **(c) Atomic** — replace the read-modify-write with one `UPDATE wallets SET balance =
     balance - :amount WHERE id = :id` and let the DB apply it under its own row lock.
   Do **not** touch the test, the window, or the global isolation level.
3. **Document the tradeoffs** of all three in the Analysis section (contention profile, retry
   storms, deadlock risk, where each is the right default).

## Acceptance criteria
- `./gradlew :p1-02-lost-update:test` is GREEN.
- Final balance is exactly `INITIAL_BALANCE - CONCURRENCY * WITHDRAWAL` (€984.00) — every
  withdrawal applied once, none lost.
- No unhandled failures (`failures` list stays empty): an optimistic fix must retry internally,
  not surface `OptimisticLockingFailureException` to the caller.
- The fix prevents the lost update for the right reason — not by removing `COMPUTE_WINDOW_MILLIS`,
  throttling the test, or weakening the assertion.

## Constraints
- Do not edit the test to make it pass.
- Do not remove or shrink `WalletService.COMPUTE_WINDOW_MILLIS`; correctness must tolerate a
  non-zero read-modify-write window.
- Do not raise the global transaction isolation level (no app-wide `SERIALIZABLE`) — fix it at
  the row, where a real system would.
- Testcontainers Postgres only — no manual DB setup (Docker must be running).

## Stretch goals
1. **Implement two approaches and contrast them empirically.** Add both optimistic-with-retry and
   pessimistic `FOR UPDATE`, run the same test against each, and report retry counts / wall-time.
   State which you would ship for a payments wallet and why.
2. **Invariant under the lock.** Add a no-overdraft rule (balance may never go negative). Seed a
   balance that covers only *some* of the 16 withdrawals and prove that, under your locking, the
   surplus withdrawals are rejected and the balance never goes negative — showing that isolation
   alone does not enforce a business invariant; the check must execute **inside** the locked path
   (`… WHERE id = :id AND balance >= :amount`, 0 rows ⇒ reject).

## How to run
```
./gradlew :p1-02-lost-update:test
```
(Docker must be running for Testcontainers.)

---

## Analysis (you fill this in)
> _TODO: mechanism of the lost update; isolation-level comparison table; why the atomic delta is
> safe at RC; PG snapshot isolation vs InnoDB RR; chosen fix and why; tradeoffs of optimistic vs
> pessimistic vs atomic._
