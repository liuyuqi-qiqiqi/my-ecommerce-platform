package com.ecommerce.product.service;

import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductImage;
import com.ecommerce.product.search.ProductDocument;
import com.ecommerce.product.search.ProductSearchRepository;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class ProductIndexService {

    private final ProductSearchRepository productSearchRepository;

    public ProductIndexService(ProductSearchRepository productSearchRepository) {
        this.productSearchRepository = productSearchRepository;
    }

    public void indexProduct(Product product) {
        productSearchRepository.save(toDocument(product));
    }

    public void deleteProduct(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    public long countIndexedProducts() {
        return productSearchRepository.count();
    }

    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setName(product.getName());
        doc.setDescription(product.getDescription());
        doc.setPrice(product.getPrice().doubleValue());
        doc.setBrandId(product.getBrand().getId());
        doc.setBrandName(product.getBrand().getName());
        doc.setCategoryId(product.getCategory().getId());
        doc.setCategoryName(product.getCategory().getName());
        doc.setStockQuantity(product.getStockQuantity());
        doc.setFeatured(product.getFeatured());
        doc.setStatus(product.getStatus().name());
        doc.setPrimaryImageUrl(product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getPrimaryImage()))
                .min(Comparator.comparing(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElse("https://placehold.co/400x400?text=No+Image"));
        return doc;
    }
}
