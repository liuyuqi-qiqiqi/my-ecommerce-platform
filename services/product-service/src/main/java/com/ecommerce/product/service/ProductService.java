package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductStatus;
import com.ecommerce.product.dto.ProductDetailDto;
import com.ecommerce.product.dto.ProductSummaryDto;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductSummaryDto> getFeaturedProducts() {
        List<Product> products =
                productRepository.findByFeaturedTrueAndStatusOrderByUpdatedAtDesc(ProductStatus.ACTIVE);
        return productMapper.toSummaries(products);
    }

    public ProductDetailDto getProductById(Long productId) {
        Product product = productRepository
                .findDetailedById(productId)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        return productMapper.toDetail(product);
    }
}
