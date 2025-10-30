package com.inventoryservice.inventoryservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryFileStatus {

    IN_PROGRESS("IN_PROGRESS"),
    FAILED("FAILED"),
    FINISHED("FINISHED");

    private final String name;

}
