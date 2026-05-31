package com.ecommerce.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public ProductEventPublisher(
            RabbitTemplate rabbitTemplate, @Value("${ecommerce.rabbitmq.exchange:commerce.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publishCreated(Long productId) {
        publish(ProductEventMessage.created(productId), "product.ProductCreated");
    }

    public void publishUpdated(Long productId) {
        publish(ProductEventMessage.updated(productId), "product.ProductUpdated");
    }

    public void publishStockChanged(Long productId, int stockQuantity) {
        publish(ProductEventMessage.stockChanged(productId, stockQuantity), "product.StockChanged");
    }

    private void publish(ProductEventMessage message, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        log.debug("Published {} for product {}", message.eventType(), message.payload().get("productId"));
    }
}
