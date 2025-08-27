package com.acmecorp.retailhub.orders.service;

import com.acmecorp.retailhub.orders.api.dto.OrderDtos;
import com.acmecorp.retailhub.orders.domain.OrderEntity;
import com.acmecorp.retailhub.orders.domain.OrderStatus;
import com.acmecorp.retailhub.orders.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderEntity createOrder(OrderDtos.OrderCreate create) {
        OrderEntity entity = OrderMapper.toEntityFromCreate(create);

        entity.setStatus(OrderStatus.PENDING_PAYMENT);
        entity.setSubtotalCents(0);
        entity.setTotalCents(0);

        return orderRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<OrderEntity> getOrder(UUID id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<OrderEntity> listOrders(OrderDtos.OrderStatus status, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), size);
        if (status == null) {
            return orderRepository.findAll(pr);
        }
        return orderRepository.findByStatus(OrderMapper.fromDtoStatus(status), pr);
    }

    @Transactional
    public Optional<OrderEntity> cancelOrder(UUID id) {
        return orderRepository.findById(id).map(entity -> {
            if (entity.getStatus() == OrderStatus.PENDING_PAYMENT) {
                entity.setStatus(OrderStatus.CANCELLED);
                return orderRepository.save(entity);
            }
            return entity; // caller will validate state
        });
    }

    @Transactional
    public Optional<OrderEntity> markPaid(UUID id, UUID paymentIntentId) {
        return orderRepository.findById(id).map(entity -> {
            entity.setPaymentIntentId(paymentIntentId);
            entity.setStatus(OrderStatus.PAID);
            return orderRepository.save(entity);
        });
    }

    @Transactional
    public Optional<OrderEntity> markFailed(UUID id, UUID paymentIntentId) {
        return orderRepository.findById(id).map(entity -> {
            entity.setPaymentIntentId(paymentIntentId);
            entity.setStatus(OrderStatus.FAILED);
            return orderRepository.save(entity);
        });
    }
}


