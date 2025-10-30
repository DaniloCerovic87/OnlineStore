package com.inventoryservice.inventoryservice.repository;

import com.inventoryservice.inventoryservice.model.InventoryRow;
import com.inventoryservice.inventoryservice.model.enums.InventoryRowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface InventoryRowRepository extends JpaRepository<InventoryRow, Long> {

    boolean existsByIdemKeyAndStatusIn(String idemKey, Collection<InventoryRowStatus> statuses);

    boolean existsByInventoryFileIdAndStatus(Long fileId, InventoryRowStatus status);

    InventoryRow findByIdemKey(String idemKey);
}
