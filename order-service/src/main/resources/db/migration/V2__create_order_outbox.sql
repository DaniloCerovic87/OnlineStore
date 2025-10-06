CREATE TABLE order_outbox (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              aggregate_type VARCHAR(50) NOT NULL,
                              aggregate_id   VARCHAR(100) NOT NULL,
                              event_type     VARCHAR(100) NOT NULL,
                              payload        JSON NOT NULL,
                              status         ENUM('NEW','SENT','FAILED') NOT NULL DEFAULT 'NEW',
                              attempts       INT NOT NULL DEFAULT 0,
                              created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              last_attempt_at TIMESTAMP NULL,
                              sent_at        TIMESTAMP NULL,
                              KEY idx_status_created (status, created_at)
);
