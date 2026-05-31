package com.ecommerce.order.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, BigDecimal subtotal, int itemCount) {

    public record CartItemDto(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineSubtotal,
            boolean available) {}
}
