package com.order.orderservice.service;

import com.order.orderservice.dto.CreateOrderRequest;
import com.order.orderservice.dto.CreateOrderResponse;
import com.order.orderservice.dto.OrderStatusResponse;
import com.order.orderservice.dto.ReserveRequest;
import com.order.orderservice.event.domain.OrderCreatedEvent;
import com.order.orderservice.event.kafka.OrderPlacedEvent;
import com.order.orderservice.event.kafka.OutboxEvent;
import com.order.orderservice.exception.ResourceNotFoundException;
import com.order.orderservice.model.Order;
import com.order.orderservice.model.OrderStatus;
import com.order.orderservice.repository.OrderRepository;
import com.order.orderservice.repository.OutboxEventRepository;
import com.order.orderservice.util.AvroJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateOrderResponse placeOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(request.price());
        order.setSkuCode(request.skuCode());
        order.setQuantity(request.quantity());
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), request.skuCode(), request.quantity(), order.getOrderNumber()));

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
        return new CreateOrderResponse(order.getId(), order.getOrderNumber(), order.getSkuCode(), order.getPrice(), order.getQuantity(), order.getStatus().toString());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("No order found for id: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }


    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with number: " + orderNumber));

        return new OrderStatusResponse(order.getOrderNumber(), order.getStatus().toString());
    }
}
