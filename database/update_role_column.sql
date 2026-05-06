-- Update existing database to add role column
-- Run this script if you already have the database set up

USE smoka;

-- Add role column if it doesn't exist
ALTER TABLE utilisateur 
ADD COLUMN IF NOT EXISTS role ENUM('USER', 'ADMIN') DEFAULT 'USER' AFTER motDePasse;

-- Update existing admin user to have ADMIN role
UPDATE utilisateur 
SET role = 'ADMIN' 
WHERE email = 'admin@smoka.com';

-- Show the updated table structure
DESCRIBE utilisateur;

-- Show all users with their roles
SELECT idUser, nom, prenom, email, role, created_at FROM utilisateur;
