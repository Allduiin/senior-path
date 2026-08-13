package com.seniorpath.outbox.config

import com.seniorpath.outbox.PaymentEvents
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
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

    @Bean
    fun merchantNotifyQueue(): Queue = Queue(PaymentEvents.NOTIFY_QUEUE, true)

    @Bean
    fun merchantNotifyBinding(): Binding =
        BindingBuilder.bind(merchantNotifyQueue())
            .to(paymentsExchange())
            .with(PaymentEvents.CAPTURED_ROUTING_KEY)

    @Bean
    fun fireAndForgetFactory(
        configurer: SimpleRabbitListenerContainerFactoryConfigurer,
        connectionFactory: ConnectionFactory,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().also {
            configurer.configure(it, connectionFactory)
            it.setAcknowledgeMode(AcknowledgeMode.NONE)
        }
}
