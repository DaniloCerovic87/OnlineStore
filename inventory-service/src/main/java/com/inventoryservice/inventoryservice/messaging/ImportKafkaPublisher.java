package com.inventoryservice.inventoryservice.messaging;

import com.inventoryservice.inventoryservice.dto.InventoryImportEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportKafkaPublisher {

    private final KafkaTemplate<String, InventoryImportEvent> kafkaTemplate;

    private static final String TOPIC = "inventory-import";

    public void publish(InventoryImportEvent event, String idemKey) {
        Message<InventoryImportEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader("idemKey", idemKey)
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event for SKU={} (fileId={}, lineNo={}): {}",
                                event.skuCode(), event.fileId(), event.lineNo(), ex.getMessage());
                    } else {
                        log.debug("Published to {} | offset={} | idemKey={}",
                                TOPIC,
                                result.getRecordMetadata().offset(),
                                idemKey);
                    }
                });
    }
}
