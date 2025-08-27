package com.acmecorp.retailhub.orders.api;

import com.acmecorp.retailhub.orders.api.dto.OrderDtos;
import com.acmecorp.retailhub.orders.domain.OrderEntity;
import com.acmecorp.retailhub.orders.domain.OrderStatus;
import com.acmecorp.retailhub.orders.service.OrderMapper;
import com.acmecorp.retailhub.orders.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
@Validated
public class OrdersController {
    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDtos.Order> create(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                  @RequestHeader(value = "X-Request-Id", required = false) String xRequestId,
                                                  @RequestBody(required = false) OrderDtos.OrderCreate body) {
        OrderEntity created = orderService.createOrder(body);
        OrderDtos.Order dto = OrderMapper.toDto(created);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/" + created.getId()));
        if (xRequestId != null) headers.add("X-Request-Id", xRequestId);
        return new ResponseEntity<>(dto, headers, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Object> list(@RequestParam(value = "status", required = false) OrderDtos.OrderStatus status,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "size", defaultValue = "20") int size,
                                       @RequestHeader(value = "X-Request-Id", required = false) String xRequestId) {
        Page<OrderEntity> orders = orderService.listOrders(status, page, size);
        List<OrderDtos.Order> items = orders.getContent().stream().map(OrderMapper::toDto).toList();
        return ResponseEntity.ok().body(new java.util.LinkedHashMap<>() {{
            {
                put("items", items);
                put("page", page);
                put("size", size);
                put("total", (int) orders.getTotalElements());
            }
        }});
    }

    @GetMapping("/{orderId}")
    public OrderDtos.Order get(@PathVariable("orderId") UUID orderId,
                               @RequestHeader(value = "X-Request-Id", required = false) String xRequestId) {
        return orderService.getOrder(orderId)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @PostMapping("/{orderId}/cancel")
    public OrderDtos.Order cancel(@PathVariable("orderId") UUID orderId,
                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                  @RequestHeader(value = "X-Request-Id", required = false) String xRequestId) {
        OrderEntity entity = orderService.getOrder(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (entity.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException("Cannot cancel (already paid or shipped)");
        }
        OrderEntity updated = orderService.cancelOrder(orderId).orElseThrow();
        return OrderMapper.toDto(updated);
    }

    @PostMapping("/_internal/payments/callback")
    public ResponseEntity<Void> paymentCallback(@RequestHeader("X-Payments-Signature") String signature,
                                                @RequestBody OrderDtos.PaymentCallback body) {
        // Signature verification would happen here (omitted for brevity)
        if ("authorized".equals(body.status())) {
            orderService.markPaid(body.orderId(), body.paymentIntentId());
        } else if ("captured".equals(body.status())) {
            orderService.markPaid(body.orderId(), body.paymentIntentId());
        } else if ("failed".equals(body.status())) {
            orderService.markFailed(body.orderId(), body.paymentIntentId());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/healthz")
    public ResponseEntity<Void> healthz() { return ResponseEntity.ok().build(); }

    @GetMapping("/readyz")
    public ResponseEntity<Void> readyz() { return ResponseEntity.ok().build(); }
}


