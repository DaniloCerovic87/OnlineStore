package com.inventoryservice.inventoryservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryRowStatus {

    NEW("NEW"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String name;

}
