package com.ecommerce.order.controller;

import com.ecommerce.order.model.OrderByUser;
import com.ecommerce.order.model.OrderEventAudit;
import com.ecommerce.order.service.OrderActivityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderActivityController {

    private final OrderActivityService orderService;

    @Data
    public static class CreateOrderRequest {
        private UUID userId;
        private String customerEmail;
        private BigDecimal totalAmount;
        private List<String> productIds;
    }

    @Data
    public static class AddEventRequest {
        private String eventType;
        private String payload;
    }

    @PostMapping
    public ResponseEntity<OrderByUser> createOrder(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(
                req.getUserId(),
                req.getCustomerEmail(),
                req.getTotalAmount(),
                req.getProductIds()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderByUser>> getUserOrders(@PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.getOrdersForUser(userId));
    }

    @PostMapping("/{orderId}/events")
    public ResponseEntity<OrderEventAudit> appendEvent(
            @PathVariable UUID orderId,
            @RequestBody AddEventRequest req) {
        return ResponseEntity.ok(orderService.recordAudit(orderId, req.getEventType(), req.getPayload()));
    }

    @GetMapping("/{orderId}/events")
    public ResponseEntity<List<OrderEventAudit>> getOrderEvents(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getAuditStream(orderId));
    }
}