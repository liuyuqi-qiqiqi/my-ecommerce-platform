package com.ecommerce.cart.config;

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
    Queue orderConfirmedQueue(
            @Value("${ecommerce.rabbitmq.order-confirmed-queue:cart.order-confirmed}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("commerce.events.dlx")
                .deadLetterRoutingKey("commerce.events.dlq")
                .build();
    }

    @Bean
    Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(commerceEventsExchange).with("order.OrderConfirmed");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
