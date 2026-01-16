-- 1. Setup Databases
CREATE SCHEMA IF NOT EXISTS libdemo;
CREATE SCHEMA IF NOT EXISTS libbook;

-- ---------------------------------------------------------
-- LIBDEMO SCHEMA
-- ---------------------------------------------------------
USE libdemo;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       `id`           bigint NOT NULL AUTO_INCREMENT,
                       `email`        varchar(255) NOT NULL,
                       `password`     varchar(255) NOT NULL,
                       `full_name`    varchar(255) NOT NULL,
                       `created_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                       `phone_number` varchar(255) NOT NULL,
                       `role`         varchar(255) NOT NULL,
                       PRIMARY KEY (`id`),
                       UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB;

INSERT IGNORE INTO users (`id`, `email`, `password`, `full_name`, `phone_number`, `role`)
VALUES
  (100, 'alice@test.com', 'pw', 'Alice', '123', 'STUDENT'),
  (101, 'bob@test.com',   'pw', 'Bob',   '456', 'STUDENT');

-- ---------------------------------------------------------
-- LIBBOOK SCHEMA
-- ---------------------------------------------------------
USE libbook;

-- Books Table
DROP TABLE IF EXISTS books;
CREATE TABLE books (
                       `book_id`    varchar(64) NOT NULL,
                       `title`      varchar(300) NOT NULL,
                       `author`     varchar(200) DEFAULT NULL,
                       `status`     varchar(20) NOT NULL,
                       `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (`book_id`)
) ENGINE=InnoDB;

-- Loans Table (Cross-db FK to libdemo.users)
DROP TABLE IF EXISTS loans;
CREATE TABLE loans (
                       `loan_id`        bigint NOT NULL AUTO_INCREMENT,
                       `user_id`        bigint NOT NULL,
                       `book_id`        varchar(64) NOT NULL,
                       `state`          varchar(16) NOT NULL,
                       `checkout_date`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `due_date`       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `return_date`    timestamp NULL DEFAULT NULL,
                       `correlation_id` varchar(64) DEFAULT NULL,
                       `created_at`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       `updated_at`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (`loan_id`),
                       CONSTRAINT `fk_loans_book` FOREIGN KEY (`book_id`) REFERENCES books (`book_id`),
                       CONSTRAINT `fk_loans_user` FOREIGN KEY (`user_id`) REFERENCES libdemo.users (`id`)
) ENGINE=InnoDB;

-- Inventory Table
DROP TABLE IF EXISTS book_inventory;
CREATE TABLE book_inventory (
                                `inventory_id` bigint NOT NULL AUTO_INCREMENT,
                                `book_id`      varchar(64) NOT NULL,
                                `quantity`     int NOT NULL,
                                `status`       varchar(16) NOT NULL,
                                `reference_id` varchar(64) DEFAULT NULL,
                                `created_at`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`inventory_id`),
                                CONSTRAINT `fk_inventory_book` FOREIGN KEY (`book_id`) REFERENCES books (`book_id`)
) ENGINE=InnoDB;

-- Outbox Table
DROP TABLE IF EXISTS book_event_outbox;
CREATE TABLE book_event_outbox (
                                   `event_id`       bigint NOT NULL AUTO_INCREMENT,
                                   `event_type`     varchar(50) NOT NULL,
                                   `book_id`        varchar(64) NOT NULL,
                                   `user_id`        bigint DEFAULT NULL,
                                   `new_status`     varchar(20) DEFAULT NULL,
                                   `correlation_id` varchar(64) DEFAULT NULL,
                                   `status`         varchar(16) NOT NULL,
                                   `payload_json`   text NOT NULL,
                                   `created_at`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  -- `published_at`   timestamp NULL DEFAULT NULL,
                                   processed BOOLEAN DEFAULT FALSE, -- ADD THIS,
                                   PRIMARY KEY (`event_id`),
                                   CONSTRAINT `fk_outbox_book` FOREIGN KEY (`book_id`) REFERENCES books (`book_id`),
                                   CONSTRAINT `fk_outbox_user` FOREIGN KEY (`user_id`) REFERENCES libdemo.users (`id`)
) ENGINE=InnoDB;

-- Inventory Log
DROP TABLE IF EXISTS inventory_event_log;
CREATE TABLE inventory_event_log (
                                     `event_id`     bigint NOT NULL AUTO_INCREMENT,
                                     `inventory_id` bigint NOT NULL,
                                     `reference_id` varchar(100) NOT NULL,
                                     `event_type`   varchar(50) NOT NULL,
                                     `payload`      text NOT NULL,
                                     `status`       varchar(16) NOT NULL,
                                     `created_at`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`event_id`),
                                     UNIQUE KEY `reference_id` (`reference_id`),
                                     CONSTRAINT `fk_event_inventory` FOREIGN KEY (`inventory_id`) REFERENCES book_inventory (`inventory_id`)
) ENGINE=InnoDB;

-- Seed Data
    INSERT IGNORE INTO books (`book_id`, `title`, `author`, `status`)
VALUES
  ('b_100', '1984', 'George Orwell', 'AVAILABLE'),
  ('b_101', 'Hamlet', 'William Shakespeare', 'AVAILABLE');

INSERT IGNORE INTO loans (`user_id`, `book_id`, `state`, `checkout_date`, `due_date`)
VALUES
  (101, 'b_101', 'OPEN', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY));