CREATE TABLE inventory_row (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               line_no INT,
                               sku_code VARCHAR(255),
                               quantity INT,
                               message VARCHAR(500),
                               status ENUM('NEW', 'SUCCESS', 'FAILED'),
                               inventory_file_id BIGINT NOT NULL,
                               idem_key VARCHAR(255) UNIQUE,
                               PRIMARY KEY (id),
                               CONSTRAINT fk_inventory_row_file
                                   FOREIGN KEY (inventory_file_id)
                                       REFERENCES inventory_file(id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE
)
