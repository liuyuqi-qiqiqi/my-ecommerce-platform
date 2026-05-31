package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductImage;
import com.ecommerce.product.dto.ProductDetailDto;
import com.ecommerce.product.dto.ProductSummaryDto;
import com.ecommerce.product.repository.ProductImageRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final ProductImageRepository productImageRepository;

    public ProductMapper(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }

    public ProductSummaryDto toSummary(Product product) {
        return new ProductSummaryDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getBrand().getName(),
                resolvePrimaryImageUrl(product),
                product.getStockQuantity() > 0);
    }

    public ProductDetailDto toDetail(Product product) {
        return new ProductDetailDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getBrand().getName(),
                resolvePrimaryImageUrl(product),
                product.getStockQuantity() > 0,
                product.getDescription(),
                product.getCategory().getName(),
                product.getStockQuantity());
    }

    public List<ProductSummaryDto> toSummaries(List<Product> products) {
        return products.stream().map(this::toSummary).toList();
    }

    private String resolvePrimaryImageUrl(Product product) {
        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getPrimaryImage()))
                .min(Comparator.comparing(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElseGet(() -> productImageRepository
                        .findFirstByProductIdAndPrimaryImageTrueOrderBySortOrderAsc(product.getId())
                        .map(ProductImage::getUrl)
                        .orElse("https://placehold.co/400x400?text=No+Image"));
    }
}
