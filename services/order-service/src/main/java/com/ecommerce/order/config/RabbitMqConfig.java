package com.ecommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    TopicExchange commerceEventsExchange(@Value("${ecommerce.rabbitmq.exchange:commerce.events}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange("commerce.events.dlx", true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable("commerce.events.dlq").build();
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("commerce.events.dlq");
    }

    @Bean
    Queue stockReservedQueue(@Value("${ecommerce.rabbitmq.stock-reserved-queue:order.stock-reserved}") String name) {
        return QueueBuilder.durable(name)
                .deadLetterExchange("commerce.events.dlx")
                .deadLetterRoutingKey("commerce.events.dlq")
                .build();
    }

    @Bean
    Queue stockFailedQueue(@Value("${ecommerce.rabbitmq.stock-failed-queue:order.stock-failed}") String name) {
        return QueueBuilder.durable(name)
                .deadLetterExchange("commerce.events.dlx")
                .deadLetterRoutingKey("commerce.events.dlq")
                .build();
    }

    @Bean
    Binding stockReservedBinding(Queue stockReservedQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(stockReservedQueue).to(commerceEventsExchange).with("product.StockReserved");
    }

    @Bean
    Binding stockFailedBinding(Queue stockFailedQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(stockFailedQueue).to(commerceEventsExchange).with("product.StockReservationFailed");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
