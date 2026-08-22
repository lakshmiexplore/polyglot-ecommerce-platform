package com.ecommerce.graph.controller;

import com.ecommerce.graph.node.ProductNode;
import com.ecommerce.graph.node.UserNode;
import com.ecommerce.graph.service.SocialGraphService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/graph")
@RequiredArgsConstructor
public class SocialGraphController {

    private final SocialGraphService graphService;

    @Data
    public static class UpsertUserRequest {
        private String id;
        private String name;
        private String email;
    }

    @Data
    public static class UpsertProductRequest {
        private String id;
        private String name;
        private String category;
    }

    @PostMapping("/users")
    public ResponseEntity<UserNode> createUser(@RequestBody UpsertUserRequest req) {
        return ResponseEntity.ok(graphService.saveUser(req.getId(), req.getName(), req.getEmail()));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductNode> createProduct(@RequestBody UpsertProductRequest req) {
        return ResponseEntity.ok(graphService.saveProduct(req.getId(), req.getName(), req.getCategory()));
    }

    @PostMapping("/users/{userId}/purchases/{productId}")
    public ResponseEntity<UserNode> recordPurchase(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(graphService.recordPurchase(userId, productId));
    }

    @PostMapping("/users/{followerId}/follow/{targetUserId}")
    public ResponseEntity<UserNode> followUser(
            @PathVariable String followerId,
            @PathVariable String targetUserId) {
        return ResponseEntity.ok(graphService.followUser(followerId, targetUserId));
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseEntity.ok(graphService.getAllUsers());
    }

    @GetMapping("/recommendations/bought-together/{productId}")
    public ResponseEntity<List<ProductNode>> getFrequentlyBoughtTogether(
            @PathVariable String productId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(graphService.getFrequentlyBoughtTogether(productId, limit));
    }

    @GetMapping("/recommendations/social/{userId}")
    public ResponseEntity<List<ProductNode>> getSocialRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(graphService.getSocialRecommendations(userId, limit));
    }
}