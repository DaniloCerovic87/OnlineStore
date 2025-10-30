package com.inventoryservice.inventoryservice.service;

import com.inventoryservice.inventoryservice.dto.InventoryImportEvent;
import com.inventoryservice.inventoryservice.dto.ParsedRow;
import com.inventoryservice.inventoryservice.dto.ReserveRequest;
import com.inventoryservice.inventoryservice.exception.OutOfStockException;
import com.inventoryservice.inventoryservice.exception.ResourceNotFoundException;
import com.inventoryservice.inventoryservice.messaging.ImportKafkaPublisher;
import com.inventoryservice.inventoryservice.model.Inventory;
import com.inventoryservice.inventoryservice.model.ReservationKey;
import com.inventoryservice.inventoryservice.parser.RowParser;
import com.inventoryservice.inventoryservice.repository.InventoryRepository;
import com.inventoryservice.inventoryservice.repository.ReservationKeyRepository;
import com.inventoryservice.inventoryservice.util.HashUtil;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryFileService fileService;
    private final InventoryRowService rowService;
    private final InventoryRepository inventoryRepository;
    private final ReservationKeyRepository reservationKeyRepository;
    private final TransactionTemplate txTemplate;
    private final ImportKafkaPublisher publisher;
    private final RowParser parser;

    public void reserve(ReserveRequest request) {
        int attempts = 0;
        while (true) {
            try {
                txTemplate.executeWithoutResult(status -> {
                    try {
                        reservationKeyRepository.save(
                                ReservationKey.builder().orderNumber(request.orderNumber()).build()
                        );
                    } catch (DataIntegrityViolationException dup) {
                        log.info("Duplicate reservation ignored for order {}", request.orderNumber());
                        return;
                    }

                    Inventory inv = inventoryRepository.findBySkuCode(request.skuCode())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "SKU not found: " + request.skuCode()));

                    if (inv.getQuantity() < request.quantity()) {
                        throw new OutOfStockException(
                                "Insufficient stock for " + request.skuCode());
                    }

                    inv.setQuantity(inv.getQuantity() - request.quantity());
                });
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                log.warn("Optimistic conflict on reserve sku={}, attempt={}", request.skuCode(), attempts);
                if (++attempts >= 5) throw e;
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

    @Transactional
    public void importProducts(MultipartFile file, String userId) {
        final String originalName = file.getOriginalFilename();
        final String fileHash = HashUtil.sha256(file);

        if (fileService.existsByHashAndStatusFinished(fileHash)) {
            log.info("Skip import: duplicate file content detected [{} | {}]", originalName, fileHash);
            return;
        }

        Long fileId = fileService.create(originalName, fileHash, userId);

        try (InputStream is = file.getInputStream()) {
            List<ParsedRow> rows = (List<ParsedRow>) parser.parse(is, originalName);
            rowService.saveAllImported(rows, fileId, fileHash);

            for (ParsedRow row : rows) {
                final String idemKey = fileHash + "#" + row.lineNo();

                InventoryImportEvent event = new InventoryImportEvent(fileId, row.lineNo(), row.sku(), row.qty());

                publisher.publish(event, idemKey);
            }

            log.info("Import queued: fileId={}, file={}", fileId, originalName);

        } catch (Exception e) {
            fileService.markFailed(fileId);
            log.error("Import failed for file {} (fileId={}): {}", originalName, fileId, e.getMessage(), e);
            throw new IllegalStateException("Import failed: " + e.getMessage(), e);
        }
    }

    public void saveOrUpdateBySkuCode(InventoryImportEvent event) {
        Inventory inventory = inventoryRepository.findBySkuCode(event.skuCode()).orElse(null);
        if (inventory == null) {
            Inventory newInventory = Inventory.builder()
                    .skuCode(event.skuCode())
                    .quantity(event.quantity()).build();
            inventoryRepository.save(newInventory);
        } else {
            inventory.setQuantity(inventory.getQuantity() + event.quantity());
            inventoryRepository.save(inventory);
        }
    }

}
