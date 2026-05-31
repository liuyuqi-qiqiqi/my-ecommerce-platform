package com.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductStatus;
import com.ecommerce.product.dto.ProductDetailDto;
import com.ecommerce.product.dto.ProductSummaryDto;
import com.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void getFeaturedProductsReturnsMappedSummaries() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        ProductSummaryDto summary = new ProductSummaryDto(1L, "Phone", BigDecimal.TEN, "Brand", "img", true);

        when(productRepository.findByFeaturedTrueAndStatusOrderByUpdatedAtDesc(ProductStatus.ACTIVE))
                .thenReturn(List.of(product));
        when(productMapper.toSummaries(List.of(product))).thenReturn(List.of(summary));

        List<ProductSummaryDto> result = productService.getFeaturedProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Phone");
    }

    @Test
    void getProductByIdThrowsWhenInactive() {
        Product product = new Product();
        product.setStatus(ProductStatus.INACTIVE);

        when(productRepository.findDetailedById(99L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getProductByIdReturnsDetailForActiveProduct() {
        Product product = new Product();
        product.setStatus(ProductStatus.ACTIVE);
        ProductDetailDto detail = new ProductDetailDto(
                1L, "Phone", BigDecimal.TEN, "Brand", "img", true, "desc", "Cat", 5);

        when(productRepository.findDetailedById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDetail(product)).thenReturn(detail);

        ProductDetailDto result = productService.getProductById(1L);

        assertThat(result.name()).isEqualTo("Phone");
    }
}
