package com.acmecorp.retailhub.orders.repository;

import com.acmecorp.retailhub.orders.domain.OrderEntity;
import com.acmecorp.retailhub.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
}


