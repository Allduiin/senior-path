package com.seniorpath.outbox

import com.seniorpath.outbox.repository.PayoutLedgerRepository
import org.springframework.stereotype.Component

@Component
class PayoutLedgerConsumer(
    private val ledger: PayoutLedgerRepository,
    private val crashPoint: CrashPoint,
) {

    // TODO(stage B, tasks 2-4 in SPEC.md): make this a listener on PaymentEvents.LEDGER_QUEUE that // allow: code-comment exercise skeleton TODO marker
    //  claims the idempotency key and credits the payout ledger in ONE local transaction, and keeps // allow: code-comment exercise skeleton TODO marker
    //  crashPoint.maybeCrash(CrashPoint.AFTER_CLAIM_BEFORE_EFFECT, payload) between the two. // allow: code-comment exercise skeleton TODO marker
    fun onPaymentCaptured(payload: String): Unit = TODO("stage B — see SPEC.md")
}
