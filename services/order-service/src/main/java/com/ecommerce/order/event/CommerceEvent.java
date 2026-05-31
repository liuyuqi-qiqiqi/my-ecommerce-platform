package com.ecommerce.order.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CommerceEvent(String eventId, String eventType, Instant timestamp, Map<String, Object> payload) {

    public static CommerceEvent orderCreated(
            Long orderId, String orderNumber, Long userId, List<Map<String, Object>> items) {
        return new CommerceEvent(
                UUID.randomUUID().toString(),
                "OrderCreated",
                Instant.now(),
                Map.of(
                        "orderId", orderId,
                        "orderNumber", orderNumber,
                        "userId", userId,
                        "items", items));
    }

    public static CommerceEvent orderConfirmed(Long orderId, String orderNumber, Long userId) {
        return new CommerceEvent(
                UUID.randomUUID().toString(),
                "OrderConfirmed",
                Instant.now(),
                Map.of("orderId", orderId, "orderNumber", orderNumber, "userId", userId));
    }

    public static CommerceEvent orderCancelled(Long orderId, String reason, List<Map<String, Object>> items) {
        return new CommerceEvent(
                UUID.randomUUID().toString(),
                "OrderCancelled",
                Instant.now(),
                Map.of("orderId", orderId, "reason", reason, "items", items));
    }

    public static CommerceEvent stockReserved(Long orderId, List<Map<String, Object>> items) {
        return new CommerceEvent(
                UUID.randomUUID().toString(),
                "StockReserved",
                Instant.now(),
                Map.of("orderId", orderId, "items", items));
    }

    public static CommerceEvent stockReservationFailed(Long orderId, List<Map<String, Object>> failedItems) {
        return new CommerceEvent(
                UUID.randomUUID().toString(),
                "StockReservationFailed",
                Instant.now(),
                Map.of("orderId", orderId, "failedItems", failedItems));
    }
}
