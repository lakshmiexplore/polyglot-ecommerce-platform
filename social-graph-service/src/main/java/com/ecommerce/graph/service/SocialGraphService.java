package com.ecommerce.graph.service;

import com.ecommerce.graph.node.ProductNode;
import com.ecommerce.graph.node.UserNode;
import com.ecommerce.graph.repository.ProductNodeRepository;
import com.ecommerce.graph.repository.UserNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialGraphService {

    private final UserNodeRepository userRepository;
    private final ProductNodeRepository productRepository;

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
        return userRepository.save(user);
    }

    @Transactional
    public UserNode followUser(String followerId, String targetUserId) {
        UserNode follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + followerId));
        UserNode target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found: " + targetUserId));

        follower.follow(target);
        return userRepository.save(follower);
    }

    public List<ProductNode> getFrequentlyBoughtTogether(String productId, int limit) {
        return productRepository.findFrequentlyBoughtTogether(productId, limit);
    }

    public List<ProductNode> getSocialRecommendations(String userId, int limit) {
        return productRepository.findRecommendedByFriends(userId, limit);
    }
}