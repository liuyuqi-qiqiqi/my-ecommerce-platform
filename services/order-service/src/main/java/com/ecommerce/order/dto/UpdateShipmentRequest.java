package com.ecommerce.order.dto;

import com.ecommerce.order.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentRequest(
        String carrier, String trackingNumber, @NotNull ShipmentStatus status) {}
