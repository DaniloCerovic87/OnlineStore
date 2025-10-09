package com.inventoryservice.inventoryservice.repository;

import com.inventoryservice.inventoryservice.model.ReservationKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationKeyRepository extends JpaRepository<ReservationKey, String> {}