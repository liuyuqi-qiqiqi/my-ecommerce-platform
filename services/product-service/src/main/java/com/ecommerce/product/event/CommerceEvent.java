package com.ecommerce.product.event;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CommerceEvent(String eventId, String eventType, Instant timestamp, Map<String, Object> payload) {

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
