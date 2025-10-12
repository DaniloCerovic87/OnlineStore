package com.order.orderservice.event.domain;

import com.order.orderservice.dto.ReserveRequest;
import com.order.orderservice.gateway.InventoryGateway;
import com.order.orderservice.model.OrderStatus;
import com.order.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final InventoryGateway inventoryGateway;
    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {
        try {
            inventoryGateway.reserve(new ReserveRequest(e.skuCode(), e.quantity(), e.orderNumber()));
            orderService.updateOrderStatus(e.orderId(), OrderStatus.CONFIRMED);
        } catch (Exception ex) {
            orderService.updateOrderStatus(e.orderId(), OrderStatus.REJECTED);
        }
    }
}
