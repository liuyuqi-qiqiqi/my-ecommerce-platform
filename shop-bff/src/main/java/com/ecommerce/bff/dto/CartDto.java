package com.ecommerce.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, BigDecimal subtotal, int itemCount) {}
