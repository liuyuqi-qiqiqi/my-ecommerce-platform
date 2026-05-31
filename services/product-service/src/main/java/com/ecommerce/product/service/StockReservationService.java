package com.ecommerce.product.service;

import com.ecommerce.product.event.OrderStockEventPublisher;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.repository.ProductRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockReservationService {

    private static final Logger log = LoggerFactory.getLogger(StockReservationService.class);

    private final ProductRepository productRepository;
    private final OrderStockEventPublisher orderStockEventPublisher;
    private final ProductEventPublisher productEventPublisher;

    public StockReservationService(
            ProductRepository productRepository,
            OrderStockEventPublisher orderStockEventPublisher,
            ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.orderStockEventPublisher = orderStockEventPublisher;
        this.productEventPublisher = productEventPublisher;
    }

    @Transactional
    public void reserveForOrder(Long orderId, List<Map<String, Object>> items) {
        List<Map<String, Object>> reservedItems = new ArrayList<>();
        List<Map<String, Object>> failedItems = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Long productId = toLong(item.get("productId"));
            int quantity = toInt(item.get("quantity"));
            int updated = productRepository.reserveStock(productId, quantity, Instant.now());
            if (updated == 1) {
                reservedItems.add(Map.of("productId", productId, "quantity", quantity, "reserved", true));
                productRepository.findStockQuantity(productId).ifPresent(stock -> productEventPublisher.publishStockChanged(productId, stock));
            } else {
                int available = productRepository.findStockQuantity(productId).orElse(0);
                failedItems.add(Map.of(
                        "productId", productId,
                        "requestedQuantity", quantity,
                        "availableQuantity", available));
            }
        }

        if (failedItems.isEmpty()) {
            orderStockEventPublisher.publishStockReserved(orderId, reservedItems);
            log.info("Stock reserved for orderId={}", orderId);
        } else {
            rollbackReserved(reservedItems);
            orderStockEventPublisher.publishStockReservationFailed(orderId, failedItems);
            log.warn("Stock reservation failed for orderId={}", orderId);
        }
    }

    @Transactional
    public void releaseForOrder(Long orderId, List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long productId = toLong(item.get("productId"));
            int quantity = toInt(item.get("quantity"));
            productRepository.releaseStock(productId, quantity, Instant.now());
            productRepository.findStockQuantity(productId).ifPresent(stock -> productEventPublisher.publishStockChanged(productId, stock));
        }
        log.info("Stock released for cancelled orderId={}", orderId);
    }

    private void rollbackReserved(List<Map<String, Object>> reservedItems) {
        for (Map<String, Object> item : reservedItems) {
            Long productId = toLong(item.get("productId"));
            int quantity = toInt(item.get("quantity"));
            productRepository.releaseStock(productId, quantity, Instant.now());
        }
    }

    private Long toLong(Object value) {
        return Long.valueOf(value.toString());
    }

    private int toInt(Object value) {
        return Integer.parseInt(value.toString());
    }
}
