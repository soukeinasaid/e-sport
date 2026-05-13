-- ============================================================
--  esport_db — Centre + Tournoi schema
--  Run this in phpMyAdmin or any MySQL client connected to
--  the `esport_db` database.
-- ============================================================

-- Create the database (skip if it already exists)
CREATE DATABASE IF NOT EXISTS esport_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE esport_db;

-- ────────────────────────────────────────────────────────────
--  TABLE: users
--  Stores back-office admins and front-office users.
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id         INT          NOT NULL AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,           -- plain-text (simple demo)
    role       ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample accounts (change passwords before production!)
INSERT IGNORE INTO users (username, email, password, role) VALUES
('admin',   'admin@esport.tn',  'admin123',  'ADMIN'),
('joueur1', 'joueur1@mail.com', 'pass1234',  'USER'),
('joueur2', 'joueur2@mail.com', 'pass1234',  'USER');

-- ────────────────────────────────────────────────────────────
--  TABLE: centers
--  One Centre  →  Many Tournois  (parent side)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS centers (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    address       VARCHAR(255)          DEFAULT NULL,
    city          VARCHAR(100) NOT NULL,
    contact_email VARCHAR(150)          DEFAULT NULL,
    map_url       VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────
--  TABLE: tournoi
--  Many Tournois  →  One Centre  (child side)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tournoi (
    id          INT            NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(150)   NOT NULL,
    jeu         VARCHAR(100)   NOT NULL,
    date_debut  DATE           NOT NULL,
    date_fin    DATE           NOT NULL,
    lieu        VARCHAR(150)   NOT NULL,
    prix        DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    nb_equipes  INT            NOT NULL DEFAULT 8,
    centre_id   INT                     DEFAULT NULL,  -- FK → centers.id
    PRIMARY KEY (id),
    CONSTRAINT fk_tournoi_centre
        FOREIGN KEY (centre_id)
        REFERENCES centers (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL          -- keep the tournament if its centre is deleted
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────
--  ADD centre_id TO AN EXISTING tournoi TABLE
--  Safe: only adds the column if it does not already exist.
-- ────────────────────────────────────────────────────────────
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tournoi'
      AND COLUMN_NAME  = 'centre_id'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE tournoi
         ADD COLUMN centre_id INT DEFAULT NULL AFTER nb_equipes,
         ADD CONSTRAINT fk_tournoi_centre
             FOREIGN KEY (centre_id) REFERENCES centers (id)
             ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT ''centre_id already exists — skipped'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ────────────────────────────────────────────────────────────
--  SAMPLE DATA — Centres
-- ────────────────────────────────────────────────────────────
INSERT IGNORE INTO centers (id, name, address, city, contact_email, map_url) VALUES
(1, 'Cyber Arena Tunis',    'Av. Habib Bourguiba 42', 'Tunis',   'contact@cyberarena.tn',   'https://maps.google.com/?q=Tunis'),
(2, 'GameZone Paris',       '18 rue de Rivoli',       'Paris',   'info@gamezone.fr',         'https://maps.google.com/?q=Paris'),
(3, 'eSport Hub Sousse',    'Cité Erriadh Bloc B',    'Sousse',  'hub@esport-sousse.tn',     'https://maps.google.com/?q=Sousse'),
(4, 'Pixel Palace Sfax',    'Rue Farhat Hached 7',    'Sfax',    'pixel@palace-sfax.tn',     'https://maps.google.com/?q=Sfax'),
(5, 'Arena Lyon',           'Place Bellecour 3',      'Lyon',    'admin@arena-lyon.fr',      'https://maps.google.com/?q=Lyon');

-- ────────────────────────────────────────────────────────────
--  SAMPLE DATA — Tournois (linked to centres)
-- ────────────────────────────────────────────────────────────
INSERT IGNORE INTO tournoi (nom, jeu, date_debut, date_fin, lieu, prix, nb_equipes, centre_id) VALUES
('Winter Cup 2025',      'Valorant',          '2025-12-01', '2025-12-05', 'Tunis',   5000.00, 16, 1),
('Spring Clash',         'League of Legends', '2025-03-10', '2025-03-12', 'Paris',   3000.00, 8,  2),
('CS2 Open Sousse',      'CS2',               '2025-06-20', '2025-06-22', 'Sousse',  2500.00, 16, 3),
('Tekken Championship',  'Tekken 8',          '2025-09-14', '2025-09-15', 'Sfax',    1000.00, 32, 4),
('Rocket League Finals', 'Rocket League',     '2025-11-01', '2025-11-03', 'Lyon',    4000.00, 8,  5);

-- ────────────────────────────────────────────────────────────
--  TABLE: teams
--  Many Teams → One Tournoi
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS teams (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    tournoi_id  INT          NOT NULL,
    captain_id  INT          NOT NULL,  -- FK → users.id
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_team_tournoi
        FOREIGN KEY (tournoi_id) REFERENCES tournoi (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_team_captain
        FOREIGN KEY (captain_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ────────────────────────────────────────────────────────────
--  TABLE: team_members
--  Many Users → Many Teams (junction table)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS team_members (
    team_id     INT NOT NULL,
    user_id     INT NOT NULL,
    joined_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_member_team
        FOREIGN KEY (team_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_team_member_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ───────────────────────────────────────────────────────────
--  TABLE: matches
--  Matches for tournament brackets
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS matches (
    id          INT NOT NULL AUTO_INCREMENT,
    tournoi_id  INT NOT NULL,
    round       INT NOT NULL,  -- e.g., 1 for round of 16, 2 for quarterfinals, etc.
    match_order INT NOT NULL,  -- order within the round
    team1_id    INT DEFAULT NULL,
    team2_id    INT DEFAULT NULL,
    winner_id   INT DEFAULT NULL,  -- FK → teams.id
    status      ENUM('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
    scheduled_at TIMESTAMP DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_match_tournoi
        FOREIGN KEY (tournoi_id) REFERENCES tournoi (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_match_team1
        FOREIGN KEY (team1_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_match_team2
        FOREIGN KEY (team2_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_match_winner
        FOREIGN KEY (winner_id) REFERENCES teams (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
