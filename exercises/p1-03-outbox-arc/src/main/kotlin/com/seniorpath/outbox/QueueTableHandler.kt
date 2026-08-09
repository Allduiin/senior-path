package com.seniorpath.outbox

import com.seniorpath.outbox.entity.QueueEvent
import com.seniorpath.outbox.repository.EventRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Example
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class QueueTableHandler(
    private val rabbit: RabbitTemplate,
    private val events: EventRepository,
    ) {


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun handleEvents() {
        events.findUnsent().forEach {
            handleEvent(it)
            markAsSent(it)
        }
    }

    private fun handleEvent(event: QueueEvent) = with(event) {
        rabbit.convertAndSend(
            exchange,
            routingKey,
            message,
        )
    }

    private fun markAsSent(event: QueueEvent) {
        event.sentAt = LocalDateTime.now()
        events.save(event)
    }
}