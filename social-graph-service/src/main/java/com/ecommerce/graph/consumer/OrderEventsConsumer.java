package com.ecommerce.graph.consumer;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.graph.service.SocialGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private final SocialGraphService socialGraphService;

    @KafkaListener(topics = "ecommerce.order.events", groupId = "social-graph-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("📥 [Kafka Event Received] Processing order '{}' for User: {}", event.getOrderId(), event.getUserId());

        if (event.getProductIds() == null || event.getProductIds().isEmpty()) {
            return;
        }

        // Normalize UUID / 3-digit ID to Neo4j unified ID format (e.g. 'u106')
        String neo4jUserId = normalizeToNeo4jId(event.getUserId());

        for (String productId : event.getProductIds()) {
            try {
                socialGraphService.recordPurchase(neo4jUserId, productId);
                log.info(" Synced purchase: (:User {})-[:PURCHASED]->(:Product {})", neo4jUserId, productId);
            } catch (Exception ex) {
                log.error("Failed to sync purchase for user {} and product {}: {}", 
                        neo4jUserId, productId, ex.getMessage());
            }
        }
    }

    private String normalizeToNeo4jId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return "u000";
        }
        String clean = rawId.trim().toLowerCase();
        
        // If it's a UUID (e.g. 00000000-0000-0000-0000-000000000106)
        if (clean.contains("-")) {
            String lastSegment = clean.substring(clean.lastIndexOf("-") + 1).replaceFirst("^0+", "");
            return "u" + (lastSegment.isEmpty() ? "0" : lastSegment);
        }
        
        // If it's already "u106"
        if (clean.startsWith("u")) {
            return clean;
        }
        
        // If it's plain "106"
        return "u" + clean;
    }
}