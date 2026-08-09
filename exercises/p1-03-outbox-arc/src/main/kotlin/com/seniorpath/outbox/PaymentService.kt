package com.seniorpath.outbox

import com.seniorpath.outbox.entity.QueueEvent
import com.seniorpath.outbox.entity.Payment
import com.seniorpath.outbox.entity.PaymentStatus
import com.seniorpath.outbox.repository.EventRepository
import com.seniorpath.outbox.repository.PaymentRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class PaymentService(
    private val payments: PaymentRepository,
    private val events: EventRepository,
    private val tx: TransactionTemplate,
    private val crashPoint: CrashPoint,
) {

    // TODO(tasks 2+3, SPEC.md): commit point #2 below must become an outbox row + relay // allow: code-comment exercise skeleton TODO marker
    fun capture(orderId: String, amountMinor: Long): Long {
        val payment = tx.execute {
            val payment = payments.save(
                Payment(orderId = orderId, amountMinor = amountMinor, status = PaymentStatus.CAPTURED),
            )
            events.save(
                QueueEvent(
                    exchange = PaymentEvents.EXCHANGE,
                    routingKey = PaymentEvents.CAPTURED_ROUTING_KEY,
                    message = capturedPayload(payment)
                )
            )
            return@execute payment
        }!!

        crashPoint.maybeCrash(CrashPoint.AFTER_COMMIT_BEFORE_PUBLISH)
        return payment.id!!
    }

    private fun capturedPayload(payment: Payment): String =
        """{"paymentId":${payment.id},"orderId":"${payment.orderId}","amountMinor":${payment.amountMinor}}"""
}
