CREATE TABLE inventory_file (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                file_name VARCHAR(255) NOT NULL,
                                file_hash VARCHAR(255) UNIQUE,
                                uploaded_by VARCHAR(255),
                                status ENUM('IN_PROGRESS', 'FAILED', 'FINISHED'),
                                PRIMARY KEY (id)
)
