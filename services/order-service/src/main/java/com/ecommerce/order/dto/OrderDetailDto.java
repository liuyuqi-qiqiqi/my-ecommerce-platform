package com.ecommerce.order.dto;

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
        AddressSnapshotDto address,
        ShipmentInfoDto shipment) {

    public record OrderItemDto(
            Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineSubtotal) {}

    public record AddressSnapshotDto(
            String recipientName,
            String phone,
            String province,
            String city,
            String district,
            String street,
            String postalCode) {}
}
