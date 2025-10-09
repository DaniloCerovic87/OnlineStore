package com.order.orderservice.exception;

public class InventoryNotAvailableException extends RuntimeException {

    public InventoryNotAvailableException(String message, String code) {
        super(message);
    }

}
