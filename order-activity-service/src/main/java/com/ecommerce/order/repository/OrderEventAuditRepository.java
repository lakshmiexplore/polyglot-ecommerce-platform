package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderEventAudit;
import com.ecommerce.order.model.OrderEventAuditEventKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderEventAuditRepository extends CassandraRepository<OrderEventAudit, OrderEventAuditEventKey> {
    List<OrderEventAudit> findByKeyOrderId(UUID orderId);
}