# Spring proxy AOP & `@Transactional` propagation

**Maps to:** Q1 (tx propagation / proxy self-invocation) · **Phase 1** · Exercise: `p1-01-tx-self-invocation`
[← back to index](../README.md)

## TL;DR
`@Transactional` is **not** code woven into your method — it's an **AOP interceptor** on a
**proxy** that wraps the bean. The transaction is begun/committed/rolled back by a
`TransactionInterceptor` delegating to a `PlatformTransactionManager`. Because the advice
lives on the proxy, **only calls that go through the proxy are transactional**. An internal
`this.method()` call hits the raw target and bypasses everything — propagation included.

## How it runs
```
caller → [proxy] → TransactionInterceptor → your @Transactional method
```
- The proxy delegates to **`TransactionInterceptor`** (an AOP `MethodInterceptor`).
- It asks a **`PlatformTransactionManager`** (JPA → `JpaTransactionManager`) to begin/join a tx,
  invokes the method, then `commit()` or `rollback()`.
- **Default rollback rule:** rolls back on **`RuntimeException` and `Error`** only — **not**
  checked exceptions. Override with `rollbackFor` / `noRollbackFor`. (All Kotlin exceptions are
  unchecked, so this matters more in Java, but a senior must know it.)

## Proxy types (and why Kotlin cares)
| | JDK dynamic proxy | CGLIB proxy |
|---|---|---|
| How | Implements the bean's **interface** (`java.lang.reflect.Proxy`) | Generates a **subclass** at runtime |
| Needs | An interface | Class/method **non-final** |
| Spring Boot default | — | **CGLIB** (`proxyTargetClass=true` since Boot 2.0) |

Consequences:
- **`private` and `final` methods are never proxied** (CGLIB can't override them).
- **Kotlin classes/methods are `final` by default** → not proxyable as-is.
- **`kotlin("plugin.spring")` (all-open plugin)** auto-opens classes annotated with Spring
  stereotypes (`@Component`, `@Transactional`, `@Configuration`, …). That's why our service
  needs no explicit `open`. Without it, you'd write `open class` / `open fun` by hand.

## Self-invocation (the Q1 bug)
The proxy wraps the **target**. External callers hold the **proxy**; but inside the bean,
`this` is the **raw target instance**. So `this.inner()` calls the method directly on the
target — the interceptor is never entered, and any `@Transactional` settings on `inner()`
(propagation, rollback) are **silently ignored**. This is moot-maker for *all* propagation
behavior: it only works if the call path goes through the proxy.

**Three fixes** (route the call through the proxy):
1. **Separate bean** — move the inner method to its own `@Service`, inject & call it. Cleanest; the call is genuinely external. Tradeoff: an extra type for what may be one method.
2. **Self-injection** — inject the bean's own proxy into itself, call `self.inner()`. Keeps one class. Tradeoff: looks odd, easy to misuse, can mask a design smell.
3. **`AopContext.currentProxy()`** — cast & call (requires `@EnableAspectJAutoProxy(exposeProxy = true)`). Tradeoff: couples code to Spring AOP internals; thread-local lookup; least readable.

## Propagation — all 7 behaviors
| Propagation | If a tx **exists** | If **no** tx | Notes / failure mode |
|---|---|---|---|
| **REQUIRED** (default) | Join it (one physical tx) | Create new | Inner failure poisons the **shared** tx → caller's commit throws **`UnexpectedRollbackException`** even if the exception was caught. |
| **REQUIRES_NEW** | **Suspend** it, start independent new tx | Create new | Two physical txns; independent commit/rollback. Needs suspension support. The audit/outbox tool. |
| **NESTED** | **Savepoint** in current tx | Behaves like REQUIRED | Rolls back **to savepoint** only. **`JpaTransactionManager` does not support it** → `NestedTransactionNotSupportedException`. |
| **SUPPORTS** | Join it | Run **non-transactionally** | "Use a tx if present." Behavior differs by presence — reason carefully. |
| **NOT_SUPPORTED** | **Suspend** it, run with no tx | Run with no tx | Force execution outside any tx (don't hold a connection/locks). Needs suspension support. |
| **MANDATORY** | Join it | **Throw** `IllegalTransactionStateException` | Contract guard: "must be called inside a tx." Starts nothing. |
| **NEVER** | **Throw** `IllegalTransactionStateException` | Run with no tx | Contract guard: "must NOT run in a tx." |

### Mnemonic — three axes
1. **Participate** if present, else create → `REQUIRED`.
2. **Isolate**: suspend + own tx → `REQUIRES_NEW`; suspend + no tx → `NOT_SUPPORTED`.
3. **Partial rollback** via savepoint → `NESTED`.
4. **Tolerant** (optional tx) → `SUPPORTS`.
5. **Assert context** (start nothing, just check) → `MANDATORY` (must have) / `NEVER` (must not).

### Cross-cutting gotchas
- **Suspension requires support.** `REQUIRES_NEW` / `NOT_SUPPORTED` rely on the tx manager
  unbinding/rebinding resources via `TransactionSynchronizationManager`. JPA/JTA/DataSource
  managers support this.
- **`REQUIRED` + caught inner exception ⇒ `UnexpectedRollbackException` + total loss.** This is
  the classic trap; `REQUIRES_NEW` is the answer when a sub-op must fail independently.
- **All propagation is gated on the proxy.** Under self-invocation, `MANDATORY` won't throw,
  `REQUIRES_NEW` won't suspend, `NEVER` won't guard — nothing runs.

## The p1-01 outcome
- **Buggy (self-invocation):** 1 payment **+ 1 audit** committed — `REQUIRES_NEW` ignored, audit
  joins outer tx, throw is swallowed, outer commits both.
- **Fixed (call via proxy):** **1 payment, 0 audit** — inner runs in its own tx, throws → that tx
  alone rolls back; swallowed exception lets the outer tx commit the payment.

## Self-check (answer cold)
1. What object begins/commits the tx, and what's the default rollback rule?
2. JDK vs CGLIB — what decides which, and what must be true of a Kotlin class to be proxied?
3. Why does `this.inner()` bypass `@Transactional`? What is `this` at that point?
4. `REQUIRED` vs `REQUIRES_NEW` when the inner method throws and the outer catches it?
5. Why is `NESTED` effectively unusable under JPA?
