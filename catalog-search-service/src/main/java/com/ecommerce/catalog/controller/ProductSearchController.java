package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.model.ProductDocument;
import com.ecommerce.catalog.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService searchService;

    @PostMapping
    public ResponseEntity<ProductDocument> createProduct(@RequestBody ProductDocument product) {
        return ResponseEntity.ok(searchService.indexProduct(product));
    }

    @GetMapping
    public ResponseEntity<Iterable<ProductDocument>> getAllProducts() {
        return ResponseEntity.ok(searchService.getAllProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDocument>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(searchService.searchProducts(q, category, minPrice, maxPrice));
    }
}