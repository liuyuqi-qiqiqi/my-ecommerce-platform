package com.ecommerce.product.dto;

import java.util.List;

public record ProductSearchResultDto(
        List<ProductSummaryDto> items, int page, int pageSize, long total) {}
