-- TerraGauge Database Setup Script
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS terragauge_db;
USE terragauge_db;

-- Drop dependent tables first to ensure clean execution
DROP TABLE IF EXISTS impact_records;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Activities Table
CREATE TABLE activities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(30) NOT NULL,
    activity_name VARCHAR(100) NOT NULL,
    quantity DOUBLE NOT NULL,
    unit VARCHAR(20) NOT NULL,
    activity_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- 3. Impact Records Table
CREATE TABLE impact_records (
    impact_id INT AUTO_INCREMENT PRIMARY KEY,
    activity_id INT NOT NULL UNIQUE,
    emission_factor DOUBLE NOT NULL,
    co2e_kg DOUBLE NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_factor_non_negative CHECK (emission_factor >= 0),
    CONSTRAINT chk_co2e_non_negative CHECK (co2e_kg >= 0),
    CONSTRAINT fk_impact_activity FOREIGN KEY (activity_id) 
        REFERENCES activities(activity_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Indices to accelerate joins and aggregation filters
CREATE INDEX idx_activities_user_date ON activities (user_id, activity_date);
CREATE INDEX idx_activities_category ON activities (category);

-- Default seed data for development and quick evaluation
INSERT INTO users (user_id, username, email) VALUES 
(1, 'eco_user', 'eco_user@terragauge.local')
ON DUPLICATE KEY UPDATE username = VALUES(username);