package com.inventoryservice.inventoryservice.repository;

import com.inventoryservice.inventoryservice.model.InventoryFile;
import com.inventoryservice.inventoryservice.model.enums.InventoryFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryFileRepository extends JpaRepository<InventoryFile, Long> {

    boolean existsByFileHashAndStatus(String fileHash, InventoryFileStatus status);

}
