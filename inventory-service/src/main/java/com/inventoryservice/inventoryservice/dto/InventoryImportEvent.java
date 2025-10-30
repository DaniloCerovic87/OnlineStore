package com.inventoryservice.inventoryservice.dto;

public record InventoryImportEvent(
        Long fileId,
        Integer lineNo,
        String skuCode,
        Integer quantity
){}