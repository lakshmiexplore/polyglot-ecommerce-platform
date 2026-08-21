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

        for (String productId : event.getProductIds()) {
            try {
                socialGraphService.recordPurchase(event.getUserId(), productId);
                log.info(" Synced purchase: (:User {})-[:PURCHASED]->(:Product {})", event.getUserId(), productId);
            } catch (Exception ex) {
                log.error("Failed to sync purchase for user {} and product {}: {}", 
                        event.getUserId(), productId, ex.getMessage());
            }
        }
    }
}