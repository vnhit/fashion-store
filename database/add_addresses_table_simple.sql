-- Add addresses table for shipping addresses
CREATE TABLE IF NOT EXISTS addresses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    street_address VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add address fields to users table
-- Note: Run these one by one, ignore error if column already exists
ALTER TABLE users ADD COLUMN default_address_id INT;
ALTER TABLE users ADD COLUMN date_of_birth DATE;
ALTER TABLE users ADD COLUMN gender VARCHAR(10);

-- Add foreign key for default address
-- Note: Ignore error if constraint already exists
ALTER TABLE users ADD CONSTRAINT fk_default_address 
FOREIGN KEY (default_address_id) REFERENCES addresses(id) ON DELETE SET NULL;

-- Insert sample addresses for user 1
INSERT INTO addresses (user_id, full_name, phone, province, district, ward, street_address, is_default) VALUES
(1, 'Nguyễn Văn A', '0123456789', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '123 Đường ABC', TRUE),
(1, 'Nguyễn Văn A', '0123456789', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '456 Đường XYZ', FALSE)
ON DUPLICATE KEY UPDATE id=id;

























