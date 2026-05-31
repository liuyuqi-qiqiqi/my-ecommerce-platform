package com.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductSummaryDto(
        Long id,
        String name,
        BigDecimal price,
        String brandName,
        String primaryImageUrl,
        boolean inStock) {}
