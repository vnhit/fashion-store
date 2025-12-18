-- Thêm trường giới tính và loại sản phẩm vào bảng products
USE fashionstore;

-- Thêm trường gender (giới tính): Nam, Nữ, Unisex
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS gender VARCHAR(20) DEFAULT 'Unisex' 
COMMENT 'Giới tính: Nam, Nữ, Unisex';

-- Thêm trường product_type (loại sản phẩm): Áo, Quần, Giày, Phụ kiện
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS product_type VARCHAR(50) DEFAULT NULL 
COMMENT 'Loại sản phẩm: Áo, Quần, Giày, Phụ kiện';

-- Cập nhật product_type dựa trên category_id và tên sản phẩm
UPDATE products 
SET product_type = 'Giày' 
WHERE category_id = 4; -- Shoes category

-- Cập nhật product_type dựa trên tên sản phẩm
UPDATE products 
SET product_type = 'Áo' 
WHERE (name_vn LIKE '%áo%' OR name_vn LIKE '%Áo%' OR name LIKE '%shirt%' OR name LIKE '%jacket%' OR name LIKE '%coat%')
AND product_type IS NULL;

UPDATE products 
SET product_type = 'Quần' 
WHERE (name_vn LIKE '%quần%' OR name_vn LIKE '%Quần%' OR name LIKE '%pants%' OR name LIKE '%jeans%' OR name LIKE '%shorts%')
AND product_type IS NULL;

-- Cập nhật gender dựa trên category_id
UPDATE products 
SET gender = 'Nữ' 
WHERE category_id = 1; -- Women category

UPDATE products 
SET gender = 'Nam' 
WHERE category_id = 2; -- Men category

UPDATE products 
SET gender = 'Trẻ em' 
WHERE category_id = 3; -- Children category

-- Cập nhật gender dựa trên tên sản phẩm nếu chưa có
UPDATE products 
SET gender = 'Nữ' 
WHERE (name_vn LIKE '%nữ%' OR name_vn LIKE '%Nữ%' OR name LIKE '%women%' OR name LIKE '%ladies%')
AND gender = 'Unisex';

UPDATE products 
SET gender = 'Nam' 
WHERE (name_vn LIKE '%nam%' OR name_vn LIKE '%Nam%' OR name LIKE '%men%' OR name LIKE '%male%')
AND gender = 'Unisex';




