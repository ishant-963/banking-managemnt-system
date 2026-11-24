-- Java Bank schema. Run this once in MySQL before starting the program:
--   mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS javabank_db;
USE javabank_db;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(10)  NOT NULL UNIQUE,
    email       VARCHAR(80)  NOT NULL,
    address     VARCHAR(120) NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    acc_no      INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT         NOT NULL,
    acc_type    VARCHAR(10) NOT NULL,
    balance     DOUBLE      NOT NULL DEFAULT 0,
    pin_hash    VARCHAR(64) NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    opened_on   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) AUTO_INCREMENT = 100001;

CREATE TABLE IF NOT EXISTS transactions (
    txn_id        INT AUTO_INCREMENT PRIMARY KEY,
    acc_no        INT          NOT NULL,
    txn_type      VARCHAR(20)  NOT NULL,
    amount        DOUBLE       NOT NULL,
    balance_after DOUBLE       NOT NULL,
    description   VARCHAR(100),
    txn_time      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (acc_no) REFERENCES accounts(acc_no)
);

CREATE TABLE IF NOT EXISTS loans (
    loan_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT         NOT NULL,
    acc_no      INT         NOT NULL,
    principal   DOUBLE      NOT NULL,
    annual_rate DOUBLE      NOT NULL,
    months      INT         NOT NULL,
    emi         DOUBLE      NOT NULL,
    remaining   DOUBLE      NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    taken_on    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (acc_no) REFERENCES accounts(acc_no)
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(30) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL
);

-- default admin login -> username: admin   password: admin123
INSERT IGNORE INTO admins (username, password_hash)
VALUES ('admin', 'admin123');
