package com.product.productservice.dto;

import java.math.BigDecimal;

public record CreateProductResponse(String id, String name, String skuCode, String description, BigDecimal price) {
}
