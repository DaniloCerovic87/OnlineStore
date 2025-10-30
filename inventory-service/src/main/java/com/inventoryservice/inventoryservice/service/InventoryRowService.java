package com.inventoryservice.inventoryservice.service;

import com.inventoryservice.inventoryservice.dto.ParsedRow;
import com.inventoryservice.inventoryservice.model.InventoryFile;
import com.inventoryservice.inventoryservice.model.InventoryRow;
import com.inventoryservice.inventoryservice.model.enums.InventoryRowStatus;
import com.inventoryservice.inventoryservice.repository.InventoryFileRepository;
import com.inventoryservice.inventoryservice.repository.InventoryRowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryRowService {

    private final InventoryRowRepository rowRepository;
    private final InventoryFileRepository fileRepository;

    @Transactional(readOnly = true)
    public boolean isRowAlreadyProcessed(String idemKey) {
        return rowRepository.existsByIdemKeyAndStatusIn(idemKey, List.of(InventoryRowStatus.SUCCESS, InventoryRowStatus.FAILED));
    }

    public void saveAllImported(List<ParsedRow> rows, Long fileId, String fileHash) {
        InventoryFile file = fileRepository.getReferenceById(fileId);
        List<InventoryRow> inventoryRows = rows.stream()
                .map(r -> InventoryRow.builder()
                .lineNo(r.lineNo())
                .skuCode(r.sku())
                .quantity(r.qty())
                .status(InventoryRowStatus.NEW)
                .inventoryFile(file)
                .idemKey(fileHash + "#" + r.lineNo()).build()).toList();
        rowRepository.saveAll(inventoryRows);
    }

    public void markFailed(String idemKey) {
        setStatus(idemKey, InventoryRowStatus.FAILED);
    }

    public void markSuccess(String idemKey) {
        setStatus(idemKey, InventoryRowStatus.SUCCESS);
    }

    private void setStatus(String idemKey, InventoryRowStatus status) {
        InventoryRow row = rowRepository.findByIdemKey(idemKey);
        row.setStatus(status);
        rowRepository.save(row);
    }
}
