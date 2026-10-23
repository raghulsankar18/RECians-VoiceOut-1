-- =====================================================================
-- RECian's VoiceOut - Database Schema
-- Rajalakshmi Engineering College (REC)
-- Target Database: MySQL 8.0+
-- =====================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS recians_voiceout
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE recians_voiceout;

-- 2. Drop existing tables in reverse dependency order (safe for schema refresh)
DROP VIEW IF EXISTS public_complaints_view;
DROP TABLE IF EXISTS complaint_supports;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS students;

-- =====================================================================
-- 3. Students Table
-- Stores student accounts, profile info, and secure password hashes.
-- College email must end with @rajalakshmi.edu.in.
-- =====================================================================
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    register_number VARCHAR(30) UNIQUE,
    department VARCHAR(50),
    academic_year VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    profile_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_email (email),
    INDEX idx_student_regno (register_number)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. Admins Table
-- Stores authorized college administrator credentials.
-- Passwords must be SHA-256 hashed (never plain text).
-- No hardcoded default account is inserted here for security.
-- =====================================================================
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(50) DEFAULT 'Administrator',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_username (username)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. Complaint Categories Table
-- Stores the 8 mandated categories for Rajalakshmi Engineering College.
-- Includes a flag for Private & Confidential issues.
-- =====================================================================
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    is_confidential BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Pre-populate the 8 required categories
INSERT INTO categories (category_name, description, is_confidential) VALUES
('Hostel Facilities', 'Hostel rooms, mess, water, electricity, and maintenance concerns', FALSE),
('Canteen and Food', 'Food quality, hygiene, pricing, and cafeteria services', FALSE),
('Faculty and Academic Concerns', 'Curriculum, exams, teaching, lab sessions, and academic guidance', FALSE),
('Student Welfare', 'Anti-ragging, safety, mental health, student rights, and well-being', FALSE),
('College Facilities', 'Wi-Fi, library, printout machines, sports, and common amenities', FALSE),
('Classrooms and Infrastructure', 'Projectors, air-conditioning, seating, labs, and building facilities', FALSE),
('Transport and Parking', 'College buses, routing, vehicle parking, and commuting issues', FALSE),
('Private & Confidential', 'Sensitive individual concerns, bullying, harassment, and private grievances', TRUE);

-- =====================================================================
-- 6. Complaints Table
-- Stores submitted grievances. Each receives a unique complaint code.
-- Initial status is 'Pending'.
-- Statuses: Pending, Under Review, In Progress, Resolved, Rejected.
-- =====================================================================
CREATE TABLE complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_code VARCHAR(30) NOT NULL UNIQUE,
    student_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('Pending', 'Under Review', 'In Progress', 'Resolved', 'Rejected') DEFAULT 'Pending',
    is_confidential BOOLEAN DEFAULT FALSE,
    support_count INT DEFAULT 0,
    admin_remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_complaints_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id),
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_category (category_id),
    INDEX idx_complaint_confidential (is_confidential),
    INDEX idx_complaint_student (student_id)
) ENGINE=InnoDB;

-- =====================================================================
-- 7. Complaint Supports Table (1 Student = 1 Support per complaint)
-- Enforces uniqueness using (complaint_id, student_id).
-- Confidential complaints must never be supported.
-- =====================================================================
CREATE TABLE complaint_supports (
    support_id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    student_id INT NOT NULL,
    supported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supports_complaint FOREIGN KEY (complaint_id)
        REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    CONSTRAINT fk_supports_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_complaint_support UNIQUE (complaint_id, student_id),
    INDEX idx_support_complaint (complaint_id),
    INDEX idx_support_student (student_id)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. Public Complaints View
-- Strict security layer: Filters out confidential complaints completely.
-- Anonymous / department-level visibility for community grievances.
-- =====================================================================
CREATE OR REPLACE VIEW public_complaints_view AS
SELECT 
    c.complaint_id,
    c.complaint_code,
    c.title,
    c.description,
    c.category_id,
    cat.category_name,
    c.status,
    c.support_count,
    c.created_at,
    s.department,
    s.academic_year
FROM complaints c
JOIN categories cat ON c.category_id = cat.category_id
JOIN students s ON c.student_id = s.student_id
WHERE c.is_confidential = FALSE 
  AND cat.is_confidential = FALSE;

-- ==========================================================
-- 9. Suggestions Table (parallel to complaints)
-- ==========================================================
CREATE TABLE IF NOT EXISTS suggestions (
    suggestion_id INT AUTO_INCREMENT PRIMARY KEY,
    suggestion_code VARCHAR(30) NOT NULL UNIQUE,
    student_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('Pending','Reviewed','Implemented') DEFAULT 'Pending',
    support_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_suggestions_student FOREIGN KEY (student_id) 
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_suggestions_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id),
    INDEX idx_suggestion_status (status),
    INDEX idx_suggestion_category (category_id),
    INDEX idx_suggestion_student (student_id)
) ENGINE=InnoDB;

-- Suggestion Supports (1‑student‑1‑support per suggestion)
CREATE TABLE IF NOT EXISTS suggestion_supports (
    support_id INT AUTO_INCREMENT PRIMARY KEY,
    suggestion_id INT NOT NULL,
    student_id INT NOT NULL,
    supported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supports_suggestion FOREIGN KEY (suggestion_id)
        REFERENCES suggestions(suggestion_id) ON DELETE CASCADE,
    CONSTRAINT fk_supports_student_sug FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_suggestion_support UNIQUE (suggestion_id, student_id),
    INDEX idx_support_suggestion (suggestion_id),
    INDEX idx_support_student_sug (student_id)
) ENGINE=InnoDB;
