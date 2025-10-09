package com.inventoryservice.inventoryservice.controller;

import com.inventoryservice.inventoryservice.dto.ReserveRequest;
import com.inventoryservice.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@RequestBody ReserveRequest request) {
        inventoryService.reserve(request);
        return ResponseEntity.ok().build();
    }

}
