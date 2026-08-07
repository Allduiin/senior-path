package com.seniorpath.outbox

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    @Bean
    fun paymentsExchange(): DirectExchange = DirectExchange(PaymentEvents.EXCHANGE)

    @Bean
    fun paymentCapturedQueue(): Queue = Queue(PaymentEvents.CAPTURED_QUEUE, true)

    @Bean
    fun paymentCapturedBinding(): Binding =
        BindingBuilder.bind(paymentCapturedQueue())
            .to(paymentsExchange())
            .with(PaymentEvents.CAPTURED_ROUTING_KEY)
}
