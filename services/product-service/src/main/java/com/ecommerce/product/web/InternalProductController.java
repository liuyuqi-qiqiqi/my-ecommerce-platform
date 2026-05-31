package com.ecommerce.product.web;

import com.ecommerce.product.dto.CatalogFilterMetaDto;
import com.ecommerce.product.dto.ProductDetailDto;
import com.ecommerce.product.dto.ProductSearchResultDto;
import com.ecommerce.product.dto.ProductSummaryDto;
import com.ecommerce.product.service.CatalogMetaService;
import com.ecommerce.product.service.ProductSearchService;
import com.ecommerce.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final CatalogMetaService catalogMetaService;

    public InternalProductController(
            ProductService productService,
            ProductSearchService productSearchService,
            CatalogMetaService catalogMetaService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.catalogMetaService = catalogMetaService;
    }

    @GetMapping("/featured")
    public List<ProductSummaryDto> getFeaturedProducts() {
        return productService.getFeaturedProducts();
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
        return productSearchService.search(q, categoryId, brandId, minPrice, maxPrice, page, pageSize);
    }

    @GetMapping("/filters")
    public CatalogFilterMetaDto getFilterMeta() {
        return catalogMetaService.getFilterMeta();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }
}
