package com.ecommerce.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDetailDto(
        Long id,
        String orderNumber,
        String status,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String paymentMethod,
        Instant createdAt,
        List<OrderItemDto> items,
        AddressDto address,
        ShipmentInfoDto shipment) {

    public record OrderItemDto(
            Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineSubtotal) {}
}
