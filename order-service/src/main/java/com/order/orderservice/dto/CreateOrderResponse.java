package com.order.orderservice.dto;

import java.math.BigDecimal;

public record CreateOrderResponse(Long id, String orderNumber, String skuCode, BigDecimal price, Integer quantity,
                                  String status) {
}
