package com.ecommerce.product.repository;

import com.ecommerce.product.domain.ProductImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findFirstByProductIdAndPrimaryImageTrueOrderBySortOrderAsc(Long productId);

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
}
