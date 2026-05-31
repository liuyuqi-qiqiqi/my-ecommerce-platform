package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.dto.OrderDetailDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.dto.ShipmentInfoDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    private final ObjectMapper objectMapper;
    private final ShipmentService shipmentService;

    public OrderMapper(ObjectMapper objectMapper, ShipmentService shipmentService) {
        this.objectMapper = objectMapper;
        this.shipmentService = shipmentService;
    }

    public OrderSummaryDto toSummary(Order order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt());
    }

    public OrderDetailDto toDetail(Order order) {
        List<OrderDetailDto.OrderItemDto> items = order.getItems().stream()
                .map(this::toItemDto)
                .toList();
        OrderDetailDto.AddressSnapshotDto address = toAddress(order.getAddressSnapshot());
        ShipmentInfoDto shipment = shipmentService.toInfo(order.getShipment());
        return new OrderDetailDto(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                items,
                address,
                shipment);
    }

    private OrderDetailDto.OrderItemDto toItemDto(OrderItem item) {
        BigDecimal lineSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderDetailDto.OrderItemDto(
                item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), lineSubtotal);
    }

    private OrderDetailDto.AddressSnapshotDto toAddress(String json) {
        try {
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<>() {});
            return new OrderDetailDto.AddressSnapshotDto(
                    map.get("recipientName"),
                    map.get("phone"),
                    map.get("province"),
                    map.get("city"),
                    map.get("district"),
                    map.get("street"),
                    map.get("postalCode"));
        } catch (Exception ex) {
            return new OrderDetailDto.AddressSnapshotDto("", "", "", "", "", "", "");
        }
    }
}
