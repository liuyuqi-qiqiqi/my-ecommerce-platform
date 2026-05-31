package com.ecommerce.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineSubtotal,
        boolean available) {}
