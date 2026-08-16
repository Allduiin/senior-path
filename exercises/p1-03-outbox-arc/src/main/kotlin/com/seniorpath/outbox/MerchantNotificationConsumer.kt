package com.seniorpath.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seniorpath.outbox.entity.NotificationLog
import com.seniorpath.outbox.entity.ProcessedEvent
import com.seniorpath.outbox.repository.NotificationLogRepository
import com.seniorpath.outbox.repository.ProcessedEventRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

private const val NOTIFICATION_KEY = "notification"

@Component
class MerchantNotificationConsumer(
    private val notifications: NotificationLogRepository,
    private val crashPoint: CrashPoint,
    private val mapper: ObjectMapper,
    private val tx: TransactionTemplate,
    private val processedEventRepository: ProcessedEventRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [PaymentEvents.NOTIFY_QUEUE])
    fun onPaymentCaptured(payload: String, @Header(AmqpHeaders.MESSAGE_ID) messageId: String) {
        val event = mapper.readValue<PaymentCapturedEvent>(payload)
        try {
            tx.execute {
                processedEventRepository.saveAndFlush(ProcessedEvent("$NOTIFICATION_KEY:$messageId"))
                crashPoint.maybeCrash(CrashPoint.AFTER_CLAIM_BEFORE_NOTIFY, payload)
                notifications.save(
                    NotificationLog(orderId = event.orderId, amountMinor = event.amountMinor),
                )
            }
        } catch (e: DuplicateKeyException) {
            logger.debug("Duplicate event: $payload, message id: $messageId", e)
        }
    }
}
