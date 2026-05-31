package com.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ecommerce.product.dto.ProductSearchResultDto;
import com.ecommerce.product.search.ProductDocument;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ProductSearchService productSearchService;

    @Test
    @SuppressWarnings("unchecked")
    void searchReturnsPagedSummaries() {
        ProductDocument doc = new ProductDocument();
        doc.setId(1L);
        doc.setName("Phone");
        doc.setPrice(999.0);
        doc.setBrandName("Acme");
        doc.setPrimaryImageUrl("img");
        doc.setStockQuantity(10);

        SearchHit<ProductDocument> hit = org.mockito.Mockito.mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc);
        SearchHits<ProductDocument> hits =
                new SearchHitsImpl<>(List.of(hit), new org.springframework.data.elasticsearch.core.TotalHits(1, TotalHitsRelation.EQUAL_TO), 1.0f);

        when(elasticsearchOperations.search(any(), eq(ProductDocument.class))).thenReturn(hits);

        ProductSearchResultDto result = productSearchService.search("phone", null, null, null, null, 1, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).name()).isEqualTo("Phone");
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchClampsPageSizeToMaximum() {
        SearchHits<ProductDocument> hits =
                new SearchHitsImpl<>(List.of(), new org.springframework.data.elasticsearch.core.TotalHits(0, TotalHitsRelation.EQUAL_TO), 0f);
        when(elasticsearchOperations.search(any(), eq(ProductDocument.class))).thenReturn(hits);

        ProductSearchResultDto result = productSearchService.search(null, null, null, null, null, 0, 500);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(100);
    }
}
