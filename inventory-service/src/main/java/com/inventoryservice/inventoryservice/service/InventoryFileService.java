package com.inventoryservice.inventoryservice.service;

import com.inventoryservice.inventoryservice.model.InventoryFile;
import com.inventoryservice.inventoryservice.model.enums.InventoryFileStatus;
import com.inventoryservice.inventoryservice.model.enums.InventoryRowStatus;
import com.inventoryservice.inventoryservice.repository.InventoryFileRepository;
import com.inventoryservice.inventoryservice.repository.InventoryRowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryFileService {

    private final InventoryFileRepository fileRepository;
    private final InventoryRowRepository rowRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long create(String fileName, String fileHash, String uploadedBy) {
        InventoryFile file = InventoryFile.builder()
                .fileName(fileName)
                .fileHash(fileHash)
                .uploadedBy(uploadedBy)
                .status(InventoryFileStatus.IN_PROGRESS)
                .createdAt(Instant.now())
                .build();

        fileRepository.save(file);
        return file.getId();
    }

    @Transactional(readOnly = true)
    public boolean existsByHashAndStatusFinished(String fileHash) {
        return fileRepository.existsByFileHashAndStatus(fileHash, InventoryFileStatus.FINISHED);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long fileId) {
        setStatus(fileId, InventoryFileStatus.FAILED);
    }

    public void markFinished(Long fileId) {
        setStatus(fileId, InventoryFileStatus.FINISHED);
    }

    private void setStatus(Long fileId, InventoryFileStatus status) {
        InventoryFile file = fileRepository.getReferenceById(fileId);
        file.setStatus(status);
        fileRepository.save(file);
    }

    @Transactional
    public void checkAndMarkFileIfFinished(Long fileId) {
        boolean hasUnprocessed = rowRepository.existsByInventoryFileIdAndStatus(fileId, InventoryRowStatus.NEW);
        if (!hasUnprocessed) {
           markFinished(fileId);
            log.info("File {} marked as FINISHED (no remaining NEW rows)", fileId);
        }
    }

}
