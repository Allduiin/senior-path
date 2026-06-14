# p1-01 — Transactional self-invocation (Spring proxy AOP)

| | |
|---|---|
| **Phase** | 1 — Distributed systems & transactional correctness |
| **Targets diagnostic** | **Q1** (tx propagation / proxy self-invocation) |
| **Start state** | RED — the integration test fails by design |
| **Done state** | GREEN — test passes because REQUIRES_NEW is genuinely applied |

## Objective
Prove the Spring proxy **self-invocation** problem empirically, then fix it without
weakening the assertion. You must end able to state — cold — *why* `this.method()` bypasses
declarative transaction advice, and the tradeoffs of each fix.

## Scenario
`PaymentLedgerService.processPayment(reference)` runs in an outer `@Transactional`. It:
1. records a payment row (committed business work), then
2. makes a **best-effort** audit write via `recordAuditAttempt(reference)`, which is annotated
   `@Transactional(propagation = REQUIRES_NEW)` and deliberately throws.

Intended semantics: the audit write runs in its **own** transaction, so its throw rolls
**only** that transaction back; the swallowed exception leaves the outer payment transaction
free to commit. Observable end state: **1 payment, 0 audit rows.**

The skeleton calls `recordAuditAttempt` through the implicit `this` reference. That call does
not traverse the AOP proxy, so `REQUIRES_NEW` is never applied; the audit insert joins the
outer transaction and commits with it. Observable end state with the bug: **1 payment,
1 audit row** → the test fails.

## Tasks
1. **Diagnose.** In the *Analysis* section below, explain the mechanism precisely: how Spring
   creates the transactional proxy (CGLIB subclass / JDK dynamic proxy), where the
   `TransactionInterceptor` sits, and why an internal `this` call never reaches it. Name the
   role of `kotlin("plugin.spring")` (all-open) in making the bean proxyable at all.
2. **Fix the call path** so a new transaction is genuinely started for the audit write. Choose
   **one** approach and implement it:
   - (a) **Separate bean** — move `recordAuditAttempt` into its own `@Service` and inject it.
   - (b) **Self-injection** — inject the proxy of this bean into itself and call through it.
   - (c) **`AopContext.currentProxy()`** — cast and call (requires `@EnableAspectJAutoProxy(exposeProxy = true)`).
3. **Document tradeoffs** of all three in the *Analysis* section (testability, readability,
   risk of misuse, what happens under further refactoring).

## Acceptance criteria
- `./gradlew :p1-01-tx-self-invocation:test` is **GREEN**.
- After `processPayment`, `payments.count() == 1` **and** `audits.count() == 0`.
- The audit row is absent **because its REQUIRES_NEW transaction rolled back** — not because
  the write or the throw was removed.
- The *Analysis* section is filled in with your own words.

## Constraints
- Do **not** edit the test to make it pass.
- Do **not** delete the `@Transactional(REQUIRES_NEW)` annotation or the `throw`.
- Keep the outer method `@Transactional`. Audit remains best-effort (exception swallowed).
- Kotlin only; no raw JDBC. Postgres comes from Testcontainers (no manual DB setup).

## Stretch goals
1. **Propagation matrix.** Add a second test proving that with `REQUIRED` (not `REQUIRES_NEW`)
   on a *correctly proxied* audit method, the thrown exception marks the **shared** transaction
   rollback-only — so the payment is lost too. Explain `UnexpectedRollbackException`.
2. **Pin the mechanism.** Add an assertion or log that demonstrates two distinct physical
   transactions occurred for the fixed case (e.g. inspect transaction names / connection use),
   so "it rolled back independently" is proven, not assumed.

## How to run
```
./gradlew :p1-01-tx-self-invocation:test
```
Requires Docker running (Testcontainers starts Postgres automatically).

---

## Analysis (you fill this in)

### Why self-invocation bypasses the proxy
> _TODO: your explanation._

### Approach chosen and why
> _TODO._

### Tradeoffs: separate bean vs self-injection vs AopContext
> _TODO._
