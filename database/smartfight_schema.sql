-- ============================================================
-- FILE: database/smartfight_schema.sql
-- PROJECT: SmartFight — Combat Sports Event & AI Matchmaking
-- ALL 5 MODULES — COMPLETE GLOBAL SCHEMA
-- DATABASE: smartfight
-- MYSQL: 8.x
-- ENCODING: utf8mb4
-- ============================================================
-- MODULE 1 — Event Management Core         (nassir)
-- MODULE 2 — Fighter & Coach Management    (ayoub)
-- MODULE 3 — AI Matchmaking & Rankings     (ahmed aissa)
-- MODULE 4 — Fight Results & Analytics     (mahdi)
-- MODULE 5 — Fan Experience & Auth         (ahmed ismail)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS smartfight;
CREATE DATABASE smartfight
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartfight;


-- ============================================================
-- MODULE 5 — USER & AUTH
-- Must be created FIRST — all other modules depend on user.id
-- Tables: user_role, user, admin, fan_profile
-- ============================================================

CREATE TABLE user_role (
    id          INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user (
    id            INT          NOT NULL AUTO_INCREMENT,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    role_id       INT          NOT NULL,
    is_active     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_email (email),
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
        REFERENCES user_role(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin (
    id            INT          NOT NULL AUTO_INCREMENT,
    user_id       INT          NOT NULL,
    access_level  ENUM('SUPER','STANDARD') NOT NULL DEFAULT 'STANDARD',
    department    VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_admin_user (user_id),
    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fan_profile (
    id              INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    favorite_sport  VARCHAR(100) DEFAULT NULL,
    country         VARCHAR(100) DEFAULT NULL,
    bio             TEXT         DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fan_user (user_id),
    CONSTRAINT fk_fan_user
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- MODULE 1 — EVENT MANAGEMENT CORE
-- Tables: venue, discipline, event, event_schedule
-- Cross-module: event.organizer_id → user.id (Module 5)
-- ============================================================

CREATE TABLE venue (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    country       VARCHAR(100) NOT NULL DEFAULT 'Tunisia',
    capacity      INT          NOT NULL DEFAULT 0,
    contact_email VARCHAR(100) DEFAULT NULL,
    contact_phone VARCHAR(20)  DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE discipline (
    id             INT          NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    description    TEXT         DEFAULT NULL,
    weight_class   VARCHAR(60)  DEFAULT NULL,
    round_duration INT          NOT NULL DEFAULT 3,
    max_rounds     INT          NOT NULL DEFAULT 3,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_discipline_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL,
    description   TEXT         DEFAULT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    status        ENUM('SCHEDULED','ONGOING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    visibility    ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    capacity      INT          NOT NULL DEFAULT 0,
    venue_id      INT          DEFAULT NULL,
    discipline_id INT          DEFAULT NULL,
    organizer_id  INT          DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_venue
        FOREIGN KEY (venue_id)
        REFERENCES venue(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_event_discipline
        FOREIGN KEY (discipline_id)
        REFERENCES discipline(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_event_organizer
        FOREIGN KEY (organizer_id)
        REFERENCES user(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_schedule (
    id             INT          NOT NULL AUTO_INCREMENT,
    event_id       INT          NOT NULL,
    title          VARCHAR(200) NOT NULL,
    scheduled_time DATETIME     NOT NULL,
    duration_min   INT          NOT NULL DEFAULT 60,
    notes          TEXT         DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_event
        FOREIGN KEY (event_id)
        REFERENCES event(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- MODULE 2 — FIGHTER & COACH MANAGEMENT
-- Tables: weight_class, fighter, coach, fighter_coach, event_fighter
-- Cross-module: fighter.user_id → user.id (Module 5)
--               coach.user_id  → user.id  (Module 5)
--               event_fighter  → event.id (Module 1)
-- ============================================================

CREATE TABLE weight_class (
    id         INT           NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)   NOT NULL,
    min_weight DECIMAL(5,2)  NOT NULL,
    max_weight DECIMAL(5,2)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_weight_class_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fighter (
    id              INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    nickname        VARCHAR(100) DEFAULT NULL,
    date_of_birth   DATE         DEFAULT NULL,
    nationality     VARCHAR(100) DEFAULT NULL,
    weight_class_id INT          DEFAULT NULL,
    wins            INT          NOT NULL DEFAULT 0,
    losses          INT          NOT NULL DEFAULT 0,
    draws           INT          NOT NULL DEFAULT 0,
    status          ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fighter_user (user_id),
    CONSTRAINT fk_fighter_user
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_fighter_weight
        FOREIGN KEY (weight_class_id)
        REFERENCES weight_class(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE coach (
    id               INT          NOT NULL AUTO_INCREMENT,
    user_id          INT          NOT NULL,
    speciality       VARCHAR(150) DEFAULT NULL,
    experience_years INT          NOT NULL DEFAULT 0,
    certification    VARCHAR(200) DEFAULT NULL,
    status           ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_coach_user (user_id),
    CONSTRAINT fk_coach_user
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fighter_coach (
    id         INT        NOT NULL AUTO_INCREMENT,
    fighter_id INT        NOT NULL,
    coach_id   INT        NOT NULL,
    start_date DATE       NOT NULL,
    end_date   DATE       DEFAULT NULL,
    is_active  TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fighter_coach (fighter_id, coach_id),
    CONSTRAINT fk_fc_fighter
        FOREIGN KEY (fighter_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_fc_coach
        FOREIGN KEY (coach_id)
        REFERENCES coach(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_fighter (
    id                INT           NOT NULL AUTO_INCREMENT,
    event_id          INT           NOT NULL,
    fighter_id        INT           NOT NULL,
    registration_date DATE          NOT NULL DEFAULT (CURRENT_DATE),
    weight_at_event   DECIMAL(5,2)  DEFAULT NULL,
    corner            ENUM('RED','BLUE') NOT NULL DEFAULT 'RED',
    PRIMARY KEY (id),
    UNIQUE KEY uq_event_fighter (event_id, fighter_id),
    CONSTRAINT fk_ef_event
        FOREIGN KEY (event_id)
        REFERENCES event(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_ef_fighter
        FOREIGN KEY (fighter_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- MODULE 3 — AI MATCHMAKING & RANKINGS
-- Tables: matchmaking_rule, match_proposal, ranking, performance_score
-- Cross-module: fighter.id (Module 2), event.id (Module 1)
-- ============================================================

CREATE TABLE matchmaking_rule (
    id          INT           NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)  NOT NULL,
    description TEXT          DEFAULT NULL,
    weight      DECIMAL(4,2)  NOT NULL DEFAULT 1.00,
    is_active   TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_proposal (
    id            INT           NOT NULL AUTO_INCREMENT,
    event_id      INT           DEFAULT NULL,
    fighter1_id   INT           NOT NULL,
    fighter2_id   INT           NOT NULL,
    compatibility DECIMAL(5,2)  DEFAULT NULL,
    status        ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
    proposed_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes         TEXT          DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mp_event
        FOREIGN KEY (event_id)
        REFERENCES event(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_mp_fighter1
        FOREIGN KEY (fighter1_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_mp_fighter2
        FOREIGN KEY (fighter2_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ranking (
    id             INT           NOT NULL AUTO_INCREMENT,
    fighter_id     INT           NOT NULL,
    discipline_id  INT           DEFAULT NULL,
    rank_position  INT           NOT NULL DEFAULT 0,
    points         DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
    season         VARCHAR(20)   NOT NULL DEFAULT '2026',
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_ranking (fighter_id, discipline_id, season),
    CONSTRAINT fk_rank_fighter
        FOREIGN KEY (fighter_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_rank_discipline
        FOREIGN KEY (discipline_id)
        REFERENCES discipline(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE performance_score (
    id            INT           NOT NULL AUTO_INCREMENT,
    fighter_id    INT           NOT NULL,
    score         DECIMAL(6,2)  NOT NULL DEFAULT 0.00,
    aggression    DECIMAL(4,2)  DEFAULT NULL,
    defense       DECIMAL(4,2)  DEFAULT NULL,
    technique     DECIMAL(4,2)  DEFAULT NULL,
    experience    DECIMAL(4,2)  DEFAULT NULL,
    calculated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ps_fighter
        FOREIGN KEY (fighter_id)
        REFERENCES fighter(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- MODULE 4 — FIGHT RESULTS & ANALYTICS
-- Tables: fight_result, fight_statistic, judge_score
-- Cross-module: event.id (M1), fighter.id (M2), match_proposal.id (M3)
-- ============================================================

CREATE TABLE fight_result (
    id              INT      NOT NULL AUTO_INCREMENT,
    event_id        INT      NOT NULL,
    match_id        INT      DEFAULT NULL,
    fighter_red_id  INT      NOT NULL,
    fighter_blue_id INT      NOT NULL,
    winner_id       INT      DEFAULT NULL,
    method          ENUM('KO','TKO','SUBMISSION','DECISION','DRAW','NO_CONTEST') NOT NULL,
    round_ended     INT      DEFAULT NULL,
    time_ended      TIME     DEFAULT NULL,
    notes           TEXT     DEFAULT NULL,
    fight_date      DATE     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_fr_event
        FOREIGN KEY (event_id)
        REFERENCES event(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_fr_match
        FOREIGN KEY (match_id)
        REFERENCES match_proposal(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_fr_red
        FOREIGN KEY (fighter_red_id)
        REFERENCES fighter(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_fr_blue
        FOREIGN KEY (fighter_blue_id)
        REFERENCES fighter(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_fr_winner
        FOREIGN KEY (winner_id)
        REFERENCES fighter(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fight_statistic (
    id              INT NOT NULL AUTO_INCREMENT,
    fight_result_id INT NOT NULL,
    fighter_id      INT NOT NULL,
    strikes_landed  INT NOT NULL DEFAULT 0,
    strikes_thrown  INT NOT NULL DEFAULT 0,
    takedowns       INT NOT NULL DEFAULT 0,
    submissions     INT NOT NULL DEFAULT 0,
    knockdowns      INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uq_stat (fight_result_id, fighter_id),
    CONSTRAINT fk_fs_result
        FOREIGN KEY (fight_result_id)
        REFERENCES fight_result(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_fs_fighter
        FOREIGN KEY (fighter_id)
        REFERENCES fighter(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE judge_score (
    id              INT          NOT NULL AUTO_INCREMENT,
    fight_result_id INT          NOT NULL,
    judge_name      VARCHAR(100) NOT NULL,
    score_red       INT          NOT NULL DEFAULT 0,
    score_blue      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_js_result
        FOREIGN KEY (fight_result_id)
        REFERENCES fight_result(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- SEED DATA — MODULE 5
-- ============================================================
INSERT INTO user_role (name, description) VALUES
('ADMIN',     'Full system access'),
('ORGANIZER', 'Can manage events'),
('FIGHTER',   'Registered fighter'),
('COACH',     'Fighter coach'),
('FAN',       'Public viewer');

INSERT INTO user (first_name, last_name, email, password, phone, role_id) VALUES
('Nassir',  'Admin',    'nassir@smartfight.tn',   'hashed_pwd_1',  '+216 20 111 001', 1),
('Ayoub',   'Org',      'ayoub@smartfight.tn',    'hashed_pwd_2',  '+216 20 111 002', 2),
('Ali',     'Hammami',  'ali@smartfight.tn',       'hashed_pwd_3',  '+216 20 111 003', 3),
('Mohamed', 'Trabelsi', 'mohamed@smartfight.tn',   'hashed_pwd_4',  '+216 20 111 004', 3),
('Karim',   'Coach',    'karim@smartfight.tn',     'hashed_pwd_5',  '+216 20 111 005', 4),
('Sana',    'Fan',      'sana@smartfight.tn',      'hashed_pwd_6',  '+216 20 111 006', 5),
('Omar',    'Belhaj',   'omar@smartfight.tn',      'hashed_pwd_7',  '+216 20 111 007', 3),
('Hana',    'Mansour',  'hana@smartfight.tn',      'hashed_pwd_8',  '+216 20 111 008', 3),
('Youssef', 'Coach2',   'youssef@smartfight.tn',   'hashed_pwd_9',  '+216 20 111 009', 4),
('Leila',   'Fan2',     'leila@smartfight.tn',     'hashed_pwd_10', '+216 20 111 010', 5);

INSERT INTO admin (user_id, access_level, department) VALUES
(1, 'SUPER', 'System Administration');

INSERT INTO fan_profile (user_id, favorite_sport, country, bio) VALUES
(6,  'Boxing', 'Tunisia', 'Huge boxing fan since 2010'),
(10, 'MMA',    'Tunisia', 'Following MMA since UFC 200');


-- ============================================================
-- SEED DATA — MODULE 1
-- ============================================================
INSERT INTO venue (name, address, city, country, capacity, contact_email, contact_phone) VALUES
('Salle Omnisports de Tunis',  'Avenue Habib Bourguiba', 'Tunis',    'Tunisia', 5000, 'contact@sot.tn',     '+216 71 100 001'),
('Palais des Sports de Sfax',  'Rue du Sport',           'Sfax',     'Tunisia', 3200, 'info@palaissfax.tn', '+216 74 200 002'),
('Mohamed Ali Sports Hall',    'Place Mohamed Ali',      'Sousse',   'Tunisia', 2500, 'admin@mas.tn',       '+216 73 300 003'),
('Salle Polyvalente Monastir', 'Avenue 7 Novembre',      'Monastir', 'Tunisia', 1800, 'contact@spm.tn',     '+216 73 400 004'),
('Cairo International Arena',  '6th of October City',   'Cairo',    'Egypt',   8000, 'info@cairoarena.eg', '+20 2 5500 050');

INSERT INTO discipline (name, description, weight_class, round_duration, max_rounds) VALUES
('Boxing',     'Classic boxing — unified rules',              'Heavyweight',  3, 12),
('MMA',        'Mixed Martial Arts — Unified Rules',          'Lightweight',  5,  5),
('Judo',       'IJF rules — randori and shiai format',        'Open',         5,  1),
('Kickboxing', 'K-1 ruleset — punches and kicks above waist', 'Middleweight', 3,  5),
('Wrestling',  'Freestyle — FILA international rules',        'Welterweight', 2,  3);

INSERT INTO event (name, description, start_date, end_date, status, visibility, capacity, venue_id, discipline_id, organizer_id) VALUES
('SmartFight Open 2026',     'Annual open boxing championship',          '2026-04-10', '2026-04-12', 'SCHEDULED', 'PUBLIC',  4500, 1, 1, 2),
('MMA Night Sfax 2026',      'Professional MMA showcase',                '2026-05-20', '2026-05-20', 'SCHEDULED', 'PUBLIC',  3000, 2, 2, 2),
('Judo Cup Sousse',          'Regional judo cup — U18 and senior',       '2026-03-15', '2026-03-16', 'COMPLETED', 'PUBLIC',  2000, 3, 3, 2),
('Kickboxing Gala Monastir', 'Charity gala — exhibition bouts',          '2026-06-01', '2026-06-01', 'SCHEDULED', 'PRIVATE', 1600, 4, 4, 2),
('Cairo Champions League',   'International tournament — 5 disciplines', '2026-07-10', '2026-07-15', 'SCHEDULED', 'PUBLIC',  7000, 5, 1, 2);

INSERT INTO event_schedule (event_id, title, scheduled_time, duration_min, notes) VALUES
(1, 'Fighter Weigh-In',        '2026-04-09 16:00:00', 120, 'Official weigh-in'),
(1, 'Opening Ceremony',        '2026-04-10 09:00:00',  60, 'Welcome address'),
(1, 'Preliminary Bouts',       '2026-04-10 10:00:00', 240, 'All preliminary rounds'),
(1, 'Semi-Finals',             '2026-04-11 14:00:00', 180, 'Top 4 fighters'),
(1, 'Finals & Award Ceremony', '2026-04-12 18:00:00', 150, 'Championship finals'),
(2, 'Doors Open',              '2026-05-20 18:00:00',  30, 'Ticket check'),
(2, 'Undercard Bouts',         '2026-05-20 19:00:00', 120, '4 preliminary MMA fights'),
(2, 'Main Card',               '2026-05-20 21:00:00', 150, '3 main event bouts'),
(3, 'U18 Rounds',              '2026-03-15 09:00:00', 300, 'Under-18 category'),
(3, 'Senior Finals',           '2026-03-16 15:00:00', 180, 'Senior division finals');


-- ============================================================
-- SEED DATA — MODULE 2
-- ============================================================
INSERT INTO weight_class (name, min_weight, max_weight) VALUES
('Flyweight',    48.00,  51.00),
('Featherweight',54.00,  57.00),
('Lightweight',  57.00,  63.50),
('Welterweight', 63.50,  69.00),
('Middleweight', 69.00,  75.00),
('Heavyweight',  91.00, 120.00);

INSERT INTO fighter (user_id, nickname, date_of_birth, nationality, weight_class_id, wins, losses, draws, status) VALUES
(3, 'The Lion',  '1998-05-14', 'Tunisian', 6, 10, 2, 1, 'ACTIVE'),
(4, 'Iron Fist', '1997-11-22', 'Tunisian', 6,  8, 3, 0, 'ACTIVE'),
(7, 'The Cobra', '2000-03-08', 'Tunisian', 3,  5, 5, 2, 'ACTIVE'),
(8, 'Tigress',   '1999-07-30', 'Tunisian', 4,  7, 1, 0, 'ACTIVE');

INSERT INTO coach (user_id, speciality, experience_years, certification, status) VALUES
(5, 'Boxing & Strength Conditioning', 10, 'AIBA Level 3',  'ACTIVE'),
(9, 'MMA Ground Game & Submissions',   7, 'IMMAF Level 2', 'ACTIVE');

INSERT INTO fighter_coach (fighter_id, coach_id, start_date, is_active) VALUES
(1, 1, '2024-01-10', 1),
(2, 1, '2024-02-15', 1),
(3, 2, '2023-09-01', 1),
(4, 2, '2023-11-20', 1);

INSERT INTO event_fighter (event_id, fighter_id, registration_date, weight_at_event, corner) VALUES
(1, 1, '2026-03-20', 91.50, 'RED'),
(1, 2, '2026-03-21', 90.80, 'BLUE'),
(2, 3, '2026-04-15', 70.10, 'RED'),
(2, 4, '2026-04-16', 69.75, 'BLUE'),
(3, 1, '2026-02-28', 90.00, 'RED');


-- ============================================================
-- SEED DATA — MODULE 3
-- ============================================================
INSERT INTO matchmaking_rule (name, description, weight, is_active) VALUES
('Weight Class Match', 'Fighters must be in same weight class',        1.00, 1),
('Win Rate Balance',   'Win rate difference must be less than 30%',    0.80, 1),
('Experience Level',   'Total fights difference must be less than 10', 0.70, 1),
('Recent Performance', 'Based on last 5 fights performance score',     0.90, 1);

INSERT INTO match_proposal (event_id, fighter1_id, fighter2_id, compatibility, status, notes) VALUES
(1, 1, 2, 87.50, 'ACCEPTED', 'Same weight class, balanced record'),
(2, 3, 4, 79.30, 'ACCEPTED', 'Good weight and experience match'),
(1, 1, 3, 45.00, 'REJECTED', 'Weight class mismatch');

INSERT INTO ranking (fighter_id, discipline_id, rank_position, points, season) VALUES
(1, 1, 1, 950.00, '2026'),
(2, 1, 2, 870.00, '2026'),
(3, 2, 1, 820.00, '2026'),
(4, 2, 2, 760.00, '2026');

INSERT INTO performance_score (fighter_id, score, aggression, defense, technique, experience) VALUES
(1, 88.50, 9.0, 8.5, 8.8, 9.0),
(2, 82.00, 8.5, 8.0, 8.2, 8.0),
(3, 76.50, 7.5, 7.8, 7.5, 7.8),
(4, 80.00, 8.0, 8.2, 8.0, 7.5);


-- ============================================================
-- SEED DATA — MODULE 4
-- ============================================================
INSERT INTO fight_result (event_id, match_id, fighter_red_id, fighter_blue_id, winner_id, method, round_ended, time_ended, fight_date) VALUES
(3, NULL, 1, 2, 1, 'DECISION', 3, '03:00', '2026-03-16'),
(3, NULL, 3, 4, 3, 'KO',       2, '01:45', '2026-03-16');

INSERT INTO fight_statistic (fight_result_id, fighter_id, strikes_landed, strikes_thrown, takedowns, submissions, knockdowns) VALUES
(1, 1, 145, 210, 3, 0, 1),
(1, 2, 112, 185, 1, 0, 0),
(2, 3,  87, 130, 5, 1, 2),
(2, 4,  54, 110, 0, 0, 0);

INSERT INTO judge_score (fight_result_id, judge_name, score_red, score_blue) VALUES
(1, 'Judge Hassan', 29, 28),
(1, 'Judge Salma',  29, 27),
(1, 'Judge Tarek',  28, 28);


SET FOREIGN_KEY_CHECKS = 1;
