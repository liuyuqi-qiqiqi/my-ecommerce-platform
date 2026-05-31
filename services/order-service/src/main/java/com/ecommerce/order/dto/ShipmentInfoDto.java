package com.ecommerce.order.dto;

import java.time.Instant;

public record ShipmentInfoDto(
        String carrier, String trackingNumber, String status, Instant statusUpdatedAt) {}
