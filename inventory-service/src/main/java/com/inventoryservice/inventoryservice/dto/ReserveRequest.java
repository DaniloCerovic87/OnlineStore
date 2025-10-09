package com.inventoryservice.inventoryservice.dto;

public record ReserveRequest(
        String skuCode,
        Integer quantity,
        String orderNumber
) {}