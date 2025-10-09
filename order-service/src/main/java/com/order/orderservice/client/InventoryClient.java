package com.order.orderservice.client;

import com.order.orderservice.dto.ReserveRequest;
import com.order.orderservice.exception.InventoryNotAvailableException;
import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

@Slf4j
public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @PostExchange("/api/inventory/reserve")
    @CircuitBreaker(name = "inventory", fallbackMethod = "reserveFallback")
    @Retry(
            name = "inventory"
    )
    void reserve(@RequestBody ReserveRequest request);

    default void reserveFallback(ReserveRequest request, Throwable ex) {
        log.info("Cannot make reservation on inventory for sku code {}, quantity {}, failure reason {}", request.skuCode(), request.quantity(), ex.getMessage());
        throw new InventoryNotAvailableException("Inventory service temporarily unavailable for SKU %s", request.skuCode());
    }

}
