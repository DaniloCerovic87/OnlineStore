package com.order.orderservice.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(String skuCode, BigDecimal price, Integer quantity, UserDetails userDetails) {

    public record UserDetails(String email, String firstName, String lastName) {}


}


