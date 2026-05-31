package com.ecommerce.bff.dto;

import java.util.List;

public record CatalogFilterMetaDto(List<FilterOptionDto> categories, List<FilterOptionDto> brands) {

    public record FilterOptionDto(Long id, String name) {}
}
