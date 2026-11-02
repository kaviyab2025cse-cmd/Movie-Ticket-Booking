-- =====================================================================
-- CineBook - Movie Ticket Booking System
-- Database Schema for MySQL 8.0+
-- Course: OOPs and DBMS Project
-- Features: 5 Tamil Movies Showcase
-- =====================================================================

-- 1. Create and select Database
CREATE DATABASE IF NOT EXISTS cinebook_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cinebook_db;

-- 2. Drop existing tables in reverse foreign key order
DROP TABLE IF EXISTS booking_seats;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS shows;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS coupons;

-- ---------------------------------------------------------------------
-- Table: movies
-- Stores movie details (Entity)
-- ---------------------------------------------------------------------
CREATE TABLE movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL UNIQUE,
    duration_mins INT NOT NULL,
    genre VARCHAR(50) NOT NULL,
    language VARCHAR(30) NOT NULL DEFAULT 'Tamil',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: shows
-- Stores showtimes for movies in the theatre (Entity & Relationship)
-- ---------------------------------------------------------------------
CREATE TABLE shows (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    show_time TIME NOT NULL,
    theatre_name VARCHAR(100) NOT NULL DEFAULT 'CineBook Cinema, Chennai',
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    UNIQUE KEY uk_movie_showtime (movie_id, show_time)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: seats
-- Stores the 5x8 seat grid for every individual show
-- Rows: A to E (5 rows), Columns: 1 to 8 (8 columns) = 40 seats per show
-- ---------------------------------------------------------------------
CREATE TABLE seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_code VARCHAR(5) NOT NULL,           -- e.g. 'A1', 'C3', 'E8'
    row_letter CHAR(1) NOT NULL,             -- 'A', 'B', 'C', 'D', 'E'
    column_number INT NOT NULL,              -- 1 to 8
    seat_type VARCHAR(20) NOT NULL,          -- 'REGULAR', 'PREMIUM', 'VIP'
    price DECIMAL(8, 2) NOT NULL,            -- Regular: 150.00, Premium: 250.00, VIP: 400.00
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,-- FALSE (Available: O), TRUE (Booked: X)
    FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE CASCADE,
    UNIQUE KEY uk_show_seat (show_id, seat_code),
    CONSTRAINT chk_row CHECK (row_letter IN ('A', 'B', 'C', 'D', 'E')),
    CONSTRAINT chk_col CHECK (column_number BETWEEN 1 AND 8)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: coupons
-- Stores discount coupons
-- ---------------------------------------------------------------------
CREATE TABLE coupons (
    coupon_code VARCHAR(20) PRIMARY KEY,
    discount_percent INT NOT NULL CHECK (discount_percent BETWEEN 1 AND 100),
    description VARCHAR(150) NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: bookings
-- Stores customer ticket booking transactions (CRUD operations)
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    booking_id VARCHAR(15) PRIMARY KEY,       -- e.g. 'BK1001', 'BK1002'
    show_id INT NOT NULL,
    movie_title VARCHAR(150) NOT NULL,
    show_time_str VARCHAR(20) NOT NULL,
    seat_codes VARCHAR(100) NOT NULL,        -- comma separated, e.g. 'C3, C4'
    base_amount DECIMAL(8, 2) NOT NULL,
    coupon_code VARCHAR(20) NULL,
    discount_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(8, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
    refund_amount DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE RESTRICT,
    FOREIGN KEY (coupon_code) REFERENCES coupons(coupon_code) ON DELETE SET NULL,
    CONSTRAINT chk_status CHECK (status IN ('CONFIRMED', 'CANCELLED', 'PENDING'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table: booking_seats
-- Bridge entity mapping bookings to individual reserved seats
-- ---------------------------------------------------------------------
CREATE TABLE booking_seats (
    booking_id VARCHAR(15) NOT NULL,
    seat_code VARCHAR(5) NOT NULL,
    PRIMARY KEY (booking_id, seat_code),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- Initial Seed Data Population - 5 Tamil Movies
-- =====================================================================

-- Insert 5 Tamil Movies
INSERT INTO movies (movie_id, title, duration_mins, genre, language) VALUES
(1, 'Baattha (Tamil)', 142, 'Comedy / Crime / Drama', 'Tamil'),
(2, 'Dorothy (Tamil)', 154, 'Crime / Drama', 'Tamil'),
(3, 'Meesaya Murukku 2 (Tamil)', 148, 'Musical / Comedy / Drama', 'Tamil'),
(4, 'Jailer 2 (Tamil)', 168, 'Action / Crime / Thriller', 'Tamil'),
(5, 'Mandaadi (Tamil)', 138, 'Sports / Action / Drama', 'Tamil');

-- Insert Shows for the 5 Tamil Movies
INSERT INTO shows (show_id, movie_id, show_time, theatre_name) VALUES
(1, 1, '10:30:00', 'CineBook Cinema, Chennai'),
(2, 1, '18:45:00', 'CineBook Cinema, Chennai'),
(3, 2, '13:45:00', 'CineBook Cinema, Chennai'),
(4, 2, '19:15:00', 'CineBook Cinema, Chennai'),
(5, 3, '11:15:00', 'CineBook Cinema, Chennai'),
(6, 4, '19:00:00', 'CineBook Cinema, Chennai'),
(7, 5, '20:30:00', 'CineBook Cinema, Chennai');

-- Insert Coupons
INSERT INTO coupons (coupon_code, discount_percent, description) VALUES
('CINE10', 10, '10% off entire booking (max discount)');

-- Populate 40 seats (5 rows x 8 columns) for each of the 7 shows
DELIMITER //
CREATE PROCEDURE PopulateAllShowSeats()
BEGIN
    DECLARE s_id INT DEFAULT 1;
    DECLARE r INT DEFAULT 0;
    DECLARE c INT DEFAULT 1;
    DECLARE r_letter CHAR(1);
    DECLARE s_type VARCHAR(20);
    DECLARE s_price DECIMAL(8,2);
    DECLARE s_code VARCHAR(5);

    WHILE s_id <= 7 DO
        SET r = 0;
        WHILE r < 5 DO
            SET r_letter = SUBSTRING('ABCDE', r + 1, 1);
            IF r_letter IN ('A', 'B') THEN
                SET s_type = 'REGULAR';
                SET s_price = 150.00;
            ELSEIF r_letter IN ('C', 'D') THEN
                SET s_type = 'PREMIUM';
                SET s_price = 250.00;
            ELSE
                SET s_type = 'VIP';
                SET s_price = 400.00;
            END IF;

            SET c = 1;
            WHILE c <= 8 DO
                SET s_code = CONCAT(r_letter, c);
                INSERT INTO seats (show_id, seat_code, row_letter, column_number, seat_type, price, is_booked)
                VALUES (s_id, s_code, r_letter, c, s_type, s_price, FALSE);
                SET c = c + 1;
            END WHILE;
            SET r = r + 1;
        END WHILE;
        SET s_id = s_id + 1;
    END WHILE;
END //
DELIMITER ;

CALL PopulateAllShowSeats();
DROP PROCEDURE PopulateAllShowSeats;

-- Verify setup
SELECT 'Database schema and Tamil movie seed data created successfully!' AS Result;
SELECT COUNT(*) AS total_shows FROM shows;
SELECT COUNT(*) AS total_seats FROM seats;
