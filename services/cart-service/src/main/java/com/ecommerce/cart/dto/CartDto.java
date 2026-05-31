package com.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, BigDecimal subtotal, int itemCount) {}
