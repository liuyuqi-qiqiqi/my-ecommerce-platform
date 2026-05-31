package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.domain.Shipment;
import com.ecommerce.order.domain.ShipmentStatus;
import com.ecommerce.order.dto.ShipmentInfoDto;
import com.ecommerce.order.dto.UpdateShipmentRequest;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentUpdateService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentUpdateService.class);

    private final OrderRepository orderRepository;
    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    public ShipmentUpdateService(
            OrderRepository orderRepository, ShipmentService shipmentService, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.shipmentService = shipmentService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ShipmentInfoDto updateShipment(Long orderId, UpdateShipmentRequest request) {
        Order order = orderRepository
                .findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot update shipment for order in status: " + order.getStatus());
        }

        Shipment shipment = order.getShipment();
        if (shipment == null) {
            shipment = new Shipment();
            shipment.setOrder(order);
            order.setShipment(shipment);
        }

        Instant now = Instant.now();
        shipment.setCarrier(request.carrier());
        shipment.setTrackingNumber(request.trackingNumber());
        shipment.setStatus(request.status());
        shipment.setStatusUpdatedAt(now);
        shipment.setEvents(appendEvent(shipment.getEvents(), request.status(), now));

        syncOrderStatus(order, request.status(), now);
        orderRepository.save(order);

        log.info(
                "AUDIT shipment_updated orderId={} status={} carrier={} trackingNumber={}",
                orderId,
                request.status(),
                request.carrier(),
                request.trackingNumber());
        return shipmentService.toInfo(shipment);
    }

    private void syncOrderStatus(Order order, ShipmentStatus shipmentStatus, Instant now) {
        if (shipmentStatus == ShipmentStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
        } else if (shipmentStatus == ShipmentStatus.SHIPPED || shipmentStatus == ShipmentStatus.IN_TRANSIT) {
            order.setStatus(OrderStatus.SHIPPED);
        }
        order.setUpdatedAt(now);
    }

    private String appendEvent(String existingJson, ShipmentStatus status, Instant at) {
        List<Map<String, Object>> events = readEvents(existingJson);
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("status", status.name());
        event.put("at", at.toString());
        events.add(event);
        try {
            return objectMapper.writeValueAsString(events);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize shipment events", ex);
        }
    }

    private List<Map<String, Object>> readEvents(String existingJson) {
        if (existingJson == null || existingJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(existingJson, new TypeReference<>() {});
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
