package com.seniorpath.outbox.config

import com.seniorpath.outbox.PaymentEvents
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

    @Bean
    fun payoutLedgerQueue(): Queue = Queue(PaymentEvents.LEDGER_QUEUE, true)

    @Bean
    fun payoutLedgerBinding(): Binding =
        BindingBuilder.bind(payoutLedgerQueue())
            .to(paymentsExchange())
            .with(PaymentEvents.CAPTURED_ROUTING_KEY)
}
