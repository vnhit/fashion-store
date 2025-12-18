-- Vouchers table
CREATE TABLE IF NOT EXISTS vouchers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    description_vn VARCHAR(255),
    discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL DEFAULT 'PERCENTAGE',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) DEFAULT 0,
    max_discount_amount DECIMAL(10, 2) NULL,
    usage_limit INT DEFAULT NULL,
    used_count INT DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Voucher usage tracking
CREATE TABLE IF NOT EXISTS voucher_usage (
    id INT PRIMARY KEY AUTO_INCREMENT,
    voucher_id INT,
    user_id INT,
    order_id INT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert sample vouchers
INSERT INTO vouchers (code, description, description_vn, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, start_date, end_date, is_active) VALUES
('WELCOME10', 'Welcome discount 10%', 'Giảm giá chào mừng 10%', 'PERCENTAGE', 10, 200000, 50000, 100, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE),
('SAVE50K', 'Save 50,000 VND', 'Tiết kiệm 50,000 VNĐ', 'FIXED', 50000, 300000, NULL, 50, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), TRUE),
('SUMMER20', 'Summer sale 20%', 'Giảm giá mùa hè 20%', 'PERCENTAGE', 20, 500000, 200000, 200, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY), TRUE),
('VIP30', 'VIP discount 30%', 'Giảm giá VIP 30%', 'PERCENTAGE', 30, 1000000, 500000, 10, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY), TRUE);










