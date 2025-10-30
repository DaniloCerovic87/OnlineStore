package com.inventoryservice.inventoryservice.messaging;

import com.inventoryservice.inventoryservice.dto.InventoryImportEvent;
import com.inventoryservice.inventoryservice.service.InventoryFileService;
import com.inventoryservice.inventoryservice.service.InventoryRowService;
import com.inventoryservice.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportKafkaConsumer {

    private final InventoryService inventoryService;
    private final InventoryRowService inventoryRowService;
    private final InventoryFileService inventoryFileService;

    @KafkaListener(topics = "inventory-import")
    public void onMessage(
            InventoryImportEvent event,
            @Header(name = "idemKey", required = false) String idemKey,
            Acknowledgment ack
    ) {
        log.debug("Consuming event: sku={} line={} fileId={} idemKey={}",
                event.skuCode(), event.lineNo(), event.fileId(), idemKey);

        if (inventoryRowService.isRowAlreadyProcessed(idemKey)) {
            log.warn("Duplicate event detected, skipping idemKey={}", idemKey);
            ack.acknowledge();
            return;
        }

        try {
            inventoryService.saveOrUpdateBySkuCode(event);
            inventoryRowService.markSuccess(idemKey);
            ack.acknowledge();
        } catch (Exception e) {
            inventoryRowService.markFailed(idemKey);
            log.error("Failed to process event for SKU={} fileId={} lineNo={}: {}",
                    event.skuCode(), event.fileId(), event.lineNo(), e.getMessage(), e);
        }
        inventoryFileService.checkAndMarkFileIfFinished(event.fileId());
    }
}
