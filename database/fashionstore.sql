-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 08, 2026 at 03:49 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `fashionstore`
--

-- --------------------------------------------------------

--
-- Table structure for table `addresses`
--

CREATE TABLE `addresses` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `province` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `ward` varchar(100) DEFAULT NULL,
  `street_address` varchar(255) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `addresses`
--

INSERT INTO `addresses` (`id`, `user_id`, `full_name`, `phone`, `province`, `district`, `ward`, `street_address`, `is_default`, `created_at`) VALUES
(1, 1, 'Nguyễn Văn A', '0123456789', 'Hà Nội', 'Quận Cầu Giấy', 'Phường Dịch Vọng', '123 Đường ABC', 1, '2025-12-15 23:08:59'),
(2, 1, 'Nguyễn Văn A', '0123456789', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '456 Đường XYZ', 0, '2025-12-15 23:08:59'),
(3, 3, 'Anh', '12345678', 'Hà Nội', 'Thanh Xuân', 'Tân Triều', 'Số 1', 1, '2025-12-15 23:12:38');

-- --------------------------------------------------------

--
-- Table structure for table `cart`
--

CREATE TABLE `cart` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL,
  `size` varchar(10) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  `quantity` int(11) DEFAULT 1,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cart`
--

INSERT INTO `cart` (`id`, `user_id`, `product_id`, `size`, `color`, `quantity`, `added_at`) VALUES
(14, 3, 13, '37', '#8b4513', 1, '2026-01-05 11:41:57');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `name_vn` varchar(100) NOT NULL,
  `icon_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `name`, `name_vn`, `icon_path`) VALUES
(1, 'Women', 'Nữ', 'Nữ.png'),
(2, 'Men', 'Nam', 'Nam.png'),
(3, 'Children', 'Trẻ em', 'Trẻ_em.png'),
(4, 'Shoes', 'Giày', 'Giày.png');

-- --------------------------------------------------------

--
-- Table structure for table `favorites`
--

CREATE TABLE `favorites` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `favorites`
--

INSERT INTO `favorites` (`id`, `user_id`, `product_id`, `created_at`) VALUES
(2, 3, 11, '2025-12-15 11:07:01'),
(3, 3, 12, '2025-12-15 11:07:08'),
(20, 3, 10, '2025-12-16 12:24:32'),
(22, 3, 8, '2026-01-04 11:14:25');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `type` varchar(50) NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `order_id`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 3, 3, 'Đơn hàng #3 đã được vận chuyển. Vui lòng chuẩn bị nhận hàng!', 'ORDER_SHIPPED', 1, '2026-01-04 10:26:51'),
(2, 3, 3, 'Đơn hàng #3 đã được giao thành công. Cảm ơn bạn đã mua hàng!', 'ORDER_DELIVERED', 1, '2026-01-04 10:27:49');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `shipping_fee` decimal(10,2) DEFAULT 30000.00,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `status` varchar(50) DEFAULT 'Chờ thanh toán',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`id`, `user_id`, `total_amount`, `shipping_fee`, `discount_amount`, `status`, `created_at`) VALUES
(1, 3, 549000.00, 0.00, 50000.00, 'Hoàn thành', '2025-12-16 09:48:16'),
(2, 3, 450000.00, 30000.00, 0.00, 'Hoàn thành', '2025-12-17 04:36:43'),
(3, 3, 5000000.00, 0.00, 1000000.00, 'Hoàn thành', '2026-01-04 10:26:02');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `name_vn` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `description_vn` text DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `discount_percent` int(11) DEFAULT 0,
  `category_id` int(11) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `rating` decimal(3,1) DEFAULT 0.0,
  `review_count` int(11) DEFAULT 0,
  `badge` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `gender` varchar(20) DEFAULT 'Unisex' COMMENT 'Giới tính: Nam, Nữ, Unisex',
  `product_type` varchar(50) DEFAULT NULL COMMENT 'Loại sản phẩm: Áo, Quần, Giày, Phụ kiện'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id`, `name`, `name_vn`, `description`, `description_vn`, `brand`, `price`, `original_price`, `discount_percent`, `category_id`, `image_path`, `rating`, `review_count`, `badge`, `created_at`, `gender`, `product_type`) VALUES
(8, 'Váy Maxi Hoa', 'Váy Maxi Hoa', 'Váy Maxi Hoa', 'Váy Maxi Hoa', 'H&M', 420000.00, NULL, 0, 1, 'vay_maxi_hoa.png', 4.9, 98, NULL, '2025-12-15 03:00:43', 'Nữ', 'Quần'),
(10, 'Áo thun Cotton Premium', 'Áo thun Cotton Premium', 'Chất liệu 100% cotton thoáng mát, form dáng regular fit phù hợp mọi hoạt động. Thiết kế tối giản nhưng tinh tế, dễ dàng phối hợp với nhiều loại trang phục khác nhau.', 'Chất liệu 100% cotton thoáng mát, form dáng regular fit phù hợp mọi hoạt động. Thiết kế tối giản nhưng tinh tế, dễ dàng phối hợp với nhiều loại trang phục khác nhau.', 'Coolmate', 250000.00, 320000.00, 20, 2, 'ao_thun_cotton_premium.png', 4.8, 124, 'HOT', '2025-12-15 03:26:11', 'Nam', 'Áo'),
(11, 'Quần Jeans Slim Fit', 'Quần Jeans Slim Fit', 'Quần Jeans 501 classic với chất liệu denim cao cấp, form slim fit ôm sát tạo dáng thanh lịch. Phù hợp cho mọi dịp từ casual đến semi-formal.', 'Quần Jeans 501 classic với chất liệu denim cao cấp, form slim fit ôm sát tạo dáng thanh lịch. Phù hợp cho mọi dịp từ casual đến semi-formal.', 'Levi\'s', 550000.00, 680000.00, 20, 2, 'quan_jeans_slim_fit.png', 4.5, 89, NULL, '2025-12-15 03:26:11', 'Nam', 'Quần'),
(12, 'Váy Hoa Mùa Hè', 'Váy Hoa Mùa Hè', 'Váy hoa mùa hè với họa tiết hoa tươi tắn, chất liệu vải mềm mại thoáng mát. Thiết kế dáng A-line thanh lịch, phù hợp cho các buổi tiệc và sự kiện.', 'Váy hoa mùa hè với họa tiết hoa tươi tắn, chất liệu vải mềm mại thoáng mát. Thiết kế dáng A-line thanh lịch, phù hợp cho các buổi tiệc và sự kiện.', 'Zara', 599000.00, 750000.00, 20, 1, 'vay_hoa_mua_he.png', 4.9, 156, 'NEW', '2025-12-15 03:26:11', 'Nữ', 'Quần'),
(13, 'Giày Thể Thao RunPro', 'Giày Thể Thao RunPro', 'Giày thể thao với công nghệ đệm khí tiên tiến, đế cao su chống trượt. Phù hợp cho chạy bộ và các hoạt động thể thao hàng ngày.', 'Giày thể thao với công nghệ đệm khí tiên tiến, đế cao su chống trượt. Phù hợp cho chạy bộ và các hoạt động thể thao hàng ngày.', 'Nike', 2100000.00, NULL, 0, 4, 'giay_the_thao_runpro.png', 4.7, 203, 'HOT', '2025-12-15 03:26:11', 'Unisex', 'Giày'),
(14, 'Áo phông đen Basic', 'Áo phông đen Basic', 'Áo phông basic màu đen với chất liệu cotton 100%, form fit vừa vặn. Item cơ bản không thể thiếu trong tủ đồ của bạn.', 'Áo phông basic màu đen với chất liệu cotton 100%, form fit vừa vặn. Item cơ bản không thể thiếu trong tủ đồ của bạn.', 'Uniqlo', 150000.00, NULL, 0, 2, 'ao_phong_den_basic.png', 4.6, 67, NULL, '2025-12-15 03:26:11', 'Nam', 'Áo'),
(15, 'Túi Tote Canvas', 'Túi Tote Canvas', 'Túi tote canvas với thiết kế tối giản, quai xách chắc chắn. Dung tích lớn, phù hợp cho shopping và đi làm hàng ngày.', 'Túi tote canvas với thiết kế tối giản, quai xách chắc chắn. Dung tích lớn, phù hợp cho shopping và đi làm hàng ngày.', 'H&M', 180000.00, NULL, 0, 1, 'tui_tote_canvas.png', 4.4, 45, NULL, '2025-12-15 03:26:11', 'Nữ', NULL),
(16, 'Áo Khoác Bomber', 'Áo Khoác Bomber', 'Áo khoác bomber với chất liệu nylon nhẹ, có lớp lót ấm áp. Thiết kế trẻ trung, năng động phù hợp với phong cách streetwear.', 'Áo khoác bomber với chất liệu nylon nhẹ, có lớp lót ấm áp. Thiết kế trẻ trung, năng động phù hợp với phong cách streetwear.', 'Zara', 890000.00, 1200000.00, 26, 2, 'ao_khoac_bomber.png', 4.6, 112, 'NEW', '2025-12-15 03:26:11', 'Nam', 'Áo'),
(18, 'Áo khoác Denim Nam', 'Áo khoác Denim Nam', 'Áo khoác denim cổ điển với thiết kế timeless. Chất liệu denim dày dặn, bền đẹp theo thời gian. Phù hợp cho mọi mùa.', 'Áo khoác denim cổ điển với thiết kế timeless. Chất liệu denim dày dặn, bền đẹp theo thời gian. Phù hợp cho mọi mùa.', 'Levi\'s', 500000.00, NULL, 0, 2, 'ao_khoac_denim_nam.png', 4.5, 134, NULL, '2025-12-15 03:26:11', 'Nam', 'Áo'),
(19, 'Giày Sneaker Trắng', 'Giày Sneaker Trắng', 'Giày sneaker trắng cổ điển với thiết kế đơn giản nhưng thanh lịch. Đế cao su chống trượt, phù hợp cho mọi hoạt động hàng ngày.', 'Giày sneaker trắng cổ điển với thiết kế đơn giản nhưng thanh lịch. Đế cao su chống trượt, phù hợp cho mọi hoạt động hàng ngày.', 'Adidas', 1200000.00, 1500000.00, 20, 4, 'giay_sneaker_trang.png', 4.8, 189, 'HOT', '2025-12-15 03:26:11', 'Unisex', 'Giày'),
(20, 'Áo Polo Nam', 'Áo Polo Nam', 'Áo polo với chất liệu pique cotton thoáng mát, cổ bẻ thanh lịch. Phù hợp cho văn phòng và các dịp bán chính thức.', 'Áo polo với chất liệu pique cotton thoáng mát, cổ bẻ thanh lịch. Phù hợp cho văn phòng và các dịp bán chính thức.', 'Polo Ralph Lauren', 650000.00, 850000.00, 24, 2, 'ao_polo_nam.png', 4.7, 145, NULL, '2025-12-15 03:26:11', 'Nam', 'Áo'),
(21, 'Quần Short Mùa Hè', 'Quần Short Mùa Hè', 'Quần short với chất liệu cotton mềm mại, thoáng mát. Thiết kế gọn gàng, phù hợp cho các hoạt động ngoài trời mùa hè.', 'Quần short với chất liệu cotton mềm mại, thoáng mát. Thiết kế gọn gàng, phù hợp cho các hoạt động ngoài trời mùa hè.', 'Uniqlo', 200000.00, NULL, 0, 2, 'quan_short_mua_he.png', 4.5, 98, NULL, '2025-12-15 03:26:11', 'Nam', 'Quần'),
(22, 'Áo Blazer Nữ', 'Áo Blazer Nữ', 'Áo blazer với thiết kế thanh lịch, form fit tôn dáng. Chất liệu vải cao cấp, phù hợp cho văn phòng và các dịp quan trọng.', 'Áo blazer với thiết kế thanh lịch, form fit tôn dáng. Chất liệu vải cao cấp, phù hợp cho văn phòng và các dịp quan trọng.', 'Zara', 1200000.00, 1500000.00, 20, 1, 'ao_blazer_nu.png', 4.8, 167, 'NEW', '2025-12-15 03:26:11', 'Nữ', 'Áo'),
(23, 'Giày Cao Gót', 'Giày Cao Gót', 'Giày cao gót với thiết kế thanh lịch, đế cao 8cm. Chất liệu da thật, phù hợp cho các dịp đặc biệt và văn phòng.', 'Giày cao gót với thiết kế thanh lịch, đế cao 8cm. Chất liệu da thật, phù hợp cho các dịp đặc biệt và văn phòng.', 'Charles & Keith', 800000.00, 1000000.00, 20, 4, 'giay_cao_got.png', 4.6, 134, NULL, '2025-12-15 03:26:11', 'Unisex', 'Giày'),
(24, 'Áo Khoác Mùa Đông', 'Áo Khoác Mùa Đông', 'Áo khoác mùa đông với lớp lót bông ấm áp, chống gió tốt. Thiết kế dài đến đầu gối, bảo vệ toàn thân khỏi cái lạnh.', 'Áo khoác mùa đông với lớp lót bông ấm áp, chống gió tốt. Thiết kế dài đến đầu gối, bảo vệ toàn thân khỏi cái lạnh.', 'The North Face', 2500000.00, 3000000.00, 17, 2, 'ao_khoac_mua_dong.png', 4.9, 203, 'HOT', '2025-12-15 03:26:11', 'Nam', 'Áo'),
(25, 'Áo thun in hình động vật', 'Áo thun in hình động vật', 'Áo thun cổ tròn chất liệu 100% cotton organic an toàn cho da bé. Hình in động vật ngộ nghĩnh, mực in không bong tróc', 'Áo thun cổ tròn chất liệu 100% cotton organic an toàn cho da bé. Hình in động vật ngộ nghĩnh, mực in không bong tróc', 'Zara', 270000.00, 300000.00, 10, 3, 'ao_thun_in_hinh_dong_vat.png', 5.0, 1, 'NEW', '2026-01-01 09:16:48', 'Trẻ em', 'Áo'),
(26, 'Váy hoa nhí công chúa', 'Váy hoa nhí công chúa', 'Váy xòe họa tiết hoa nhí xinh xắn, tay bồng nhẹ nhàng. Chất liệu vải thô lụa mát mẻ, thấm hút mồ hôi tốt cho bé vui chơi', 'Váy xòe họa tiết hoa nhí xinh xắn, tay bồng nhẹ nhàng. Chất liệu vải thô lụa mát mẻ, thấm hút mồ hôi tốt cho bé vui chơi', 'H&M', 250000.00, NULL, 0, 3, 'product.png', 0.0, 0, NULL, '2026-01-01 09:20:56', 'Trẻ em', 'Áo'),
(27, 'Quần Short Jeans bé trai', 'Quần Short Jeans bé trai', 'Quần short jeans lưng chun co giãn mềm mại, không gây hằn bụng. Chất denim đã qua xử lý wash mềm, bền đẹp', 'Quần short jeans lưng chun co giãn mềm mại, không gây hằn bụng. Chất denim đã qua xử lý wash mềm, bền đẹp', 'H&M', 160000.00, 200000.00, 20, 3, 'quan_short_jeans_be_trai.png', 0.0, 0, 'HOT', '2026-01-01 09:25:09', 'Trẻ em', 'Quần'),
(28, 'Bộ nỉ thể thao thu đông', 'Bộ nỉ thể thao thu đông', 'Bộ đồ nỉ da cá dày dặn, giữ ấm tốt cho ngày lạnh. Thiết kế bo gấu gọn gàng, phong cách thể thao năng động cho cả bé trai và gái.', 'Bộ đồ nỉ da cá dày dặn, giữ ấm tốt cho ngày lạnh. Thiết kế bo gấu gọn gàng, phong cách thể thao năng động cho cả bé trai và gái.', 'Zara', 450000.00, NULL, 0, 3, 'bo_ni_the_thao_thu_dong.png', 0.0, 0, NULL, '2026-01-01 09:27:54', 'Trẻ em', 'Áo'),
(29, 'Giày thể thao siêu nhẹ', 'Giày thể thao siêu nhẹ', 'Giày thể thao đế eva siêu nhẹ, chống trượt an toàn. Thiết kế quai dán tiện lợi giúp bé dễ dàng tự mang tháo', 'Giày thể thao đế eva siêu nhẹ, chống trượt an toàn. Thiết kế quai dán tiện lợi giúp bé dễ dàng tự mang tháo', 'Nike', 420000.00, NULL, 0, 3, 'giay_the_thao_sieu_nhe.png', 0.0, 0, NULL, '2026-01-01 09:31:45', 'Trẻ em', 'Giày');

-- --------------------------------------------------------

--
-- Table structure for table `product_colors`
--

CREATE TABLE `product_colors` (
  `id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL,
  `color_name` varchar(50) DEFAULT NULL,
  `color_code` varchar(7) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product_sizes`
--

CREATE TABLE `product_sizes` (
  `id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL,
  `size` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_sizes`
--

INSERT INTO `product_sizes` (`id`, `product_id`, `size`) VALUES
(49, 11, 'S'),
(50, 11, 'M'),
(51, 11, 'L'),
(52, 11, 'XL'),
(53, 12, 'S'),
(54, 12, 'M'),
(55, 12, 'L'),
(56, 12, 'XL'),
(57, 13, '35'),
(58, 13, '36'),
(59, 13, '37'),
(60, 13, '38'),
(61, 13, '39'),
(62, 14, 'S'),
(63, 14, 'M'),
(64, 14, 'L'),
(65, 14, 'XL'),
(66, 15, 'S'),
(67, 15, 'M'),
(68, 15, 'L'),
(69, 16, 'S'),
(70, 16, 'M'),
(71, 16, 'L'),
(72, 16, 'XL'),
(73, 18, 'S'),
(74, 18, 'M'),
(75, 18, 'L'),
(76, 18, 'XL'),
(77, 19, '36'),
(78, 19, '37'),
(79, 19, '38'),
(80, 19, '39'),
(81, 20, 'S'),
(82, 20, 'M'),
(83, 20, 'L'),
(84, 20, 'XL'),
(85, 21, 'S'),
(86, 21, 'M'),
(87, 21, 'L'),
(88, 21, 'XL'),
(89, 22, 'S'),
(90, 22, 'M'),
(91, 22, 'L'),
(92, 23, '36'),
(93, 23, '37'),
(94, 23, '38'),
(95, 23, '39'),
(96, 24, 'S'),
(97, 24, 'M'),
(98, 24, 'L'),
(99, 24, 'XL'),
(104, 8, 'S'),
(105, 8, 'M'),
(106, 8, 'L'),
(110, 25, 'S'),
(111, 25, 'M'),
(112, 25, 'L'),
(116, 26, 'S'),
(117, 26, 'M'),
(118, 26, 'L'),
(119, 27, 'S'),
(120, 27, 'M'),
(121, 27, 'L'),
(122, 29, '35'),
(123, 29, '36'),
(124, 29, '37'),
(125, 10, 'S'),
(126, 10, 'M'),
(127, 10, 'L'),
(128, 10, 'XL'),
(129, 28, 'S'),
(130, 28, 'M'),
(131, 28, 'L');

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `rating` int(11) NOT NULL CHECK (`rating` >= 1 and `rating` <= 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reviews`
--

INSERT INTO `reviews` (`id`, `product_id`, `user_id`, `rating`, `comment`, `created_at`, `updated_at`) VALUES
(2, 25, 3, 5, '', '2026-01-01 09:17:27', '2026-01-01 09:17:27');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar_path` varchar(255) DEFAULT NULL,
  `membership_level` varchar(50) DEFAULT 'Silver',
  `points` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `password` varchar(255) DEFAULT NULL,
  `default_address_id` int(11) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `phone`, `avatar_path`, `membership_level`, `points`, `created_at`, `password`, `default_address_id`, `date_of_birth`, `gender`) VALUES
(1, 'Nguyễn Văn A', 'nguyenvana@example.com', '0123456789', NULL, 'Silver', 750, '2025-12-15 03:00:43', '123456', NULL, NULL, NULL),
(3, 'anh', 'a@gmail.com', '123456', 'C:\\Users\\Vinh\\Pictures\\Camera Roll\\wallpaperflare.com_wallpaper (1).jpg', 'Silver', 0, '2025-12-15 03:48:19', '1', 3, '2025-12-11', 'Nam'),
(4, 'un', 'an@gmail.com', '123456', NULL, 'Silver', 0, '2026-01-01 09:09:56', '1', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `vouchers`
--

CREATE TABLE `vouchers` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `description_vn` varchar(255) DEFAULT NULL,
  `discount_type` enum('PERCENTAGE','FIXED') NOT NULL DEFAULT 'PERCENTAGE',
  `discount_value` decimal(10,2) NOT NULL,
  `min_order_amount` decimal(10,2) DEFAULT 0.00,
  `max_discount_amount` decimal(10,2) DEFAULT NULL,
  `usage_limit` int(11) DEFAULT NULL,
  `used_count` int(11) DEFAULT 0,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vouchers`
--

INSERT INTO `vouchers` (`id`, `code`, `description`, `description_vn`, `discount_type`, `discount_value`, `min_order_amount`, `max_discount_amount`, `usage_limit`, `used_count`, `start_date`, `end_date`, `is_active`, `created_at`) VALUES
(1, 'WELCOME10', 'Welcome discount 10%', 'Giảm giá chào mừng 10%', 'PERCENTAGE', 10.00, 200000.00, 50000.00, 100, 0, '2025-12-16', '2026-01-15', 1, '2025-12-16 02:09:48'),
(2, 'SAVE50K', 'Save 50,000 VND', 'Tiết kiệm 50,000 VNĐ', 'FIXED', 50000.00, 300000.00, NULL, 50, 0, '2025-12-16', '2026-02-14', 1, '2025-12-16 02:09:48'),
(3, 'SUMMER20', 'Summer sale 20%', 'Giảm giá mùa hè 20%', 'PERCENTAGE', 20.00, 500000.00, 200000.00, 200, 0, '2025-12-16', '2026-03-16', 1, '2025-12-16 02:09:48'),
(4, 'VIP30', 'VIP discount 30%', 'Giảm giá VIP 30%', 'PERCENTAGE', 30.00, 1000000.00, 500000.00, 10, 0, '2025-12-16', '2026-12-16', 1, '2025-12-16 02:09:48');

-- --------------------------------------------------------

--
-- Table structure for table `voucher_usage`
--

CREATE TABLE `voucher_usage` (
  `id` int(11) NOT NULL,
  `voucher_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `order_id` int(11) DEFAULT NULL,
  `used_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `addresses`
--
ALTER TABLE `addresses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `cart`
--
ALTER TABLE `cart`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `favorites`
--
ALTER TABLE `favorites`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_user_product` (`user_id`,`product_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `product_colors`
--
ALTER TABLE `product_colors`
  ADD PRIMARY KEY (`id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `product_sizes`
--
ALTER TABLE `product_sizes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_user_product_review` (`user_id`,`product_id`),
  ADD KEY `idx_reviews_product` (`product_id`),
  ADD KEY `idx_reviews_user` (`user_id`),
  ADD KEY `idx_reviews_created` (`created_at`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `fk_default_address` (`default_address_id`);

--
-- Indexes for table `vouchers`
--
ALTER TABLE `vouchers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Indexes for table `voucher_usage`
--
ALTER TABLE `voucher_usage`
  ADD PRIMARY KEY (`id`),
  ADD KEY `voucher_id` (`voucher_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `addresses`
--
ALTER TABLE `addresses`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `cart`
--
ALTER TABLE `cart`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `favorites`
--
ALTER TABLE `favorites`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `product_colors`
--
ALTER TABLE `product_colors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `product_sizes`
--
ALTER TABLE `product_sizes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=132;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `vouchers`
--
ALTER TABLE `vouchers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `voucher_usage`
--
ALTER TABLE `voucher_usage`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `addresses`
--
ALTER TABLE `addresses`
  ADD CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `cart`
--
ALTER TABLE `cart`
  ADD CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `favorites`
--
ALTER TABLE `favorites`
  ADD CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- Constraints for table `product_colors`
--
ALTER TABLE `product_colors`
  ADD CONSTRAINT `product_colors_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `product_sizes`
--
ALTER TABLE `product_sizes`
  ADD CONSTRAINT `product_sizes_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_default_address` FOREIGN KEY (`default_address_id`) REFERENCES `addresses` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `voucher_usage`
--
ALTER TABLE `voucher_usage`
  ADD CONSTRAINT `voucher_usage_ibfk_1` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `voucher_usage_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
