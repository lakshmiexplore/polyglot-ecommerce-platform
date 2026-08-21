package com.ecommerce.order.service;

import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.order.model.OrderByUser;
import com.ecommerce.order.model.OrderByUserKey;
import com.ecommerce.order.model.OrderEventAudit;
import com.ecommerce.order.model.OrderEventAuditEventKey;
import com.ecommerce.order.repository.OrderByUserRepository;
import com.ecommerce.order.repository.OrderEventAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderActivityService {

    private final OrderByUserRepository orderRepository;
    private final OrderEventAuditRepository auditRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    private static final String ORDER_TOPIC = "ecommerce.order.events";

    public OrderByUser createOrder(UUID userId, String customerEmail, BigDecimal totalAmount, List<String> productIds) {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();

        OrderByUser order = OrderByUser.builder()
                .key(OrderByUserKey.builder()
                        .userId(userId)
                        .createdAt(now)
                        .orderId(orderId)
                        .build())
                .customerEmail(customerEmail)
                .totalAmount(totalAmount)
                .status("CREATED")
                .build();

        // 1. Save to Cassandra Clustered Order Table
        OrderByUser savedOrder = orderRepository.save(order);

        // 2. Record initial event in the Cassandra audit append-log
        recordAudit(orderId, "ORDER_CREATED", "{\"status\":\"CREATED\",\"total\":" + totalAmount + "}");

        // 3. Publish OrderPlacedEvent to Kafka
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(orderId.toString())
                .userId(userId.toString())
                .productIds(productIds)
                .totalAmount(totalAmount)
                .timestamp(now)
                .build();

        kafkaTemplate.send(ORDER_TOPIC, userId.toString(), event);
        log.info("📢 [Kafka Published] Emitted OrderPlacedEvent for order: {} (user: {})", orderId, userId);

        return savedOrder;
    }

    public OrderEventAudit recordAudit(UUID orderId, String eventType, String payload) {
        OrderEventAudit audit = OrderEventAudit.builder()
                .key(OrderEventAuditEventKey.builder()
                        .orderId(orderId)
                        .occurredAt(Instant.now())
                        .eventId(UUID.randomUUID())
                        .build())
                .eventType(eventType)
                .payload(payload)
                .build();

        return auditRepository.save(audit);
    }

    public List<OrderByUser> getOrdersForUser(UUID userId) {
        return orderRepository.findByKeyUserId(userId);
    }

    public List<OrderEventAudit> getAuditStream(UUID orderId) {
        return auditRepository.findByKeyOrderId(orderId);
    }
}