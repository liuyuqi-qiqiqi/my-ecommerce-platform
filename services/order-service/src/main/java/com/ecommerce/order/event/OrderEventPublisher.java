package com.ecommerce.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public OrderEventPublisher(
            RabbitTemplate rabbitTemplate, @Value("${ecommerce.rabbitmq.exchange:commerce.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publishOrderCreated(CommerceEvent event) {
        publish(event, "order.OrderCreated");
    }

    public void publishOrderConfirmed(CommerceEvent event) {
        publish(event, "order.OrderConfirmed");
    }

    public void publishOrderCancelled(CommerceEvent event) {
        publish(event, "order.OrderCancelled");
    }

    private void publish(CommerceEvent event, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.debug("Published {} for order {}", event.eventType(), event.payload().get("orderId"));
    }
}
