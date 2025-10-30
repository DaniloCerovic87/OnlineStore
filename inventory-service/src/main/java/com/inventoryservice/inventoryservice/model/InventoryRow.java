package com.inventoryservice.inventoryservice.model;

import com.inventoryservice.inventoryservice.model.enums.InventoryRowStatus;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer lineNo;
    private String skuCode;
    private Integer quantity;

    private String message;

    @Enumerated(EnumType.STRING)
    private InventoryRowStatus status;

    @ManyToOne
    @JoinColumn(name = "inventory_file_id")
    @NotNull
    private InventoryFile inventoryFile;

    @Column(unique = true)
    private String idemKey; // fileHash#lineNo
}
