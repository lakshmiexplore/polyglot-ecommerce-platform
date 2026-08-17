package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderByUser;
import com.ecommerce.order.model.OrderByUserKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderByUserRepository extends CassandraRepository<OrderByUser, OrderByUserKey> {
    List<OrderByUser> findByKeyUserId(UUID userId);
}