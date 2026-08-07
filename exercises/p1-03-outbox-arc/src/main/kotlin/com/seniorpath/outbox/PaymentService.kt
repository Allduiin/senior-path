package com.seniorpath.outbox

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class PaymentService(
    private val payments: PaymentRepository,
    private val tx: TransactionTemplate,
    private val rabbit: RabbitTemplate,
    private val crashPoint: CrashPoint,
) {

    // TODO(tasks 2+3, SPEC.md): commit point #2 below must become an outbox row + relay // allow: code-comment exercise skeleton TODO marker
    fun capture(orderId: String, amountMinor: Long): Long {
        val payment = tx.execute {
            payments.save(
                Payment(orderId = orderId, amountMinor = amountMinor, status = PaymentStatus.CAPTURED),
            )
        }!!

        crashPoint.maybeCrash(CrashPoint.AFTER_COMMIT_BEFORE_PUBLISH)

        rabbit.convertAndSend(
            PaymentEvents.EXCHANGE,
            PaymentEvents.CAPTURED_ROUTING_KEY,
            capturedPayload(payment),
        )
        return payment.id!!
    }

    private fun capturedPayload(payment: Payment): String =
        """{"paymentId":${payment.id},"orderId":"${payment.orderId}","amountMinor":${payment.amountMinor}}"""
}
