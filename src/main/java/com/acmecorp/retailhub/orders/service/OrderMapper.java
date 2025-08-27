package com.acmecorp.retailhub.orders.service;

import com.acmecorp.retailhub.orders.api.dto.OrderDtos;
import com.acmecorp.retailhub.orders.domain.Address;
import com.acmecorp.retailhub.orders.domain.OrderEntity;
import com.acmecorp.retailhub.orders.domain.OrderItem;
import com.acmecorp.retailhub.orders.domain.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderMapper {
    public static OrderEntity toEntityFromCreate(OrderDtos.OrderCreate create) {
        OrderEntity entity = new OrderEntity();
        if (create != null && create.shippingAddress() != null) {
            entity.setShippingAddress(toAddress(create.shippingAddress()));
        }
        return entity;
    }

    public static OrderDtos.Order toDto(OrderEntity entity) {
        List<OrderDtos.OrderItem> items = new ArrayList<>();
        for (OrderItem item : entity.getItems()) {
            items.add(new OrderDtos.OrderItem(
                    item.getProductId(),
                    item.getName(),
                    item.getQty(),
                    item.getUnitPriceCents(),
                    item.getLineTotalCents()
            ));
        }
        OrderDtos.Address addr = null;
        if (entity.getShippingAddress() != null) {
            Address a = entity.getShippingAddress();
            addr = new OrderDtos.Address(a.getLine1(), a.getLine2(), a.getCity(), a.getState(), a.getPostalCode(), a.getCountry());
        }
        return new OrderDtos.Order(
                entity.getId(),
                switch (entity.getStatus()) {
                    case PENDING_PAYMENT -> OrderDtos.OrderStatus.pending_payment;
                    case PAID -> OrderDtos.OrderStatus.paid;
                    case CANCELLED -> OrderDtos.OrderStatus.cancelled;
                    case FAILED -> OrderDtos.OrderStatus.failed;
                },
                entity.getCurrency(),
                items,
                entity.getSubtotalCents(),
                entity.getTotalCents(),
                addr,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getPaymentIntentId()
        );
    }

    public static OrderStatus fromDtoStatus(OrderDtos.OrderStatus status) {
        return switch (status) {
            case pending_payment -> OrderStatus.PENDING_PAYMENT;
            case paid -> OrderStatus.PAID;
            case cancelled -> OrderStatus.CANCELLED;
            case failed -> OrderStatus.FAILED;
        };
    }

    public static Address toAddress(OrderDtos.Address dto) {
        Address a = new Address();
        a.setLine1(dto.line1());
        a.setLine2(dto.line2());
        a.setCity(dto.city());
        a.setState(dto.state());
        a.setPostalCode(dto.postalCode());
        a.setCountry(dto.country());
        return a;
    }
}


