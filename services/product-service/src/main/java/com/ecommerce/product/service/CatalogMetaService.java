package com.ecommerce.product.service;

import com.ecommerce.product.dto.CatalogFilterMetaDto;
import com.ecommerce.product.dto.CatalogFilterMetaDto.FilterOptionDto;
import com.ecommerce.product.repository.BrandRepository;
import com.ecommerce.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogMetaService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public CatalogMetaService(CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    public CatalogFilterMetaDto getFilterMeta() {
        var categories = categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> new FilterOptionDto(c.getId(), c.getName()))
                .toList();
        var brands = brandRepository.findAllByOrderByNameAsc().stream()
                .map(b -> new FilterOptionDto(b.getId(), b.getName()))
                .toList();
        return new CatalogFilterMetaDto(categories, brands);
    }
}
