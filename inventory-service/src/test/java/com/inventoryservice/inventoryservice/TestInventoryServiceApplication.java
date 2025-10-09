package com.inventoryservice.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
public class TestInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(InventoryServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
