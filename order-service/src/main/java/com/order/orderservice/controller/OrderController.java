package com.order.orderservice.controller;

import com.order.orderservice.dto.CreateOrderRequest;
import com.order.orderservice.dto.CreateOrderResponse;
import com.order.orderservice.dto.OrderStatusResponse;
import com.order.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> placeOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.placeOrder(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{orderNumber}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderNumber));
    }

}
