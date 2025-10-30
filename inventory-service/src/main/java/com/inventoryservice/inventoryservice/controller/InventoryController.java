package com.inventoryservice.inventoryservice.controller;

import com.inventoryservice.inventoryservice.dto.ReserveRequest;
import com.inventoryservice.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@RequestBody ReserveRequest request) {
        inventoryService.reserve(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importProducts(@RequestPart("file") MultipartFile file,
                                                          @RequestHeader("X-User-Id") String userId) {
        log.info("importProducts started({})", file.getOriginalFilename());
        inventoryService.importProducts(file, userId);
        return ResponseEntity.ok().build();
    }


}
