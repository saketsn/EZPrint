-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 18, 2025 at 06:43 PM
-- Server version: 10.4.27-MariaDB
-- PHP Version: 8.1.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ezprint`
--

-- --------------------------------------------------------

--
-- Table structure for table `devices`
--

CREATE TABLE `devices` (
  `device_id` bigint(20) NOT NULL,
  `user_type` enum('student','shopkeeper') NOT NULL,
  `user_id` int(11) NOT NULL,
  `fcm_token` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `devices`
--

INSERT INTO `devices` (`device_id`, `user_type`, `user_id`, `fcm_token`, `created_at`) VALUES
(12, 'shopkeeper', 4, 'eBLK51-kTCK85E_1PAKll0:APA91bGCbyq8pOMC8iDvooWlPXmasrtW2xUc7AAohCOvNtnnZroChwb8-ZpfLYSF2EWTBTObCks4kLZZ5_Ua91fVqn8wDCGaWCWBGCbHCwEEgEE42N3mD1g', '2025-11-13 04:55:30'),
(13, 'shopkeeper', 4, 'eBLK51-kTCK85E_1PAKll0:APA91bGCbyq8pOMC8iDvooWlPXmasrtW2xUc7AAohCOvNtnnZroChwb8-ZpfLYSF2EWTBTObCks4kLZZ5_Ua91fVqn8wDCGaWCWBGCbHCwEEgEE42N3mD1g', '2025-11-13 04:55:30');

-- --------------------------------------------------------

--
-- Table structure for table `documents`
--

CREATE TABLE `documents` (
  `document_id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `doc_type` enum('print_file','id_proof') DEFAULT 'print_file',
  `file_url` varchar(255) NOT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `copies` int(11) NOT NULL DEFAULT 1,
  `is_color` tinyint(1) NOT NULL DEFAULT 0,
  `is_duplex` tinyint(1) NOT NULL DEFAULT 0,
  `orientation` enum('portrait','landscape') NOT NULL DEFAULT 'portrait',
  `page_range` varchar(100) NOT NULL DEFAULT 'All',
  `is_printed` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `documents`
--

INSERT INTO `documents` (`document_id`, `order_id`, `doc_type`, `file_url`, `uploaded_at`, `copies`, `is_color`, `is_duplex`, `orientation`, `page_range`, `is_printed`) VALUES
(43, 20, 'id_proof', 'uploads/EZP-4-191-20251112-1126-4028/shop4-ID-191-1762926979456.pdf', '2025-11-12 05:56:19', 1, 0, 0, 'portrait', 'All', 0),
(44, 20, 'print_file', 'uploads/EZP-4-191-20251112-1126-4028/shop4-PRINT-191-1762926979488.pdf', '2025-11-12 05:56:19', 1, 0, 0, 'portrait', 'All', 1),
(45, 21, 'id_proof', 'uploads/EZP-4-456-20251112-1152-1393/shop4-ID-456-1762928544842.jpg', '2025-11-12 06:22:25', 1, 0, 0, 'portrait', 'All', 0),
(46, 21, 'print_file', 'uploads/EZP-4-456-20251112-1152-1393/shop4-PRINT-456-1762928545190.pdf', '2025-11-12 06:22:25', 2, 0, 0, 'portrait', 'All', 0),
(47, 22, 'id_proof', 'uploads/EZP-4-24mcar0131-20251113-0958-6680/shop4-ID-24mcar0131-1763008136192.jpg', '2025-11-13 04:28:59', 1, 0, 0, 'portrait', 'All', 0),
(48, 22, 'print_file', 'uploads/EZP-4-24mcar0131-20251113-0958-6680/shop4-PRINT-24mcar0131-1763008139048.pdf', '2025-11-13 04:28:59', 2, 1, 0, 'portrait', 'All', 1),
(49, 23, 'id_proof', 'uploads/EZP-4-24mcar0131-20251113-1028-9389/shop4-ID-24mcar0131-1763009905777.jpg', '2025-11-13 04:58:31', 1, 0, 0, 'portrait', 'All', 0),
(50, 23, 'print_file', 'uploads/EZP-4-24mcar0131-20251113-1028-9389/shop4-PRINT-24mcar0131-1763009911480.pdf', '2025-11-13 04:58:31', 1, 0, 0, 'portrait', 'All', 0),
(51, 23, 'print_file', 'uploads/EZP-4-24mcar0131-20251113-1028-9389/shop4-PRINT-24mcar0131-1763009911664.pdf', '2025-11-13 04:58:31', 1, 1, 1, 'portrait', 'All', 1);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` bigint(20) NOT NULL,
  `user_type` enum('student','shopkeeper') NOT NULL,
  `user_id` int(11) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` bigint(20) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `order_code` varchar(50) DEFAULT NULL,
  `status` enum('pending','accepted','printing','ready','completed','rejected','cancelled') DEFAULT 'pending',
  `expected_time` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `notes` text DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_status` enum('unpaid','paid') NOT NULL DEFAULT 'unpaid'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `shop_id`, `student_id`, `order_code`, `status`, `expected_time`, `created_at`, `updated_at`, `notes`, `amount`, `payment_status`) VALUES
(20, 4, 15, 'EZP-4-191-20251112-1126-4028', 'completed', NULL, '2025-11-12 05:56:19', '2025-11-12 05:58:47', '', '40.00', 'paid'),
(21, 4, 15, 'EZP-4-456-20251112-1152-1393', 'completed', NULL, '2025-11-12 06:22:25', '2025-11-12 06:22:46', '', '50.00', 'paid'),
(22, 4, 16, 'EZP-4-24mcar0131-20251113-0958-6680', 'completed', NULL, '2025-11-13 04:28:59', '2025-11-13 04:30:08', 'Yes do stable', '10.00', 'paid'),
(23, 4, 16, 'EZP-4-24mcar0131-20251113-1028-9389', 'completed', NULL, '2025-11-13 04:58:31', '2025-11-13 04:59:39', 'Do Staple ', '15.00', 'paid');

-- --------------------------------------------------------

--
-- Table structure for table `price_settings`
--

CREATE TABLE `price_settings` (
  `price_setting_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `price_per_bw_page` decimal(10,2) NOT NULL DEFAULT 2.00,
  `price_per_color_page` decimal(10,2) NOT NULL DEFAULT 10.00,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `price_per_bw_duplex` decimal(10,2) NOT NULL DEFAULT 3.50,
  `price_per_color_duplex` decimal(10,2) NOT NULL DEFAULT 15.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `price_settings`
--

INSERT INTO `price_settings` (`price_setting_id`, `shop_id`, `price_per_bw_page`, `price_per_color_page`, `last_updated`, `price_per_bw_duplex`, `price_per_color_duplex`) VALUES
(3, 4, '2.00', '10.00', '2025-11-12 05:56:48', '3.50', '15.00');

-- --------------------------------------------------------

--
-- Table structure for table `shops`
--

CREATE TABLE `shops` (
  `shop_id` int(11) NOT NULL,
  `shop_name` varchar(100) NOT NULL,
  `owner_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `qr_code_url` varchar(255) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `profile_img` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shops`
--

INSERT INTO `shops` (`shop_id`, `shop_name`, `owner_name`, `email`, `phone`, `password_hash`, `qr_code_url`, `address`, `profile_img`, `created_at`) VALUES
(4, 'Ayush\'s Prints', 'Ayush Sharma', 'ayush@gmail.com', '9852345612', '$2b$10$W3.04xitgKcCc0XhrHM3oezYbgaMy2dDhAY9Kx0g1PAefkL55ZOkm', 'uploads/qr-codes/shop-qr-4-1763007985023.png', 'Jayanagar 9th Block', 'uploads/profile/profile_pic-1763008279939-696370632.jpg', '2025-11-12 05:53:16');

-- --------------------------------------------------------

--
-- Table structure for table `students`
--

CREATE TABLE `students` (
  `student_id` int(11) NOT NULL,
  `student_name` varchar(100) NOT NULL,
  `usn_or_id` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `students`
--

INSERT INTO `students` (`student_id`, `student_name`, `usn_or_id`, `email`, `phone`, `created_at`) VALUES
(15, 'Anush', '191', 'abc@gmail.com', '9065128045', '2025-11-12 05:56:19'),
(16, 'Akshita ', '24mcar0131 ', 'akshitabansal917@gmail.com', '8360483326', '2025-11-13 04:28:59');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `devices`
--
ALTER TABLE `devices`
  ADD PRIMARY KEY (`device_id`);

--
-- Indexes for table `documents`
--
ALTER TABLE `documents`
  ADD PRIMARY KEY (`document_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD UNIQUE KEY `order_code` (`order_code`),
  ADD KEY `shop_id` (`shop_id`),
  ADD KEY `student_id` (`student_id`);

--
-- Indexes for table `price_settings`
--
ALTER TABLE `price_settings`
  ADD PRIMARY KEY (`price_setting_id`),
  ADD UNIQUE KEY `shop_id` (`shop_id`);

--
-- Indexes for table `shops`
--
ALTER TABLE `shops`
  ADD PRIMARY KEY (`shop_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phone` (`phone`);

--
-- Indexes for table `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`student_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `devices`
--
ALTER TABLE `devices`
  MODIFY `device_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `documents`
--
ALTER TABLE `documents`
  MODIFY `document_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `price_settings`
--
ALTER TABLE `price_settings`
  MODIFY `price_setting_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `shops`
--
ALTER TABLE `shops`
  MODIFY `shop_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `students`
--
ALTER TABLE `students`
  MODIFY `student_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `documents`
--
ALTER TABLE `documents`
  ADD CONSTRAINT `documents_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`shop_id`),
  ADD CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`);

--
-- Constraints for table `price_settings`
--
ALTER TABLE `price_settings`
  ADD CONSTRAINT `fk_shop_price` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`shop_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
