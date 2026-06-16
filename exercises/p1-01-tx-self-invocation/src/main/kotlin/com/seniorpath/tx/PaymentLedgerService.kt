package com.seniorpath.tx

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * p1-01 — Spring proxy self-invocation (targets diagnostic Q1).
 *
 * SCENARIO
 * --------
 * [processPayment] runs in an outer transaction. It records a payment, then makes a
 * BEST-EFFORT audit write that is *supposed* to run in its own independent transaction
 * ([recordAuditAttempt], annotated REQUIRES_NEW) and that fails (throws). The intent:
 *   - the audit row rolls back independently (its own tx aborts), and
 *   - the payment row still commits (the swallowed failure does not poison the outer tx).
 *
 * THE BUG (do not "fix" it by deleting the annotation — fix the call path)
 * -----------------------------------------------------------------------
 * [processPayment] calls [recordAuditAttempt] via the implicit `this` reference. That call
 * does NOT pass through the Spring AOP proxy, so the @Transactional(REQUIRES_NEW) advice is
 * never applied. The audit insert therefore runs inside the OUTER transaction. Because the
 * thrown exception is caught here (best-effort), the outer transaction commits — and the
 * audit row is committed alongside the payment instead of being rolled back.
 *
 * The integration test asserts the audit table is EMPTY after [processPayment]. With this
 * self-invocation bug it contains one row, so the test FAILS BY DESIGN (red start).
 *
 * YOUR TASKS (see SPEC.md)
 * ------------------------
 *  1. Explain, in SPEC.md, the precise mechanism by which self-invocation bypasses the proxy.
 *  2. Make the test pass by routing the inner call through the proxy. Pick ONE approach and
 *     note the tradeoffs of all three in SPEC.md:
 *       (a) extract [recordAuditAttempt] into a SEPARATE @Service bean and inject it;
 *       (b) SELF-INJECT this bean (inject the proxy of itself) and call through it;
 *       (c) use `AopContext.currentProxy()` (requires exposeProxy = true).
 *  3. Do NOT weaken the test. The audit row must roll back because REQUIRES_NEW is genuinely
 *     applied — not because you stopped writing it or stopped throwing.
 */
@Service
class PaymentLedgerService(
    private val payments: PaymentRepository,
    private val auditService: AuditService,
) {

    @Transactional
    fun processPayment(reference: String) {
        payments.save(PaymentEntity(reference = reference))

        try {
            // BUG: self-invocation through `this` — the REQUIRES_NEW proxy advice is bypassed.
            // TODO(task 2): route this call through the proxy so a NEW transaction is started.
            auditService.recordAuditAttempt(reference)
        } catch (ex: AuditSinkUnavailableException) {
            // Audit is best-effort: we deliberately swallow the failure and let the payment commit.
            // (Correct behaviour: only the audit's OWN transaction should have rolled back.)
        }
    }
}

class AuditSinkUnavailableException(message: String) : RuntimeException(message)
