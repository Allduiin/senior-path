package com.seniorpath.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.seniorpath.outbox.entity.QueueEvent
import com.seniorpath.outbox.entity.Payment
import com.seniorpath.outbox.entity.PaymentStatus
import com.seniorpath.outbox.repository.QueueEventRepository
import com.seniorpath.outbox.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class PaymentService(
    private val payments: PaymentRepository,
    private val events: QueueEventRepository,
    private val tx: TransactionTemplate,
    private val crashPoint: CrashPoint,
    private val mapper: ObjectMapper,
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
                    message = mapper.writeValueAsString(payment.toEvent()),
                )
            )
            return@execute payment
        }!!

        crashPoint.maybeCrash(CrashPoint.AFTER_COMMIT_BEFORE_PUBLISH)
        return payment.id!!
    }

    private fun Payment.toEvent() = PaymentCapturedEvent(
        paymentId = id!!,
        orderId = orderId,
        amountMinor = amountMinor,
    )
}
