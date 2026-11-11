USE literary_emporium;

ALTER TABLE orders ADD COLUMN order_code VARCHAR(100) UNIQUE AFTER id;
ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50) DEFAULT 'Pay Later' AFTER status;
ALTER TABLE orders ADD COLUMN payment_status VARCHAR(50) DEFAULT 'PENDING' AFTER payment_method;
ALTER TABLE orders ADD COLUMN payment_id VARCHAR(100) AFTER payment_status;
ALTER TABLE orders ADD COLUMN customer_name VARCHAR(100) AFTER payment_id;
ALTER TABLE orders ADD COLUMN customer_email VARCHAR(100) AFTER customer_name;
ALTER TABLE orders ADD COLUMN customer_phone VARCHAR(30) AFTER customer_email;
ALTER TABLE orders ADD COLUMN delivery_address TEXT AFTER customer_phone;

CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL UNIQUE,
    order_id INT NULL,
    order_code VARCHAR(100) NULL,
    user_id INT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    provider VARCHAR(50) DEFAULT 'UPI_QR_SANDBOX',
    upi_string TEXT,
    qr_code_url TEXT,
    provider_reference VARCHAR(100) NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_id (payment_id),
    INDEX idx_order_code (order_code)
);

UPDATE orders SET order_code = CONCAT('ORD-', id) WHERE order_code IS NULL;
UPDATE orders SET payment_method = 'Pay Later', payment_status = 'PENDING' WHERE payment_method IS NULL;
