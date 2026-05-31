package com.ecommerce.product.repository;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"brand", "category", "images"})
    List<Product> findByFeaturedTrueAndStatusOrderByUpdatedAtDesc(ProductStatus status);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findDetailedById(@Param("id") Long id);

    long countByStatus(ProductStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity, p.updatedAt = :updatedAt "
            + "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity, @Param("updatedAt") Instant updatedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity, p.updatedAt = :updatedAt WHERE p.id = :productId")
    int releaseStock(@Param("productId") Long productId, @Param("quantity") int quantity, @Param("updatedAt") Instant updatedAt);

    @Query("SELECT p.stockQuantity FROM Product p WHERE p.id = :productId")
    Optional<Integer> findStockQuantity(@Param("productId") Long productId);
}
