package com.order.orderservice.event.domain;

public record OrderCreatedEvent(
        Long orderId,
        String skuCode,
        Integer quantity,
        String orderNumber) {
}
