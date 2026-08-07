package com.seniorpath.outbox

object PaymentEvents {
    const val EXCHANGE = "payments"
    const val CAPTURED_ROUTING_KEY = "payment.captured"
    const val CAPTURED_QUEUE = "payment-captured"
}
