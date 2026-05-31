package com.ecommerce.order.dto;

import java.util.List;

public record OrderPageDto(List<OrderSummaryDto> items, int page, int pageSize, long total) {}
