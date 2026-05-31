package com.ecommerce.product.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductEventMessage(
        String eventId, String eventType, Instant timestamp, Map<String, Object> payload) {

    public static ProductEventMessage created(Long productId) {
        return new ProductEventMessage(
                UUID.randomUUID().toString(),
                "ProductCreated",
                Instant.now(),
                Map.of("productId", productId));
    }

    public static ProductEventMessage updated(Long productId) {
        return new ProductEventMessage(
                UUID.randomUUID().toString(),
                "ProductUpdated",
                Instant.now(),
                Map.of("productId", productId));
    }

    public static ProductEventMessage stockChanged(Long productId, int stockQuantity) {
        return new ProductEventMessage(
                UUID.randomUUID().toString(),
                "StockChanged",
                Instant.now(),
                Map.of("productId", productId, "stockQuantity", stockQuantity));
    }
}
