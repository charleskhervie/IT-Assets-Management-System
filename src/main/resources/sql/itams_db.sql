CREATE DATABASE IF NOT EXISTS itams_db;
USE itams_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `transaction`;
DROP TABLE IF EXISTS `units`;
DROP TABLE IF EXISTS `equipment`;
DROP TABLE IF EXISTS `employees`;
DROP TABLE IF EXISTS `departments`;
DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `categories` VALUES 
(1,'Laptop'),
(2,'Monitor'),
(3,'Keyboard'),
(4,'Mouse'),
(5,'Docking Station'),
(6,'Headset');

CREATE TABLE `departments` (
  `department_id` int NOT NULL AUTO_INCREMENT,
  `department_name` varchar(100) NOT NULL,
  `location` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `departments` VALUES 
(1,'IT Services','Building A - Room 101'),
(2,'Human Resources','Building A - Room 202'),
(3,'Accounting','Building B - Room 105'),
(4,'Marketing','Building C - Room 301'),
(5,'Academic Affairs','Building A - Room 102'),
(6,'Research and Development','Building D - Room 404');

CREATE TABLE `employees` (
  `emp_id` int NOT NULL AUTO_INCREMENT,
  `department_id` int DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  PRIMARY KEY (`emp_id`),
  UNIQUE KEY `username` (`username`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `employees` VALUES 
(1,1,'admin_xeon','pass123','Admin','Xeon Dadulla'),
(2,1,'staff_andrei','pass456','Staff','Andrei Dadivalos'),
(3,1,'staff_charles','pass789','Staff','Charles Realino'),
(4,2,'jdoe_hr','pass000','Staff','Jane Doe'),
(5,3,'msmith_acc','pass111','Staff','Mark Smith'),
(6,5,'rreyes_acad','pass222','Staff','Reyna Reyes');

CREATE TABLE `equipment` (
  `equipment_id` int NOT NULL AUTO_INCREMENT,
  `equipment_name` varchar(100) NOT NULL,
  `brand` varchar(50) DEFAULT NULL,
  `model` varchar(50) DEFAULT NULL,
  `specifications` text,
  `category_id` int DEFAULT NULL,
  PRIMARY KEY (`equipment_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `equipment_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `equipment` VALUES 
(1,'MacBook Air M2','Apple','A2681','8GB RAM, 256GB SSD',1),
(2,'ThinkPad X1 Carbon','Lenovo','Gen 11','16GB RAM, 512GB SSD',1),
(3,'UltraSharp 27','Dell','U2723QE','4K Resolution, USB-C Hub',2),
(4,'MX Keys S','Logitech','920-011406','Wireless, Backlit',3),
(5,'MX Master 3S','Logitech','910-006557','8000 DPI, Silent Clicks',4),
(6,'Surface Dock 2','Microsoft','1917','199W Power Supply',5),
(7,'WH-1000XM5','Sony','WH1000XM5','30hr Battery, Noise Cancelling',6),
(8,'UltraSharp 32','Dell','U3223QE','4K Resolution, USB-C Hub',2),
(9,'MX Master 3S Pro','Logitech','910-006780','8000 DPI, Ergonomic',4);

CREATE TABLE `units` (
  `unit_id` int NOT NULL AUTO_INCREMENT,
  `equipment_id` int DEFAULT NULL,
  `serial_number` varchar(100) NOT NULL,
  `status` varchar(20) DEFAULT 'Available',
  `added_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `assigned_to` int DEFAULT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`unit_id`),
  UNIQUE KEY `serial_number` (`serial_number`),
  KEY `equipment_id` (`equipment_id`),
  KEY `added_by` (`added_by`),
  KEY `assigned_to` (`assigned_to`),
  CONSTRAINT `units_ibfk_1` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`equipment_id`),
  CONSTRAINT `units_ibfk_2` FOREIGN KEY (`added_by`) REFERENCES `employees` (`emp_id`),
  CONSTRAINT `units_ibfk_3` FOREIGN KEY (`assigned_to`) REFERENCES `employees` (`emp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `units` VALUES 
(1,1,'SN-APPLE-001','Available',1,'2026-01-10 08:00:00',NULL,FALSE),
(2,1,'SN-APPLE-002','Available',1,'2026-01-10 08:00:00',NULL,FALSE),
(3,1,'SN-APPLE-003','Available',2,'2026-01-10 08:00:00',NULL,FALSE),
(4,2,'SN-LENO-001','Available',1,'2026-01-12 08:00:00',NULL,FALSE),
(5,2,'SN-LENO-002','Available',2,'2026-01-12 08:00:00',NULL,FALSE),
(6,2,'SN-LENO-003','Available',1,'2026-01-12 08:00:00',NULL,FALSE),
(7,3,'SN-DELL-MON-001','Available',1,'2026-01-15 08:00:00',NULL,FALSE),
(8,3,'SN-DELL-MON-002','Available',2,'2026-01-15 08:00:00',NULL,FALSE),
(9,8,'SN-DELL-MON-003','Available',1,'2026-01-15 08:00:00',NULL,FALSE),
(10,4,'SN-LOGI-KB-001','Available',3,'2026-01-18 08:00:00',NULL,FALSE),
(11,4,'SN-LOGI-KB-002','Available',1,'2026-01-18 08:00:00',NULL,FALSE),
(12,5,'SN-LOGI-MS-001','Available',1,'2026-01-20 08:00:00',NULL,FALSE),
(13,5,'SN-LOGI-MS-002','Available',2,'2026-01-20 08:00:00',NULL,FALSE),
(14,6,'SN-MSFT-DOCK-001','Available',1,'2026-01-22 08:00:00',NULL,FALSE),
(15,6,'SN-MSFT-DOCK-002','Maintenance',2,'2026-01-22 08:00:00',NULL,FALSE),
(16,7,'SN-SONY-HEAD-001','Available',1,'2026-01-25 08:00:00',NULL,FALSE),
(17,7,'SN-SONY-HEAD-002','Available',3,'2026-01-25 08:00:00',NULL,FALSE),
(18,9,'SN-LOGI-MS-PRO-001','Available',1,'2026-02-01 08:00:00',NULL,FALSE),
(19,9,'SN-LOGI-MS-PRO-002','Available',2,'2026-02-01 08:00:00',NULL,FALSE);

CREATE TABLE `transaction` (
  `transaction_id` int NOT NULL AUTO_INCREMENT,
  `unit_id` int DEFAULT NULL,
  `borrowed_by` int DEFAULT NULL,
  `processed_by` int DEFAULT NULL,
  `borrowed_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `return_date` timestamp NULL DEFAULT NULL,
  `remarks` text,
  `status` varchar(20) DEFAULT 'checked out',
  PRIMARY KEY (`transaction_id`),
  KEY `unit_id` (`unit_id`),
  KEY `borrowed_by` (`borrowed_by`),
  KEY `processed_by` (`processed_by`),
  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`unit_id`) REFERENCES `units` (`unit_id`),
  CONSTRAINT `transaction_ibfk_2` FOREIGN KEY (`borrowed_by`) REFERENCES `employees` (`emp_id`),
  CONSTRAINT `transaction_ibfk_3` FOREIGN KEY (`processed_by`) REFERENCES `employees` (`emp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;