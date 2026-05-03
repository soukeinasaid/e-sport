-- Database Setup Script for Smoka Forum Application
-- Run this script in MySQL to create the required database and tables

-- Create the database
CREATE DATABASE IF NOT EXISTS smoka CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE smoka;

-- Create utilisateur table for users
CREATE TABLE IF NOT EXISTS utilisateur (
    idUser INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    motDePasse VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create forum table for posts
CREATE TABLE IF NOT EXISTS forum (
    idForum INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idUser INT NOT NULL,
    FOREIGN KEY (idUser) REFERENCES utilisateur(idUser) ON DELETE CASCADE
);

-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    idFavorite INT AUTO_INCREMENT PRIMARY KEY,
    idForum INT NOT NULL,
    idUser INT NOT NULL,
    dateAdded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idForum) REFERENCES forum(idForum) ON DELETE CASCADE,
    FOREIGN KEY (idUser) REFERENCES utilisateur(idUser) ON DELETE CASCADE,
    UNIQUE KEY unique_favorite (idForum, idUser)
);

-- Insert some sample data for testing
INSERT INTO utilisateur (nom, prenom, email, motDePasse) VALUES
('Admin', 'User', 'admin@smoka.com', 'admin123'),
('John', 'Doe', 'john@example.com', 'password123'),
('Jane', 'Smith', 'jane@example.com', 'password123');

INSERT INTO forum (titre, description, idUser) VALUES
('Welcome to Smoka Forum!', 'This is the first post in our forum. Feel free to share your thoughts and ideas.', 1),
('How to use this forum', 'You can create, edit, and delete posts. Use the search bar to find specific topics.', 1),
('Introduction Thread', 'Hello everyone! I''m new here and excited to be part of this community.', 2),
('Technical Discussion', 'Let''s discuss the latest technologies and trends in software development.', 3);

-- Insert some sample favorites data
INSERT INTO favorites (idForum, idUser) VALUES
(1, 2), -- John Doe favorites the welcome post
(1, 3), -- Jane Smith favorites the welcome post
(3, 1), -- Admin favorites the introduction thread
(4, 2); -- John Doe favorites the technical discussion

-- Show the created tables
SHOW TABLES;

-- Show sample data
SELECT * FROM utilisateur;
SELECT * FROM forum;
SELECT * FROM favorites;
