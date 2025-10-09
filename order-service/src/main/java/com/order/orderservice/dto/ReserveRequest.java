package com.order.orderservice.dto;

public record ReserveRequest(
        String skuCode,
        Integer quantity,
        String orderNumber
) {}