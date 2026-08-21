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
//import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElse(UserNode.builder().id(id).build());
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public ProductNode saveProduct(String id, String name, String category) {
        ProductNode product = productRepository.findById(id)
                .orElse(ProductNode.builder().id(id).build());
        product.setName(name);
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public UserNode recordPurchase(String userId, String productId) {
        UserNode user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        ProductNode product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

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
            for (String followerId : followerIds) {
                socialCache.evict(followerId);
                log.info("Evicted social recommendation cache for follower: {}", followerId);
            }
        }

        return savedUser;
    }

    @Transactional
    @CacheEvict(value = "social-recommendations", key = "#followerId")
    public UserNode followUser(String followerId, String targetUserId) {
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
}