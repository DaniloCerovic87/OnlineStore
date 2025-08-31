package com.order.orderservice.service;

import com.order.orderservice.client.InventoryClient;
import com.order.orderservice.dto.CreateOrderRequest;
import com.order.orderservice.dto.CreateOrderResponse;
import com.order.orderservice.event.OrderPlacedEvent;
import com.order.orderservice.model.Order;
import com.order.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public CreateOrderResponse placeOrder(CreateOrderRequest request) {
        var isProductInStock = inventoryClient.isInStock(request.skuCode(), request.quantity());

        if (!isProductInStock) {
            throw new RuntimeException("Product with SkuCode " + request.skuCode() + " is not in stock");
        }

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(request.price());
        order.setSkuCode(request.skuCode());
        order.setQuantity(request.quantity());
        orderRepository.save(order);

        // Send the message to Kafka Topic
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
        orderPlacedEvent.setOrderNumber(order.getOrderNumber());
        orderPlacedEvent.setEmail(request.userDetails().email());
        orderPlacedEvent.setFirstName(request.userDetails().firstName());
        orderPlacedEvent.setLastName(request.userDetails().lastName());
        log.info("Start - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
        kafkaTemplate.send("order-placed", orderPlacedEvent);
        log.info("End - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);

        return new CreateOrderResponse(order.getId(), order.getSkuCode(), order.getPrice(), order.getQuantity());
    }

}
