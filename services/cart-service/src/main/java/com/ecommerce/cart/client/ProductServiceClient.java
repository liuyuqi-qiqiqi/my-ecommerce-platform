package com.ecommerce.cart.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/internal/products")
public interface ProductServiceClient {

    @GetMapping("/{productId}")
    ProductDetailDto getProductById(@PathVariable("productId") Long productId);

    record ProductDetailDto(
            Long id,
            String name,
            BigDecimal price,
            String brandName,
            String primaryImageUrl,
            boolean inStock,
            String description,
            String categoryName,
            int stockQuantity) {}
}
