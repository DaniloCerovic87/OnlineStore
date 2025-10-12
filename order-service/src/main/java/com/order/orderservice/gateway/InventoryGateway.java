package com.order.orderservice.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.orderservice.client.InventoryClient;
import com.order.orderservice.dto.ReserveRequest;
import com.order.orderservice.exception.InventoryNotAvailableException;
import com.order.orderservice.exception.ResourceNotFoundException;
import com.order.orderservice.exception.ValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
public class InventoryGateway {

    private final InventoryClient api;

    @CircuitBreaker(
            name = "inventory",
            fallbackMethod = "reserveFallback"
    )
    @Retry(name = "inventory")
    public void reserve(ReserveRequest request) {
        try {
            api.reserve(request);
        } catch (HttpClientErrorException ex) {
            HttpStatusCode status = ex.getStatusCode();
            String body = ex.getResponseBodyAsString();
            String message = extractMessage(body);

            switch (status.value()) {
                case 400 -> throw new ValidationException(message);
                case 404 -> throw new ResourceNotFoundException(message);
                default -> throw ex;
            }
        }
    }

    private void reserveFallback(ReserveRequest request, Throwable ex) {
        if (ex instanceof ValidationException ve) throw ve;
        if (ex instanceof ResourceNotFoundException rnfe) throw rnfe;

        if (ex instanceof RuntimeException re) throw re;
        throw new InventoryNotAvailableException("Inventory service temporarily unavailable for SKU %s"
                .formatted(request.skuCode()), ex);
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode n = objectMapper.readTree(body);
            if (n.hasNonNull("debugMessage")) {
                return n.get("debugMessage").asText();
            }
            if (n.hasNonNull("message")) {
                return n.get("message").asText();
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}