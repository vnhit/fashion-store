-- FashionStore Database Schema
CREATE DATABASE IF NOT EXISTS fashionstore;
USE fashionstore;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    name_vn VARCHAR(100) NOT NULL,
    icon_path VARCHAR(255)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    name_vn VARCHAR(255) NOT NULL,
    description TEXT,
    description_vn TEXT,
    brand VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    discount_percent INT DEFAULT 0,
    category_id INT,
    image_path VARCHAR(255),
    rating DECIMAL(3, 1) DEFAULT 0,
    review_count INT DEFAULT 0,
    badge VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Product colors
CREATE TABLE IF NOT EXISTS product_colors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    color_name VARCHAR(50),
    color_code VARCHAR(7),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Product sizes
CREATE TABLE IF NOT EXISTS product_sizes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT,
    size VARCHAR(10),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255),
    avatar_path VARCHAR(255),
    membership_level VARCHAR(50) DEFAULT 'Silver',
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart table
CREATE TABLE IF NOT EXISTS cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    product_id INT,
    size VARCHAR(10),
    color VARCHAR(50),
    quantity INT DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    total_amount DECIMAL(10, 2),
    shipping_fee DECIMAL(10, 2) DEFAULT 30000,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'Chờ thanh toán',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert sample categories
INSERT INTO categories (name, name_vn, icon_path) VALUES
('Women', 'Nữ', ''),
('Men', 'Nam', ''),
('Children', 'Trẻ em', ''),
('Shoes', 'Giày', '');

-- Insert sample products
INSERT INTO products (name, name_vn, description, description_vn, brand, price, original_price, discount_percent, category_id, image_path, rating, review_count, badge) VALUES
('Cotton T-shirt', 'Áo thun Cotton Premium', '100% cotton material, regular fit', 'Chất liệu 100% cotton thoáng mát, form dáng regular fit phù hợp mọi hoạt động. Thiết kế tối giản nhưng tinh tế, dễ dàng phối hợp với nhiều loại trang phục khác nhau.', 'Coolmate', 250000, 320000, 20, 2, '', 4.8, 124, 'HOT'),
('Slim Fit Jeans', 'Quần Jeans Slim Fit', 'Classic slim fit jeans', 'Quần Jeans 501 classic với chất liệu denim cao cấp, form slim fit ôm sát tạo dáng thanh lịch. Phù hợp cho mọi dịp từ casual đến semi-formal.', 'Levi\'s', 550000, 680000, 20, 2, '', 4.5, 89, NULL),
('Summer Floral Dress', 'Váy Hoa Mùa Hè', 'Beautiful summer dress', 'Váy hoa mùa hè với họa tiết hoa tươi tắn, chất liệu vải mềm mại thoáng mát. Thiết kế dáng A-line thanh lịch, phù hợp cho các buổi tiệc và sự kiện.', 'Zara', 599000, 750000, 20, 1, '', 4.9, 156, 'NEW'),
('Sports Shoes', 'Giày Thể Thao RunPro', 'Comfortable running shoes', 'Giày thể thao với công nghệ đệm khí tiên tiến, đế cao su chống trượt. Phù hợp cho chạy bộ và các hoạt động thể thao hàng ngày.', 'Nike', 2100000, NULL, 0, 4, '', 4.7, 203, 'HOT'),
('Basic Black T-shirt', 'Áo phông đen Basic', 'Simple black t-shirt', 'Áo phông basic màu đen với chất liệu cotton 100%, form fit vừa vặn. Item cơ bản không thể thiếu trong tủ đồ của bạn.', 'Uniqlo', 150000, NULL, 0, 2, '', 4.6, 67, NULL),
('Canvas Tote Bag', 'Túi Tote Canvas', 'Stylish canvas tote bag', 'Túi tote canvas với thiết kế tối giản, quai xách chắc chắn. Dung tích lớn, phù hợp cho shopping và đi làm hàng ngày.', 'H&M', 180000, NULL, 0, 1, '', 4.4, 45, NULL),
('Bomber Jacket', 'Áo Khoác Bomber', 'Trendy bomber jacket', 'Áo khoác bomber với chất liệu nylon nhẹ, có lớp lót ấm áp. Thiết kế trẻ trung, năng động phù hợp với phong cách streetwear.', 'Zara', 890000, 1200000, 26, 2, '', 4.6, 112, 'NEW'),
('Floral Maxi Dress', 'Váy Maxi Hoa', 'Elegant maxi dress', 'Váy maxi dài với họa tiết hoa thanh lịch, dáng suông thoải mái. Chất liệu vải mềm mại, phù hợp cho các dịp đặc biệt.', 'H&M', 420000, 550000, 24, 1, '', 4.9, 98, NULL),
('Denim Jacket', 'Áo khoác Denim Nam', 'Classic denim jacket', 'Áo khoác denim cổ điển với thiết kế timeless. Chất liệu denim dày dặn, bền đẹp theo thời gian. Phù hợp cho mọi mùa.', 'Levi\'s', 500000, NULL, 0, 2, '', 4.5, 134, NULL),
('White Sneakers', 'Giày Sneaker Trắng', 'Classic white sneakers', 'Giày sneaker trắng cổ điển với thiết kế đơn giản nhưng thanh lịch. Đế cao su chống trượt, phù hợp cho mọi hoạt động hàng ngày.', 'Adidas', 1200000, 1500000, 20, 4, '', 4.8, 189, 'HOT'),
('Polo Shirt', 'Áo Polo Nam', 'Classic polo shirt', 'Áo polo với chất liệu pique cotton thoáng mát, cổ bẻ thanh lịch. Phù hợp cho văn phòng và các dịp bán chính thức.', 'Polo Ralph Lauren', 650000, 850000, 24, 2, '', 4.7, 145, NULL),
('Summer Shorts', 'Quần Short Mùa Hè', 'Comfortable summer shorts', 'Quần short với chất liệu cotton mềm mại, thoáng mát. Thiết kế gọn gàng, phù hợp cho các hoạt động ngoài trời mùa hè.', 'Uniqlo', 200000, NULL, 0, 2, '', 4.5, 98, NULL),
('Blazer Jacket', 'Áo Blazer Nữ', 'Elegant blazer jacket', 'Áo blazer với thiết kế thanh lịch, form fit tôn dáng. Chất liệu vải cao cấp, phù hợp cho văn phòng và các dịp quan trọng.', 'Zara', 1200000, 1500000, 20, 1, '', 4.8, 167, 'NEW'),
('High Heels', 'Giày Cao Gót', 'Elegant high heels', 'Giày cao gót với thiết kế thanh lịch, đế cao 8cm. Chất liệu da thật, phù hợp cho các dịp đặc biệt và văn phòng.', 'Charles & Keith', 800000, 1000000, 20, 4, '', 4.6, 134, NULL),
('Winter Coat', 'Áo Khoác Mùa Đông', 'Warm winter coat', 'Áo khoác mùa đông với lớp lót bông ấm áp, chống gió tốt. Thiết kế dài đến đầu gối, bảo vệ toàn thân khỏi cái lạnh.', 'The North Face', 2500000, 3000000, 17, 2, '', 4.9, 203, 'HOT');

-- Insert product colors
INSERT INTO product_colors (product_id, color_name, color_code) VALUES
(1, 'Navy Blue', '#1e3a5f'),
(1, 'White', '#ffffff'),
(1, 'Light Gray', '#e0e0e0'),
(1, 'Brown', '#8b4513'),
(2, 'Blue', '#4169e1'),
(2, 'Light Blue', '#87ceeb'),
(3, 'Beige', '#f5f5dc'),
(4, 'Blue', '#0000ff');

-- Insert product sizes
INSERT INTO product_sizes (product_id, size) VALUES
(1, 'S'), (1, 'M'), (1, 'L'), (1, 'XL'), (1, '2XL'),
(2, '28'), (2, '30'), (2, '32'), (2, '34'), (2, '36'),
(3, 'S'), (3, 'M'), (3, 'L'),
(4, '38'), (4, '39'), (4, '40'), (4, '41'), (4, '42');

-- Insert sample user
INSERT INTO users (name, email, phone, password, membership_level, points) VALUES
('Nguyễn Văn A', 'nguyenvana@example.com', '0123456789', '123456', 'Gold', 750),
('Admin', 'admin@fashionstore.com', '0987654321', 'admin123', 'Diamond', 2000);

