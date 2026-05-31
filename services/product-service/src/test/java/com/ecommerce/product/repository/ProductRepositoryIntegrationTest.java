package com.ecommerce.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.product.domain.Brand;
import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("product_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findFeaturedActiveProducts() {
        Brand brand = new Brand();
        brand.setName("Acme");
        brand = brandRepository.save(brand);

        Category category = new Category();
        category.setName("Phones");
        category = categoryRepository.save(category);

        Product featured = new Product();
        featured.setSku("SKU-1");
        featured.setName("Featured Phone");
        featured.setPrice(BigDecimal.valueOf(999));
        featured.setBrand(brand);
        featured.setCategory(category);
        featured.setStockQuantity(10);
        featured.setFeatured(true);
        featured.setStatus(ProductStatus.ACTIVE);
        featured.setCreatedAt(Instant.now());
        featured.setUpdatedAt(Instant.now());
        productRepository.save(featured);

        Product inactive = new Product();
        inactive.setSku("SKU-2");
        inactive.setName("Inactive");
        inactive.setPrice(BigDecimal.ONE);
        inactive.setBrand(brand);
        inactive.setCategory(category);
        inactive.setStockQuantity(1);
        inactive.setFeatured(true);
        inactive.setStatus(ProductStatus.INACTIVE);
        inactive.setCreatedAt(Instant.now());
        inactive.setUpdatedAt(Instant.now());
        productRepository.save(inactive);

        var results = productRepository.findByFeaturedTrueAndStatusOrderByUpdatedAtDesc(ProductStatus.ACTIVE);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Featured Phone");
    }
}
