package com.inventoryservice.inventoryservice.dto;

public record ParsedRow(int lineNo, String sku, int qty) {}
