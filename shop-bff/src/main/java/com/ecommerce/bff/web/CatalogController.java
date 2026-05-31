package com.ecommerce.bff.web;

import com.ecommerce.bff.client.ProductServiceClient;
import com.ecommerce.bff.dto.CatalogFilterMetaDto;
import com.ecommerce.bff.dto.ProductDetailDto;
import com.ecommerce.bff.dto.ProductSearchResultDto;
import com.ecommerce.bff.dto.ProductSummaryDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class CatalogController {

    private final ProductServiceClient productServiceClient;

    public CatalogController(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @GetMapping("/featured")
    public List<ProductSummaryDto> getFeaturedProducts() {
        return productServiceClient.getFeaturedProducts();
    }

    @GetMapping("/search")
    public ProductSearchResultDto searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return productServiceClient.searchProducts(q, categoryId, brandId, minPrice, maxPrice, page, pageSize);
    }

    @GetMapping("/filters")
    public CatalogFilterMetaDto getFilterMeta() {
        return productServiceClient.getFilterMeta();
    }

    @GetMapping("/{productId}")
    public ProductDetailDto getProductById(@PathVariable Long productId) {
        return productServiceClient.getProductById(productId);
    }
}
