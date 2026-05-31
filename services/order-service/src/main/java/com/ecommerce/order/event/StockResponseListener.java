package com.ecommerce.order.event;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.Shipment;
import com.ecommerce.order.domain.ShipmentStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.IdempotencyService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockResponseListener {

    private static final Logger log = LoggerFactory.getLogger(StockResponseListener.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final IdempotencyService idempotencyService;

    public StockResponseListener(
            OrderRepository orderRepository,
            OrderEventPublisher orderEventPublisher,
            IdempotencyService idempotencyService) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.idempotencyService = idempotencyService;
    }

    @RabbitListener(queues = "${ecommerce.rabbitmq.stock-reserved-queue:order.stock-reserved}")
    @Transactional
    public void onStockReserved(CommerceEvent event) {
        if (!idempotencyService.registerIfNew(event.eventId())) {
            return;
        }
        Long orderId = toLong(event.payload().get("orderId"));
        Order order = orderRepository.findDetailedById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.now());
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setStatusUpdatedAt(Instant.now());
        order.setShipment(shipment);
        orderRepository.save(order);

        orderEventPublisher.publishOrderConfirmed(
                CommerceEvent.orderConfirmed(order.getId(), order.getOrderNumber(), order.getUserId()));
        log.info("Order confirmed: orderId={}", orderId);
    }

    @RabbitListener(queues = "${ecommerce.rabbitmq.stock-failed-queue:order.stock-failed}")
    @Transactional
    public void onStockReservationFailed(CommerceEvent event) {
        if (!idempotencyService.registerIfNew(event.eventId())) {
            return;
        }
        Long orderId = toLong(event.payload().get("orderId"));
        Order order = orderRepository.findDetailedById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> Map.<String, Object>of(
                        "productId", item.getProductId(), "quantity", item.getQuantity()))
                .toList();
        orderEventPublisher.publishOrderCancelled(
                CommerceEvent.orderCancelled(orderId, "STOCK_RESERVATION_FAILED", items));
        log.warn("Order cancelled due to stock failure: orderId={}", orderId);
    }

    private Long toLong(Object value) {
        return Long.valueOf(value.toString());
    }
}
