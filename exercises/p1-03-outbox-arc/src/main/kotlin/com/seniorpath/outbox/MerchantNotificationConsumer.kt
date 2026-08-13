package com.seniorpath.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seniorpath.outbox.entity.NotificationLog
import com.seniorpath.outbox.repository.NotificationLogRepository
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class MerchantNotificationConsumer(
    private val notifications: NotificationLogRepository,
    private val crashPoint: CrashPoint,
    private val mapper: ObjectMapper,
) {

    // TODO(stage C, tasks 2-3 in SPEC.md): this consumer ships working-but-wrong — decide where // allow: code-comment exercise skeleton TODO marker
    //  the ack must sit (the containerFactory below is the seeded choice), then make the effect // allow: code-comment exercise skeleton TODO marker
    //  effectively-once. Keep crashPoint.maybeCrash(AFTER_CLAIM_BEFORE_NOTIFY, payload) between // allow: code-comment exercise skeleton TODO marker
    //  your claim and the notification write. // allow: code-comment exercise skeleton TODO marker
    @RabbitListener(queues = [PaymentEvents.NOTIFY_QUEUE], containerFactory = "fireAndForgetFactory")
    fun onPaymentCaptured(payload: String) {
        val event = mapper.readValue<PaymentCapturedEvent>(payload)
        crashPoint.maybeCrash(CrashPoint.AFTER_CLAIM_BEFORE_NOTIFY, payload)
        notifications.save(
            NotificationLog(orderId = event.orderId, amountMinor = event.amountMinor),
        )
    }
}
