package com.seniorpath.outbox

import com.seniorpath.outbox.entity.QueueEvent
import com.seniorpath.outbox.repository.QueueEventRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class QueueTableHandler(
    private val rabbit: RabbitTemplate,
    private val events: QueueEventRepository,
    private val crashPoint: CrashPoint,
    ) {


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun handleEvents() {
        events.findUnsent().forEach {
            handleEvent(it)
            crashPoint.maybeCrash(CrashPoint.AFTER_PUBLISH_BEFORE_MARK, it.message)
            markAsSent(it)
        }
    }

    private fun handleEvent(event: QueueEvent) = with(event) {
        rabbit.convertAndSend(
            exchange,
            routingKey,
            message
        ) { message ->
            message.apply { messageProperties.messageId = event.id.toString() }
        }
    }

    private fun markAsSent(event: QueueEvent) {
        event.sentAt = LocalDateTime.now()
        events.save(event)
    }
}