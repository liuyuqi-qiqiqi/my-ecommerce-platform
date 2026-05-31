package com.ecommerce.bff.dto;

import java.util.List;

public record ProductSearchResultDto(
        List<ProductSummaryDto> items, int page, int pageSize, long total) {}
