package com.inventoryservice.inventoryservice.service;

import com.inventoryservice.inventoryservice.dto.ReserveRequest;
import com.inventoryservice.inventoryservice.exception.OutOfStockException;
import com.inventoryservice.inventoryservice.exception.ResourceNotFoundException;
import com.inventoryservice.inventoryservice.model.Inventory;
import com.inventoryservice.inventoryservice.model.ReservationKey;
import com.inventoryservice.inventoryservice.repository.InventoryRepository;
import com.inventoryservice.inventoryservice.repository.ReservationKeyRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationKeyRepository reservationKeyRepository;

    @Transactional
    public void reserve(ReserveRequest request) {
        int attempts = 0;
        while (true) {
            try {
                try {
                    reservationKeyRepository.save(ReservationKey.builder().orderNumber(request.orderNumber()).build());
                } catch (DataIntegrityViolationException e) {
                    log.info("Duplicate reservation ignored for order {}", request.orderNumber());
                    return;
                }

                Inventory inv = inventoryRepository.findBySkuCode(request.skuCode())
                        .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + request.skuCode()));
                if (inv.getQuantity() < request.quantity()) {
                    throw new OutOfStockException("Insufficient stock for " + request.skuCode());
                }
                inv.setQuantity(inv.getQuantity() - request.quantity());
                return;
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                if (++attempts > 5) {
                    throw e;
                }
                backoff(attempts);
            }
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(30L * attempt);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
