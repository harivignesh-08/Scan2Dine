-- Database Creation
CREATE DATABASE IF NOT EXISTS `scan2dine` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `scan2dine`;

-- 1. College Table
CREATE TABLE IF NOT EXISTS `colleges` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_name` VARCHAR(150) NOT NULL,
    `college_code` VARCHAR(50) NOT NULL UNIQUE,
    `logo` VARCHAR(255),
    `theme_color` VARCHAR(50) DEFAULT '#4F46E5',
    `email` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `erp_name` VARCHAR(100),
    `erp_base_url` VARCHAR(255),
    `erp_api_key` VARCHAR(255),
    `subscription_plan` VARCHAR(50) NOT NULL DEFAULT 'FREE', -- FREE, BASIC, PREMIUM
    `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',        -- PENDING, APPROVED, SUSPENDED
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Users Table (Platform level / Tenant level)
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT, -- NULL for Super Admins
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) NOT NULL, -- SUPER_ADMIN, COLLEGE_ADMIN, WARDEN
    `active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Hostels Table
CREATE TABLE IF NOT EXISTS `hostels` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `capacity` INT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_hostel_per_college` (`college_id`, `name`)
) ENGINE=InnoDB;

-- 4. Rooms Table
CREATE TABLE IF NOT EXISTS `rooms` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `hostel_id` BIGINT NOT NULL,
    `room_number` VARCHAR(50) NOT NULL,
    `capacity` INT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hostel_id`) REFERENCES `hostels`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_room_per_hostel` (`college_id`, `hostel_id`, `room_number`)
) ENGINE=InnoDB;

-- 5. Students Table
CREATE TABLE IF NOT EXISTS `students` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `roll_number` VARCHAR(50) NOT NULL,
    `department` VARCHAR(100) NOT NULL,
    `year` INT NOT NULL,
    `phone` VARCHAR(20),
    `hostel_id` BIGINT,
    `room_id` BIGINT,
    `barcode` VARCHAR(100), -- Linked barcode
    `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`hostel_id`) REFERENCES `hostels`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`room_id`) REFERENCES `rooms`(`id`) ON DELETE SET NULL,
    UNIQUE KEY `unique_roll_per_college` (`college_id`, `roll_number`),
    UNIQUE KEY `unique_barcode_per_college` (`college_id`, `barcode`)
) ENGINE=InnoDB;

-- 6. Meals Table
CREATE TABLE IF NOT EXISTS `meals` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `meal_name` VARCHAR(50) NOT NULL, -- Breakfast, Lunch, Dinner
    `start_time` TIME NOT NULL,
    `end_time` TIME NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_meal_name_per_college` (`college_id`, `meal_name`)
) ENGINE=InnoDB;

-- 7. Barcode Registration Table
CREATE TABLE IF NOT EXISTS `barcode_registrations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL UNIQUE,
    `barcode_value` VARCHAR(100) NOT NULL,
    `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `registered_by` BIGINT,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`registered_by`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    UNIQUE KEY `unique_barcode_reg_per_college` (`college_id`, `barcode_value`)
) ENGINE=InnoDB;

-- 8. Attendance Table
CREATE TABLE IF NOT EXISTS `attendance` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    `meal_id` BIGINT NOT NULL,
    `scan_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `attendance_date` DATE NOT NULL,
    `status` VARCHAR(50) NOT NULL, -- PRESENT, DUPLICATE_ATTEMPT
    `warden_id` BIGINT,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`meal_id`) REFERENCES `meals`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`warden_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_attendance_lookup` (`college_id`, `attendance_date`, `meal_id`, `student_id`)
) ENGINE=InnoDB;

-- 9. Notifications Table
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` TEXT NOT NULL,
    `read_status` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Reports Metadata/Log Table
CREATE TABLE IF NOT EXISTS `reports` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL,
    `report_type` VARCHAR(50) NOT NULL, -- DAILY, MONTHLY, MEAL_UTILIZATION
    `generated_by` BIGINT,
    `file_path` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `colleges`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`generated_by`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- CREATE INDEXES FOR OPTIMAL MULTI-TENANCY PERFORMANCE
CREATE INDEX `idx_users_tenant` ON `users` (`college_id`);
CREATE INDEX `idx_students_tenant` ON `students` (`college_id`, `roll_number`);
CREATE INDEX `idx_students_barcode` ON `students` (`college_id`, `barcode`);
CREATE INDEX `idx_hostels_tenant` ON `hostels` (`college_id`);
CREATE INDEX `idx_rooms_tenant` ON `rooms` (`college_id`);
CREATE INDEX `idx_meals_tenant` ON `meals` (`college_id`);
CREATE INDEX `idx_attendance_tenant` ON `attendance` (`college_id`);
CREATE INDEX `idx_barcode_reg_tenant` ON `barcode_registrations` (`college_id`);
CREATE INDEX `idx_notifications_tenant` ON `notifications` (`college_id`);
CREATE INDEX `idx_reports_tenant` ON `reports` (`college_id`);
