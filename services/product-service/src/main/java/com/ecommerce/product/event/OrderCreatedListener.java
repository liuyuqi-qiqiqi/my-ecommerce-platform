package com.ecommerce.product.event;

import com.ecommerce.product.service.IdempotencyService;
import com.ecommerce.product.service.StockReservationService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final IdempotencyService idempotencyService;
    private final StockReservationService stockReservationService;

    public OrderCreatedListener(IdempotencyService idempotencyService, StockReservationService stockReservationService) {
        this.idempotencyService = idempotencyService;
        this.stockReservationService = stockReservationService;
    }

    @RabbitListener(queues = "${ecommerce.rabbitmq.order-created-queue:product.order-created}")
    public void onOrderCreated(CommerceEvent event) {
        if (!idempotencyService.registerIfNew(event.eventId())) {
            return;
        }
        Long orderId = Long.valueOf(event.payload().get("orderId").toString());
        List<Map<String, Object>> items = extractItems(event.payload().get("items"));
        stockReservationService.reserveForOrder(orderId, items);
        log.info("Processed OrderCreated for orderId={}", orderId);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Object rawItems) {
        return (List<Map<String, Object>>) rawItems;
    }
}
