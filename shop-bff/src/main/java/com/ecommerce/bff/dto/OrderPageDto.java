package com.ecommerce.bff.dto;

import java.util.List;

public record OrderPageDto(List<OrderSummaryDto> items, int page, int pageSize, long total) {}
