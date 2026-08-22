package com.ecommerce.graph.service;

import com.ecommerce.graph.node.ProductNode;
import com.ecommerce.graph.node.UserNode;
import com.ecommerce.graph.repository.ProductNodeRepository;
import com.ecommerce.graph.repository.UserNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialGraphService {

    private final UserNodeRepository userRepository;
    private final ProductNodeRepository productRepository;
    private final CacheManager cacheManager;

    @Transactional
    public UserNode saveUser(String id, String name, String email) {
        UserNode user = userRepository.findById(id)
                .orElseGet(() -> UserNode.builder()
                        .id(id)
                        .purchasedProducts(new HashSet<>())
                        .following(new HashSet<>())
                        .build());
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public ProductNode saveProduct(String id, String name, String category) {
        ProductNode product = productRepository.findById(id)
                .orElseGet(() -> ProductNode.builder().id(id).build());
        product.setName(name);
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public UserNode recordPurchase(String userId, String productId) {
        // Auto-create UserNode if this is their first purchase
        UserNode user = userRepository.findById(userId).orElseGet(() -> {
            log.info("Auto-creating User node in Neo4j during order sync: {}", userId);
            return UserNode.builder()
                    .id(userId)
                    .name("User " + userId)
                    .email(userId + "@example.com")
                    .purchasedProducts(new HashSet<>())
                    .following(new HashSet<>())
                    .build();
        });

        // Auto-create ProductNode if missing
        ProductNode product = productRepository.findById(productId).orElseGet(() -> {
            log.info("Auto-creating Product node in Neo4j during order sync: {}", productId);
            return productRepository.save(ProductNode.builder()
                    .id(productId)
                    .name(productId.toUpperCase())
                    .category("General")
                    .build());
        });

        user.purchase(product);
        UserNode savedUser = userRepository.save(user);

        // 1. Evict the bought-together cache for this specific product
        Cache boughtTogetherCache = cacheManager.getCache("bought-together");
        if (boughtTogetherCache != null) {
            boughtTogetherCache.evict(productId);
            log.info("Evicted 'bought-together' cache for product {}", productId);
        }

        // 2. Targeted Eviction: Find followers of this user and evict only their feeds
        List<String> followerIds = userRepository.findFollowerIds(userId);
        Cache socialCache = cacheManager.getCache("social-recommendations");
        if (socialCache != null) {
            // Evict the buyer's own feed (to filter out the item they just purchased)
            socialCache.evict(userId);

            // Evict feeds of all direct followers
            if (followerIds != null) {
                for (String followerId : followerIds) {
                    socialCache.evict(followerId);
                    log.info("Evicted social recommendation cache for follower: {}", followerId);
                }
            }
        }

        return savedUser;
    }

    @Transactional
    @CacheEvict(value = "social-recommendations", key = "#followerId")
    public UserNode followUser(String followerId, String targetUserId) {
        if (followerId == null || targetUserId == null || followerId.trim().equalsIgnoreCase(targetUserId.trim())) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }

        log.info("User {} followed {} -> evicting social recommendation cache for {}", followerId, targetUserId, followerId);
        UserNode follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + followerId));
        UserNode target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found: " + targetUserId));

        follower.follow(target);
        return userRepository.save(follower);
    }

    @Cacheable(value = "bought-together", key = "#productId")
    public List<ProductNode> getFrequentlyBoughtTogether(String productId, int limit) {
        log.info("⚡ [CACHE MISS] Querying Neo4j for bought-together recommendations on product: {}", productId);
        return productRepository.findFrequentlyBoughtTogether(productId, limit);
    }

    @Cacheable(value = "social-recommendations", key = "#userId")
    public List<ProductNode> getSocialRecommendations(String userId, int limit) {
        log.info("⚡ [CACHE MISS] Querying Neo4j for social recommendations on user: {}", userId);
        return productRepository.findRecommendedByFriends(userId, limit);
    }

    public List<UserNode> getAllUsers() {
        return userRepository.findAll();
    }
}