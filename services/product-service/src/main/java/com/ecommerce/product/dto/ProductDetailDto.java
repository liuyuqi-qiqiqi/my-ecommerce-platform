package com.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductDetailDto(
        Long id,
        String name,
        BigDecimal price,
        String brandName,
        String primaryImageUrl,
        boolean inStock,
        String description,
        String categoryName,
        int stockQuantity) {}
