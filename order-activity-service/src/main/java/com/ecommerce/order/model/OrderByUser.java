package com.ecommerce.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders_by_user")
public class OrderByUser {

    @PrimaryKey
    private OrderByUserKey key;

    @Column("customer_email")
    private String customerEmail;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private String status;
}