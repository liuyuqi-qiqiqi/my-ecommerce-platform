package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.event.CommerceEvent;
import com.ecommerce.order.event.OrderEventPublisher;
import com.ecommerce.order.repository.OrderRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelOrderService {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CancelOrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findDetailedByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> Map.<String, Object>of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity()))
                .toList();

        CommerceEvent event = CommerceEvent.orderCancelled(
                order.getId(), "Cancelled by user", items);
        eventPublisher.publishOrderCancelled(event);

        log.info("Order {} cancelled by user {}", order.getOrderNumber(), userId);
    }
}
