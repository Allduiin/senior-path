package com.seniorpath.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.seniorpath.outbox.entity.PayoutEntry
import com.seniorpath.outbox.entity.ProcessedEvent
import com.seniorpath.outbox.repository.PayoutLedgerRepository
import com.seniorpath.outbox.repository.ProcessedEventRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.amqp.support.converter.MessageConversionException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class PayoutLedgerConsumer(
    private val ledger: PayoutLedgerRepository,
    private val processedEventRepository: ProcessedEventRepository,
    private val crashPoint: CrashPoint,
    private val mapper: ObjectMapper,
    private val tx: TransactionTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [PaymentEvents.LEDGER_QUEUE])
    fun onPaymentCaptured(payload: String, @Header(AmqpHeaders.MESSAGE_ID) messageId: String) {
        runCatching {
            mapper.readValue<PaymentCapturedEvent>(payload)
        }.fold(
            onSuccess = { processEvent(payload, it, messageId) },
            onFailure = { throw MessageConversionException("Failed to deserialize payload", it) }
        )
    }

    private fun processEvent(payload: String, event: PaymentCapturedEvent, messageId: String) {
        try {
            tx.execute {
                processedEventRepository.saveAndFlush(ProcessedEvent(messageId))
                crashPoint.maybeCrash(CrashPoint.AFTER_CLAIM_BEFORE_EFFECT, payload)
                ledger.save(event.toPayoutEntry())
            }
        } catch (e: DataIntegrityViolationException) {
            logger.warn("Duplicate event: $payload, message id: $messageId", e)
        }
    }

    private fun PaymentCapturedEvent.toPayoutEntry(): PayoutEntry = PayoutEntry(
        orderId = orderId,
        amountMinor = amountMinor
    )
}
