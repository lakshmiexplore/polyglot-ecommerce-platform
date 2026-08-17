package com.ecommerce.order.service;

import com.ecommerce.order.model.OrderByUser;
import com.ecommerce.order.model.OrderByUserKey;
import com.ecommerce.order.model.OrderEventAudit;
import com.ecommerce.order.model.OrderEventAuditEventKey;
import com.ecommerce.order.repository.OrderByUserRepository;
import com.ecommerce.order.repository.OrderEventAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderActivityService {

    private final OrderByUserRepository orderRepository;
    private final OrderEventAuditRepository auditRepository;

    public OrderByUser createOrder(UUID userId, String customerEmail, BigDecimal totalAmount) {
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

        OrderByUser savedOrder = orderRepository.save(order);

        // Record initial event in the audit append-log
        recordAudit(orderId, "ORDER_CREATED", "{\"status\":\"CREATED\",\"total\":" + totalAmount + "}");

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