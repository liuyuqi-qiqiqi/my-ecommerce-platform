package com.ecommerce.product.config;

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
    Queue productSearchIndexerQueue(
            @Value("${ecommerce.rabbitmq.product-index-queue:product.search-indexer}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Queue orderCreatedQueue(@Value("${ecommerce.rabbitmq.order-created-queue:product.order-created}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("commerce.events.dlx")
                .deadLetterRoutingKey("commerce.events.dlq")
                .build();
    }

    @Bean
    Queue orderCancelledQueue(
            @Value("${ecommerce.rabbitmq.order-cancelled-queue:product.order-cancelled}") String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("commerce.events.dlx")
                .deadLetterRoutingKey("commerce.events.dlq")
                .build();
    }

    @Bean
    Binding productCreatedBinding(Queue productSearchIndexerQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(productSearchIndexerQueue)
                .to(commerceEventsExchange)
                .with("product.ProductCreated");
    }

    @Bean
    Binding productUpdatedBinding(Queue productSearchIndexerQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(productSearchIndexerQueue)
                .to(commerceEventsExchange)
                .with("product.ProductUpdated");
    }

    @Bean
    Binding stockChangedBinding(Queue productSearchIndexerQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(productSearchIndexerQueue)
                .to(commerceEventsExchange)
                .with("product.StockChanged");
    }

    @Bean
    Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(commerceEventsExchange).with("order.OrderCreated");
    }

    @Bean
    Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange commerceEventsExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(commerceEventsExchange).with("order.OrderCancelled");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
