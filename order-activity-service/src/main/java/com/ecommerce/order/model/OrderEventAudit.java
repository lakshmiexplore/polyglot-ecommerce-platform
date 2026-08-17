package com.ecommerce.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_events_audit")
public class OrderEventAudit {

    @PrimaryKey
    private OrderEventAuditEventKey key;

    @Column("event_type")
    private String eventType;

    @Column("payload")
    private String payload;
}