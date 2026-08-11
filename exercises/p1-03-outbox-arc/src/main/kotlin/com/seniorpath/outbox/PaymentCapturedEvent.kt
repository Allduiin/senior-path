package com.seniorpath.outbox

data class PaymentCapturedEvent(
    val paymentId: Long,
    val orderId: String,
    val amountMinor: Long,
)