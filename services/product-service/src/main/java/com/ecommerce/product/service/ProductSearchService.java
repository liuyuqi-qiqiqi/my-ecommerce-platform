package com.ecommerce.product.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ecommerce.product.dto.ProductSearchResultDto;
import com.ecommerce.product.dto.ProductSummaryDto;
import com.ecommerce.product.search.ProductDocument;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public ProductSearchResultDto search(
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        filterQueries.add(Query.of(q -> q.term(t -> t.field("status").value("ACTIVE"))));

        if (StringUtils.hasText(keyword)) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .fields("name^3", "description", "brandName", "categoryName")
                    .query(keyword.trim()))));
        }
        if (categoryId != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("categoryId").value(categoryId))));
        }
        if (brandId != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("brandId").value(brandId))));
        }
        if (minPrice != null) {
            filterQueries.add(Query.of(q -> q.range(r -> r.field("price").gte(co.elastic.clients.json.JsonData.of(minPrice)))));
        }
        if (maxPrice != null) {
            filterQueries.add(Query.of(q -> q.range(r -> r.field("price").lte(co.elastic.clients.json.JsonData.of(maxPrice)))));
        }

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder().filter(filterQueries);
        if (!mustQueries.isEmpty()) {
            boolBuilder.must(mustQueries);
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .withPageable(PageRequest.of(safePage - 1, safePageSize))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        List<ProductSummaryDto> items = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toSummary)
                .toList();

        return new ProductSearchResultDto(items, safePage, safePageSize, hits.getTotalHits());
    }

    private ProductSummaryDto toSummary(ProductDocument doc) {
        return new ProductSummaryDto(
                doc.getId(),
                doc.getName(),
                BigDecimal.valueOf(doc.getPrice()),
                doc.getBrandName(),
                doc.getPrimaryImageUrl(),
                doc.getStockQuantity() != null && doc.getStockQuantity() > 0);
    }
}
