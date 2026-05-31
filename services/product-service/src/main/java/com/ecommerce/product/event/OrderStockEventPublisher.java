package com.ecommerce.product.event;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderStockEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderStockEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public OrderStockEventPublisher(
            RabbitTemplate rabbitTemplate, @Value("${ecommerce.rabbitmq.exchange:commerce.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publishStockReserved(Long orderId, List<Map<String, Object>> items) {
        publish(CommerceEvent.stockReserved(orderId, items), "product.StockReserved");
    }

    public void publishStockReservationFailed(Long orderId, List<Map<String, Object>> failedItems) {
        publish(CommerceEvent.stockReservationFailed(orderId, failedItems), "product.StockReservationFailed");
    }

    private void publish(CommerceEvent event, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.debug("Published {} for order {}", event.eventType(), event.payload().get("orderId"));
    }
}
