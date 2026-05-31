package com.ecommerce.bff.client;

import com.ecommerce.bff.dto.CatalogFilterMetaDto;
import com.ecommerce.bff.dto.ProductDetailDto;
import com.ecommerce.bff.dto.ProductSearchResultDto;
import com.ecommerce.bff.dto.ProductSummaryDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", path = "/internal/products")
public interface ProductServiceClient {

    @GetMapping("/featured")
    List<ProductSummaryDto> getFeaturedProducts();

    @GetMapping("/search")
    ProductSearchResultDto searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @GetMapping("/filters")
    CatalogFilterMetaDto getFilterMeta();

    @GetMapping("/{productId}")
    ProductDetailDto getProductById(@PathVariable("productId") Long productId);
}
