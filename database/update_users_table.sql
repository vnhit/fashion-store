-- Update users table to add password column
USE fashionstore;

-- Add password column if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Update existing users with default password
UPDATE users SET password = '123456' WHERE password IS NULL OR password = '';

-- Add sample users if they don't exist
INSERT INTO users (name, email, phone, password, membership_level, points) 
VALUES 
('Nguyễn Văn A', 'nguyenvana@example.com', '0123456789', '123456', 'Gold', 750),
('Admin', 'admin@fashionstore.com', '0987654321', 'admin123', 'Diamond', 2000)
ON DUPLICATE KEY UPDATE password = VALUES(password);




























