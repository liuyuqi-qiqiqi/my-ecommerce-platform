package com.ecommerce.product.event;

import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductIndexEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexEventListener.class);

    private final ProductRepository productRepository;
    private final ProductIndexService productIndexService;

    public ProductIndexEventListener(ProductRepository productRepository, ProductIndexService productIndexService) {
        this.productRepository = productRepository;
        this.productIndexService = productIndexService;
    }

    @RabbitListener(queues = "${ecommerce.rabbitmq.product-index-queue:product.search-indexer}")
    public void handleProductEvent(ProductEventMessage message) {
        Object productIdObj = message.payload().get("productId");
        if (productIdObj == null) {
            log.warn("Received product event without productId: {}", message.eventType());
            return;
        }
        Long productId = Long.valueOf(productIdObj.toString());
        productRepository.findDetailedById(productId).ifPresentOrElse(productIndexService::indexProduct, () -> log.warn(
                "Product {} not found for indexing after event {}", productId, message.eventType()));
    }
}
