package com.acmecorp.retailhub.orders.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDtos {
    public record Address(
            @Size(max = 200) String line1,
            @Size(max = 200) String line2,
            @Size(max = 100) String city,
            @Size(max = 100) String state,
            @Size(max = 20) String postalCode,
            @Size(max = 2) String country
    ) {}

    public record OrderCreate(
            @Valid Address shippingAddress,
            @Size(max = 1000) String notes
    ) {}

    public enum OrderStatus { pending_payment, paid, cancelled, failed }

    public record OrderItem(
            @NotNull UUID productId,
            @Size(max = 200) String name,
            @Min(1) int qty,
            @Min(0) int unitPriceCents,
            @Min(0) int lineTotalCents
    ) {}

    public record Order(
            @NotNull UUID id,
            @NotNull OrderStatus status,
            @Pattern(regexp = "^[A-Z]{3}$") String currency,
            @NotNull List<@Valid OrderItem> items,
            @Min(0) int subtotalCents,
            @Min(0) int totalCents,
            @Valid Address shippingAddress,
            @NotNull OffsetDateTime createdAt,
            @NotNull OffsetDateTime updatedAt,
            UUID paymentIntentId
    ) {}

    public record PaymentCallback(
            @NotNull UUID orderId,
            @NotNull UUID paymentIntentId,
            @NotNull @Pattern(regexp = "authorized|captured|failed") String status,
            String failureReason
    ) {}

    public record Problem(
            @NotBlank String title,
            @NotNull Integer status,
            String detail,
            @Pattern(regexp = "^https?://.*|about:blank") String type
    ) {}
}


