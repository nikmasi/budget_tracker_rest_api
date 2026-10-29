CREATE DATABASE IF NOT EXISTS budget_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE budget_tracker;

DROP TABLE IF EXISTS transaction; 
DROP TABLE IF EXISTS budget;
DROP TABLE IF EXISTS category;

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    color VARCHAR(7) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uc_category_name UNIQUE (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE budget (
    id BIGINT NOT NULL AUTO_INCREMENT,
    month VARCHAR(7) NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT chk_budget_amount
        CHECK (amount > 0 AND amount <= 1000000.00),

    CONSTRAINT uc_category_month
        UNIQUE (category_id, month),

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
        REFERENCES category(id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `transaction` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(200) NULL,
    category_id BIGINT NOT NULL,

    PRIMARY KEY (id),

	CONSTRAINT chk_transaction_type
    CHECK (type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0 AND amount <= 1000000.00),

     CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
        REFERENCES category(id)
        ON DELETE RESTRICT,

    INDEX idx_transaction_date (date DESC),
    INDEX idx_transaction_type (type),
    INDEX idx_transaction_category (category_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

