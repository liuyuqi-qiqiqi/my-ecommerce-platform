package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryDto(
        Long id, String orderNumber, String status, BigDecimal totalAmount, Instant createdAt) {}
