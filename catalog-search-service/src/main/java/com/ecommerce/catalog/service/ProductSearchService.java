package com.ecommerce.catalog.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ecommerce.catalog.model.ProductDocument;
import com.ecommerce.catalog.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductDocument indexProduct(ProductDocument product) {
        return repository.save(product);
    }

    public Iterable<ProductDocument> getAllProducts() {
        return repository.findAll();
    }

    // Fuzzy, multi-match full-text search across name, description, and tags
    public List<ProductDocument> searchProducts(String keyword, String category, Double minPrice, Double maxPrice) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    if (keyword != null && !keyword.isBlank()) {
                        b.must(m -> m.multiMatch(mm -> mm
                                .query(keyword)
                                .fields("name^3", "description^1", "tags^2")
                                .fuzziness("AUTO")
                        ));
                    }
                    if (category != null && !category.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("category").value(category)));
                    }
                    if (minPrice != null || maxPrice != null) {
                        b.filter(f -> f.range(r -> {
                            r.field("price");
                            if (minPrice != null) r.gte(co.elastic.clients.json.JsonData.of(minPrice));
                            if (maxPrice != null) r.lte(co.elastic.clients.json.JsonData.of(maxPrice));
                            return r;
                        }));
                    }
                    return b;
                }))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        return hits.stream().map(SearchHit::getContent).toList();
    }
}