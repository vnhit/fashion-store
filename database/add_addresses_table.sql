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

-- Add address fields to users table (check if columns exist first)
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = 'default_address_id')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE users ADD COLUMN default_address_id INT'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = 'date_of_birth')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE users ADD COLUMN date_of_birth DATE'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = 'gender')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE users ADD COLUMN gender VARCHAR(10)'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add foreign key for default address (check if constraint exists first)
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (constraint_name = 'fk_default_address')
  ) > 0,
  'SELECT 1',
  'ALTER TABLE users ADD CONSTRAINT fk_default_address FOREIGN KEY (default_address_id) REFERENCES addresses(id) ON DELETE SET NULL'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Insert sample addresses for user 1
INSERT INTO addresses (user_id, full_name, phone, province, district, ward, street_address, is_default) VALUES
(1, 'Nguyễn Văn A', '0123456789', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '123 Đường ABC', TRUE),
(1, 'Nguyễn Văn A', '0123456789', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '456 Đường XYZ', FALSE);

