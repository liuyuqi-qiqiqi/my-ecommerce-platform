package com.ecommerce.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSummaryDto(
        Long id,
        String name,
        BigDecimal price,
        String brandName,
        String primaryImageUrl,
        boolean inStock) {}
