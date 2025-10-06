package com.order.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.orderservice.client.InventoryClient;
import com.order.orderservice.dto.CreateOrderRequest;
import com.order.orderservice.dto.CreateOrderResponse;
import com.order.orderservice.event.OrderPlacedEvent;
import com.order.orderservice.event.OutboxEvent;
import com.order.orderservice.exception.OutOfStockException;
import com.order.orderservice.model.Order;
import com.order.orderservice.repository.OrderRepository;
import com.order.orderservice.repository.OutboxEventRepository;
import com.order.orderservice.util.AvroJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final InventoryClient inventoryClient;

    @Transactional
    public CreateOrderResponse placeOrder(CreateOrderRequest request) {
        var isProductInStock = inventoryClient.isInStock(request.skuCode(), request.quantity());

        if (!isProductInStock) {
            throw new OutOfStockException(
                    "Product with SkuCode %s is not in stock (requested amount=%d)"
                            .formatted(request.skuCode(), request.quantity())
            );
        }

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(request.price());
        order.setSkuCode(request.skuCode());
        order.setQuantity(request.quantity());
        orderRepository.save(order);

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderNumber(order.getOrderNumber());
        orderPlacedEvent.setEmail(request.userDetails().email());
        orderPlacedEvent.setFirstName(request.userDetails().firstName());
        orderPlacedEvent.setLastName(request.userDetails().lastName());

        OutboxEvent evt = new OutboxEvent();
        evt.setAggregateType("order");
        evt.setAggregateId(order.getOrderNumber());
        evt.setEventType("OrderPlaced");

        evt.setPayload(AvroJsonUtil.toJson(orderPlacedEvent));
        outboxEventRepository.save(evt);
        return new CreateOrderResponse(order.getId(), order.getOrderNumber(), order.getSkuCode(), order.getPrice(), order.getQuantity());
    }

}
