package com.order.orderservice.client;

import com.order.orderservice.dto.ReserveRequest;
import groovy.util.logging.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

@Slf4j
public interface InventoryClient {

    @PostExchange("/api/inventory/reserve")
    void reserve(@RequestBody ReserveRequest request);

}
