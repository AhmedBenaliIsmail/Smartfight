-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 26, 2026 at 11:05 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smartfight`
--

-- --------------------------------------------------------

--
-- Table structure for table `blog_article`
--

CREATE TABLE `blog_article` (
  `id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `title` varchar(220) NOT NULL,
  `content` longtext NOT NULL,
  `summary` longtext DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `view_count` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `blog_article`
--

INSERT INTO `blog_article` (`id`, `category_id`, `author_id`, `title`, `content`, `summary`, `status`, `view_count`, `created_at`, `updated_at`, `image_path`, `video_path`) VALUES
(16, 1, 20, 'test symfony', 'test symfony', 'test symfony', 'PUBLISHED', 2, '2026-04-12 01:08:18', '2026-04-12 01:09:05', 'bivol-69dad4625fc02306254220.png', 'vid_16.mp4'),
(17, 1, 20, 'test symfony 22', 'test symfony 22', 'test symfony 22', 'PUBLISHED', 0, '2026-04-13 15:53:26', '2026-04-13 15:53:26', 'img_17.jpg', 'vid_17.mp4'),
(18, 1, 20, 'test symfony 1111', 'test symfony 1111', 'test symfony 1111', 'PUBLISHED', 1, '2026-04-13 16:38:32', '2026-04-13 16:38:55', 'bivol-69dcffff76f5d267663375.png', 'vid_18.mp4');

-- --------------------------------------------------------

--
-- Table structure for table `blog_category`
--

CREATE TABLE `blog_category` (
  `id` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `description` longtext DEFAULT NULL,
  `slug` varchar(150) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `blog_category`
--

INSERT INTO `blog_category` (`id`, `name`, `description`, `slug`, `image_url`, `created_at`, `updated_at`) VALUES
(1, 'Event Recaps', 'Post-event summaries and highlights', 'event-recaps', NULL, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 'Fighter Stories', 'Profiles and interviews with fighters', 'fighter-stories', NULL, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 'Training Tips', 'Training advice and workout breakdowns', 'training-tips', NULL, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 'Fight Predictions', 'Expert and community fight predictions', 'fight-predictions', NULL, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 'Interviews', 'Exclusive fighter and coach interviews', 'interviews', NULL, '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `classement`
--

CREATE TABLE `classement` (
  `id` int(11) NOT NULL,
  `combattant_id` int(11) NOT NULL,
  `score` double NOT NULL,
  `rang` int(11) NOT NULL,
  `discipline` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `classement`
--

INSERT INTO `classement` (`id`, `combattant_id`, `score`, `rang`, `discipline`) VALUES
(18, 6, 2203.1, 1, 'MMA'),
(19, 9, 1925.7, 2, 'MMA'),
(20, 11, 1799.2, 3, 'MMA'),
(21, 5, 1605, 4, 'MMA'),
(22, 7, 1572.3, 5, 'MMA'),
(23, 3, 1487.3, 6, 'MMA'),
(24, 8, 1100, 7, 'MMA'),
(25, 4, 786.7, 8, 'MMA'),
(26, 10, 0, 9, 'MMA');

-- --------------------------------------------------------

--
-- Table structure for table `combat`
--

CREATE TABLE `combat` (
  `id` int(11) NOT NULL,
  `score_ia` double NOT NULL,
  `resultat` varchar(50) DEFAULT NULL,
  `date_combat` datetime DEFAULT NULL,
  `combattant1_id` int(11) NOT NULL,
  `combattant2_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `combattant`
--

CREATE TABLE `combattant` (
  `id` int(11) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `nationalite` varchar(255) NOT NULL,
  `weight_class` varchar(255) NOT NULL,
  `wins` int(11) NOT NULL,
  `losses` int(11) NOT NULL,
  `draws` int(11) NOT NULL,
  `discipline` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `combattant`
--

INSERT INTO `combattant` (`id`, `nickname`, `nationalite`, `weight_class`, `wins`, `losses`, `draws`, `discipline`) VALUES
(3, 'Iron Fist', 'Tunisian', 'Unknown', 8, 3, 0, 'MMA'),
(4, 'The Cobra', 'Tunisian', 'Unknown', 5, 5, 2, 'MMA'),
(5, 'Tigress', 'Tunisian', 'Unknown', 7, 1, 0, 'MMA'),
(6, 'The Tank', 'Tunisian', 'Unknown', 12, 1, 0, 'MMA'),
(7, 'Lightning', 'Tunisian', 'Unknown', 9, 3, 1, 'MMA'),
(8, 'The Ghost', 'Tunisian', 'Unknown', 6, 4, 0, 'MMA'),
(9, 'Hammer', 'Tunisian', 'Unknown', 11, 2, 1, 'MMA'),
(10, 'test', 'tunisian', 'Middleweight', 0, 0, 0, 'MMA'),
(11, 'Bones', 'Tunisian', 'Unknown', 10, 2, 1, 'MMA'),
(12, 'test', 'tunisian', 'Featherweight', 2, 3, 2, 'Boxing');

-- --------------------------------------------------------

--
-- Table structure for table `discipline`
--

CREATE TABLE `discipline` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` longtext DEFAULT NULL,
  `weight_class` varchar(60) DEFAULT NULL,
  `round_duration` int(11) NOT NULL DEFAULT 3,
  `max_rounds` int(11) NOT NULL DEFAULT 3,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `discipline`
--

INSERT INTO `discipline` (`id`, `name`, `description`, `weight_class`, `round_duration`, `max_rounds`, `created_at`, `updated_at`) VALUES
(1, 'Boxing', 'Classic boxing — unified rules', 'Heavyweight', 3, 12, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 'MMA', 'Mixed Martial Arts — Unified Rules', 'Lightweight', 5, 5, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 'Judo', 'IJF rules — randori and shiai format', 'Open', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 'Kickboxing', 'K-1 ruleset — punches and kicks above waist', 'Middleweight', 3, 5, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 'Wrestling', 'Freestyle — FILA international rules', 'Welterweight', 2, 3, '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260411172928', '2026-04-12 15:30:30', 159),
('DoctrineMigrations\\Version20260411195209', '2026-04-12 15:30:30', 63),
('DoctrineMigrations\\Version20260411222346', '2026-04-12 01:00:48', 782),
('DoctrineMigrations\\Version20260412143000', '2026-04-12 12:29:54', 100),
('DoctrineMigrations\\Version20260412225325', '2026-04-13 01:07:20', 507),
('DoctrineMigrations\\Version20260424000000', '2026-04-24 21:37:47', 441),
('DoctrineMigrations\\Version20260424212118', '2026-04-24 23:21:56', 150),
('DoctrineMigrations\\Version20260424213335', '2026-04-24 23:33:54', 13),
('DoctrineMigrations\\Version20260425000000', '2026-04-25 02:39:33', 85),
('DoctrineMigrations\\Version20260426000000', '2026-04-26 16:09:58', 182);

-- --------------------------------------------------------

--
-- Table structure for table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` longtext DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `visibility` varchar(10) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 0,
  `discipline_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `starts_at` datetime DEFAULT NULL,
  `ends_at` datetime DEFAULT NULL,
  `venue_name` varchar(150) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `country` varchar(2) DEFAULT NULL,
  `poster_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event`
--

INSERT INTO `event` (`id`, `name`, `description`, `status`, `visibility`, `capacity`, `discipline_id`, `created_at`, `updated_at`, `starts_at`, `ends_at`, `venue_name`, `city`, `country`, `poster_url`) VALUES
(1, 'SmartFight Open 2026', 'Annual open boxing championship', 'LIVE', 'PUBLIC', 4500, 1, '2026-04-04 10:53:06', '2026-04-25 02:38:35', '2026-04-10 00:00:00', '2026-04-12 23:59:59', NULL, NULL, NULL, NULL),
(2, 'MMA Night Sfax 2026', 'Professional MMA showcase', 'SCHEDULED', 'PUBLIC', 3000, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06', '2026-05-20 00:00:00', '2026-05-20 23:59:59', NULL, NULL, NULL, NULL),
(3, 'Judo Cup Sousse', 'Regional judo cup — U18 and senior', 'LIVE', 'PUBLIC', 2000, 3, '2026-04-04 10:53:06', '2026-04-25 02:38:35', '2026-03-15 00:00:00', '2026-03-16 23:59:59', NULL, NULL, NULL, NULL),
(4, 'Kickboxing Gala Monastir', 'Charity gala — exhibition bouts', 'SCHEDULED', 'PRIVATE', 1600, 4, '2026-04-04 10:53:06', '2026-04-04 10:53:06', '2026-06-01 00:00:00', '2026-06-01 23:59:59', NULL, NULL, NULL, NULL),
(5, 'Cairo Champions League', 'International tournament — 5 disciplines', 'SCHEDULED', 'PUBLIC', 6993, 1, '2026-04-04 10:53:06', '2026-04-13 16:59:17', '2026-07-10 00:00:00', '2026-07-15 23:59:59', NULL, NULL, NULL, NULL),
(6, 'FightSphere FC 1: Origins', 'Inaugural FightSphere MMA card — 8 bouts', 'LIVE', 'PUBLIC', 3500, 2, '2026-04-04 10:53:06', '2026-04-25 02:16:49', '2026-04-25 00:00:00', '2026-04-25 23:59:59', NULL, NULL, NULL, NULL),
(7, 'Boxing Thunder Sousse', 'Pro boxing doubleheader under the stars', 'SCHEDULED', 'PUBLIC', 2500, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', '2026-05-05 00:00:00', '2026-05-05 23:59:59', NULL, NULL, NULL, NULL),
(8, 'Kickboxing Clash Tunis', 'K-1 rules, 6 bouts main card', 'SCHEDULED', 'PUBLIC', 4000, 4, '2026-04-04 10:53:06', '2026-04-04 10:53:06', '2026-05-15 00:00:00', '2026-05-15 23:59:59', NULL, NULL, NULL, NULL),
(13, 'TEST', 'TEST', 'LIVE', 'PUBLIC', 10, 1, '2026-04-13 12:44:19', '2026-04-25 02:38:35', '2026-04-14 00:00:00', '2026-04-15 23:59:59', NULL, NULL, NULL, NULL),
(14, 'TEST', NULL, 'LIVE', 'PUBLIC', 4, NULL, '2026-04-13 13:06:44', '2026-04-25 02:38:35', '2026-04-16 00:00:00', '2026-04-17 23:59:59', NULL, NULL, NULL, NULL),
(15, 'TEST', NULL, 'LIVE', 'PUBLIC', 100, 1, '2026-04-13 16:58:55', '2026-04-25 02:38:35', '2026-03-12 00:00:00', '2026-04-05 23:59:59', NULL, NULL, NULL, NULL),
(16, 'TEST', NULL, 'LIVE', 'PUBLIC', 2000, 1, '2026-04-25 02:42:35', '2026-04-26 01:58:21', '2026-04-26 01:42:00', '2026-04-27 01:42:00', 'sfax', 'sfax', 'TN', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `event_booking`
--

CREATE TABLE `event_booking` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `booking_status` varchar(20) NOT NULL,
  `ticket_quantity` int(11) NOT NULL DEFAULT 1,
  `total_price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `ticket_type` varchar(20) NOT NULL,
  `booking_date` date NOT NULL,
  `booking_reference` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event_booking`
--

INSERT INTO `event_booking` (`id`, `event_id`, `user_id`, `booking_status`, `ticket_quantity`, `total_price`, `ticket_type`, `booking_date`, `booking_reference`, `created_at`, `updated_at`) VALUES
(1, 5, 21, 'CONFIRMED', 1, 25.00, 'REGULAR', '2026-04-12', 'SF-3923AC67', '2026-04-12 12:42:18', '2026-04-12 12:52:40'),
(2, 13, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-13', 'SF-EFE5170B', '2026-04-13 12:44:59', '2026-04-13 16:40:53'),
(3, 14, 21, 'CONFIRMED', 1, 25.00, 'REGULAR', '2026-04-13', 'SF-BA693183', '2026-04-13 16:39:57', '2026-04-13 16:39:57'),
(4, 15, 22, 'CONFIRMED', 1, 25.00, 'REGULAR', '2026-04-25', 'SF-F7FE36BC', '2026-04-25 00:51:20', '2026-04-25 00:51:20');

-- --------------------------------------------------------

--
-- Table structure for table `fan_notification`
--

CREATE TABLE `fan_notification` (
  `id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `type` varchar(30) NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` longtext NOT NULL,
  `related_event_id` int(11) DEFAULT NULL,
  `related_fight_id` int(11) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Fan Notification Center — inbox per fan';

--
-- Dumping data for table `fan_notification`
--

INSERT INTO `fan_notification` (`id`, `fan_id`, `type`, `title`, `message`, `related_event_id`, `related_fight_id`, `is_read`, `created_at`) VALUES
(1, 6, 'NEW_EVENT', 'New Boxing event announced', 'SmartFight Open 2026 is coming to Tunis on April 10. Book your tickets now!', 1, NULL, 1, '2026-04-04 10:53:06'),
(2, 10, 'NEW_EVENT', 'New MMA event announced', 'MMA Night Sfax 2026 is scheduled for May 20. Get ready!', 2, NULL, 0, '2026-04-04 10:53:06'),
(3, 6, 'ADMIN_BROADCAST', 'Welcome to FightSphere Fan Portal', 'Follow fighters, predict fight outcomes, and react to results. Enjoy!', NULL, NULL, 1, '2026-04-04 10:53:06'),
(4, 10, 'ADMIN_BROADCAST', 'Welcome to FightSphere Fan Portal', 'Follow fighters, predict fight outcomes, and react to results. Enjoy!', NULL, NULL, 0, '2026-04-04 10:53:06'),
(5, 6, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for SmartFight Open 2026 (Ref: SF-9D046AB1) is confirmed!', 1, NULL, 1, '2026-04-04 10:53:06'),
(6, 6, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for MMA Night Sfax 2026 (Ref: SF-D46475DA) is confirmed!', 2, NULL, 1, '2026-04-04 10:53:06'),
(7, 6, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for MMA Night Sfax 2026 (Ref: SF-D3783EA5) is confirmed!', 2, NULL, 1, '2026-04-04 10:53:06'),
(8, 6, 'ADMIN_BROADCAST', 'test', 'hello everyone', NULL, NULL, 1, '2026-04-04 10:53:06'),
(9, 10, 'ADMIN_BROADCAST', 'test', 'hello everyone', NULL, NULL, 0, '2026-04-04 10:53:06'),
(10, 6, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for SmartFight Open 2026 (Ref: SF-E6E13DC0) is confirmed!', 1, NULL, 1, '2026-04-04 10:53:06'),
(11, 6, 'NEW_EVENT', 'FightSphere FC 1 Announced!', 'The inaugural FightSphere FC card drops April 25 in Tunis. 8 bouts!', 6, NULL, 1, '2026-04-04 10:53:06'),
(12, 6, 'NEW_EVENT', 'Boxing Thunder Sousse', 'Pro boxing returns to Sousse on May 5. Bones vs Iron Fist rematch confirmed!', 7, NULL, 1, '2026-04-04 10:53:06'),
(13, 6, 'NEW_EVENT', 'Kickboxing Clash Tunis', 'K-1 rules, 6 bouts. The Ghost vs Hammer headlines May 15!', 8, NULL, 1, '2026-04-04 10:53:06'),
(14, 6, 'PREDICTION_SCORED', 'Prediction Scored!', 'Your prediction for Ali Hammami vs Mohamed Trabelsi was CORRECT! +3 points.', NULL, NULL, 1, '2026-04-04 10:53:06'),
(15, 6, 'PREDICTION_SCORED', 'Prediction Scored!', 'Your prediction for Omar vs Hana: correct winner, wrong method. +1 point.', NULL, NULL, 1, '2026-04-04 10:53:06'),
(16, 6, 'LEADERBOARD_CHANGE', 'Leaderboard Update', 'You moved to #1 on the season leaderboard with 4 points!', NULL, NULL, 1, '2026-04-04 10:53:06'),
(17, 10, 'NEW_EVENT', 'FightSphere FC 1 Announced!', 'The inaugural FightSphere FC card drops April 25 in Tunis!', 6, NULL, 0, '2026-04-04 10:53:06'),
(18, 10, 'PREDICTION_SCORED', 'Prediction Scored!', 'Your prediction for Ali Hammami vs Mohamed Trabelsi was WRONG. 0 points.', NULL, NULL, 0, '2026-04-04 10:53:06'),
(19, 10, 'PREDICTION_SCORED', 'Prediction Scored!', 'Your prediction for Omar vs Hana: correct winner, wrong method. +1 point.', NULL, NULL, 0, '2026-04-04 10:53:06'),
(20, 11, 'NEW_EVENT', 'FightSphere FC 1 Announced!', 'The inaugural FightSphere FC card drops April 25!', 6, NULL, 0, '2026-04-04 10:53:06'),
(21, 11, 'NEW_EVENT', 'Boxing Thunder Sousse', 'Bones vs Iron Fist rematch confirmed for May 5!', 7, NULL, 0, '2026-04-04 10:53:06'),
(22, 12, 'NEW_EVENT', 'FightSphere FC 1', 'Don\'t miss the inaugural card — April 25!', 6, NULL, 0, '2026-04-04 10:53:06'),
(23, 13, 'ADMIN_BROADCAST', 'Welcome!', 'Welcome to FightSphere Fan Portal, Rami!', NULL, NULL, 0, '2026-04-04 10:53:06'),
(24, 14, 'ADMIN_BROADCAST', 'Welcome!', 'Welcome to FightSphere Fan Portal, Amira!', NULL, NULL, 0, '2026-04-04 10:53:06'),
(25, 15, 'ADMIN_BROADCAST', 'Welcome!', 'Welcome to FightSphere Fan Portal, Karim!', NULL, NULL, 0, '2026-04-04 10:53:06'),
(26, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for SmartFight Open 2026 (Ref: SF-E7714EDD) is confirmed!', 1, NULL, 1, '2026-04-04 18:36:41'),
(27, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for SmartFight Open 2026 (Ref: SF-17BD85A2) is confirmed!', 1, NULL, 1, '2026-04-04 18:37:30'),
(28, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for FightSphere FC 1: Origins (Ref: SF-060D110D) is confirmed!', 6, NULL, 1, '2026-04-05 22:01:48'),
(29, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for SmartFight Open 2026 (Ref: SF-76AB4DBD) is confirmed!', 1, NULL, 1, '2026-04-06 08:44:57'),
(30, 21, 'PREDICTION_SCORED', 'Prediction result', 'Your prediction for Ali Hammami earned you 1 point(s)!', NULL, NULL, 1, '2026-04-06 08:48:30'),
(31, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for FightSphere FC 1: Origins (Ref: SF-306CA102) is confirmed!', 6, NULL, 1, '2026-04-06 10:03:22'),
(32, 6, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(33, 10, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(34, 11, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(35, 12, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(36, 13, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(37, 14, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(38, 15, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 0, '2026-04-06 22:07:15'),
(39, 21, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 1, '2026-04-06 22:07:15'),
(40, 6, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(41, 10, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(42, 11, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(43, 12, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(44, 13, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(45, 14, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(46, 15, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20'),
(47, 21, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 1, '2026-04-12 11:40:20'),
(48, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for Cairo Champions League (Ref: SF-E438A0FF) is confirmed!', 5, NULL, 1, '2026-04-12 12:42:18'),
(49, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for Cairo Champions League (Ref: SF-3923AC67) is confirmed!', 5, NULL, 1, '2026-04-12 12:52:40'),
(50, 6, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(51, 10, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(52, 11, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(53, 12, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(54, 13, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(55, 14, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(56, 15, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 0, '2026-04-12 13:32:30'),
(57, 21, 'ADMIN_BROADCAST', 'Test Symfony 2', 'Test Symfony 2', NULL, NULL, 1, '2026-04-12 13:32:30'),
(58, 6, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(59, 10, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(60, 11, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(61, 12, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(62, 13, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(63, 14, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(64, 15, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 0, '2026-04-13 12:37:38'),
(65, 21, 'ADMIN_BROADCAST', 'Test Symfony 3', 'TEST 3', NULL, NULL, 1, '2026-04-13 12:37:38'),
(66, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for TEST (Ref: SF-EFE5170B) is confirmed!', 13, NULL, 1, '2026-04-13 12:44:59'),
(67, 21, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for TEST (Ref: SF-BA693183) is confirmed!', 14, NULL, 1, '2026-04-13 16:39:57'),
(68, 6, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(69, 10, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(70, 11, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(71, 12, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(72, 13, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(73, 14, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(74, 15, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 0, '2026-04-13 16:41:37'),
(75, 21, 'ADMIN_BROADCAST', 'Test Symfony22', 'Test Symfony22', NULL, NULL, 1, '2026-04-13 16:41:37'),
(76, 22, 'ADMIN_BROADCAST', 'Booking Confirmed', 'Your booking for TEST (Ref: SF-F7FE36BC) is confirmed!', 15, NULL, 0, '2026-04-25 00:51:20');

-- --------------------------------------------------------

--
-- Table structure for table `fan_prediction`
--

CREATE TABLE `fan_prediction` (
  `id` int(11) NOT NULL,
  `match_proposal_id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `predicted_winner_id` int(11) NOT NULL,
  `predicted_method` varchar(20) NOT NULL,
  `submitted_at` datetime NOT NULL,
  `is_locked` tinyint(1) NOT NULL DEFAULT 1,
  `points_earned` int(11) DEFAULT NULL,
  `is_scored` tinyint(1) NOT NULL DEFAULT 0,
  `season` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Fan Prediction League — one prediction per fan per fight';

--
-- Dumping data for table `fan_prediction`
--

INSERT INTO `fan_prediction` (`id`, `match_proposal_id`, `fan_id`, `predicted_winner_id`, `predicted_method`, `submitted_at`, `is_locked`, `points_earned`, `is_scored`, `season`) VALUES
(2, 1, 10, 2, 'KO', '2026-04-04 10:53:06', 1, 0, 1, '2026'),
(4, 2, 10, 3, 'KO', '2026-04-04 10:53:06', 1, 1, 1, '2026'),
(6, 4, 10, 6, 'DECISION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(7, 4, 11, 3, 'SUBMISSION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(8, 4, 12, 3, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(10, 5, 10, 5, 'DECISION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(11, 5, 11, 1, 'DECISION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(12, 5, 13, 5, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(13, 5, 14, 1, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(15, 6, 11, 2, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(16, 6, 12, 1, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(17, 6, 15, 1, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(19, 7, 13, 7, 'DECISION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(21, 8, 14, 4, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(34, 7, 11, 2, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(36, 5, 12, 5, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(37, 7, 12, 1, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(38, 9, 13, 7, 'DECISION', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(39, 10, 13, 4, 'TKO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(40, 2, 6, 3, 'KO', '2026-04-04 10:53:06', 1, NULL, 0, '2026'),
(41, 1, 21, 1, 'TKO', '2026-04-05 22:01:12', 1, 1, 1, '2026');

-- --------------------------------------------------------

--
-- Table structure for table `fan_preference`
--

CREATE TABLE `fan_preference` (
  `id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `favorite_discipline_id` int(11) DEFAULT NULL,
  `favorite_fighter_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Fan preferences for notifications and personalization';

--
-- Dumping data for table `fan_preference`
--

INSERT INTO `fan_preference` (`id`, `fan_id`, `favorite_discipline_id`, `favorite_fighter_id`, `created_at`, `updated_at`) VALUES
(1, 6, 1, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 10, 2, 3, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 11, 2, 5, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 12, 1, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 13, 4, 7, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(6, 14, 3, 4, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(7, 15, 2, 3, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(8, 21, 1, 8, '2026-04-06 22:07:56', '2026-04-06 22:07:56');

-- --------------------------------------------------------

--
-- Table structure for table `fan_profile`
--

CREATE TABLE `fan_profile` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `favorite_sport` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `bio` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fan_profile`
--

INSERT INTO `fan_profile` (`id`, `user_id`, `favorite_sport`, `country`, `bio`) VALUES
(1, 6, 'Boxing', 'Tunisia', 'Huge boxing fan since 2010'),
(2, 10, 'MMA', 'Tunisia', 'Following MMA since UFC 200'),
(3, 11, 'MMA', 'Tunisia', 'MMA fanatic, trains amateur kickboxing'),
(4, 12, 'Boxing', 'Tunisia', 'Boxing analyst and content creator'),
(5, 13, 'Kickboxing', 'Tunisia', 'K-1 fan since day one'),
(6, 14, 'Judo', 'Tunisia', 'Former judo competitor turned superfan'),
(7, 15, 'MMA', 'Tunisia', 'UFC and SmartFight hardcore fan'),
(8, 22, NULL, NULL, NULL),
(9, 23, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `fan_reaction`
--

CREATE TABLE `fan_reaction` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `reaction_type` varchar(20) NOT NULL,
  `comment` varchar(140) DEFAULT NULL,
  `reacted_at` datetime NOT NULL,
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Community Reaction Wall — one reaction per fan per fight';

--
-- Dumping data for table `fan_reaction`
--

INSERT INTO `fan_reaction` (`id`, `fight_result_id`, `fan_id`, `reaction_type`, `comment`, `reacted_at`, `is_pinned`, `is_deleted`) VALUES
(1, 1, 6, 'RESPECT', 'The Lion deserved every point of that decision', '2026-04-04 10:53:06', 0, 0),
(2, 1, 10, 'CONTROVERSIAL', 'Should have been stopped earlier in round 2', '2026-04-04 10:53:06', 0, 1),
(3, 2, 6, 'FIRE', 'That KO was unreal, The Cobra is on another level', '2026-04-04 10:53:06', 0, 0),
(4, 2, 10, 'DOMINANT', NULL, '2026-04-04 10:53:06', 0, 0),
(5, 3, 6, 'SHOCK', 'Didn\'t see that coming! Iron Fist looked done after round 3', '2026-04-04 10:53:06', 0, 0),
(6, 3, 11, 'DOMINANT', 'The Tank is a MACHINE. Nobody can stand with him', '2026-04-04 10:53:06', 0, 0),
(7, 3, 12, 'FIRE', 'Best TKO of the year so far!', '2026-04-04 10:53:06', 1, 0),
(8, 3, 13, 'RESPECT', 'Iron Fist showed heart but The Tank was too much', '2026-04-04 10:53:06', 0, 0),
(9, 3, 10, 'FIRE', 'THE TANK IS UNSTOPPABLE', '2026-04-04 10:53:06', 0, 0),
(10, 4, 6, 'FIRE', 'THAT RIGHT HOOK! Bones is the real deal', '2026-04-04 10:53:06', 0, 0),
(11, 4, 10, 'SHOCK', 'Round 7 KO?! This rivalry is insane', '2026-04-04 10:53:06', 0, 0),
(12, 4, 11, 'DOMINANT', 'Bones dominated every round. Total destruction', '2026-04-04 10:53:06', 0, 0),
(13, 4, 12, 'RESPECT', NULL, '2026-04-04 10:53:06', 0, 0),
(14, 4, 13, 'FIRE', 'FIGHT OF THE YEAR candidate right here', '2026-04-04 10:53:06', 0, 0),
(15, 4, 14, 'SHOCK', 'I had money on Iron Fist... devastating', '2026-04-04 10:53:06', 1, 0),
(16, 4, 15, 'DOMINANT', 'Bones proving why he\'s #1. The GOAT', '2026-04-04 10:53:06', 0, 0),
(17, 5, 11, 'CONTROVERSIAL', 'That scorecard was way too wide. Cobra won rounds 8-10', '2026-04-04 10:53:06', 0, 0),
(18, 5, 12, 'RESPECT', 'Both warriors! 12 rounds of war', '2026-04-04 10:53:06', 0, 0),
(19, 5, 13, 'DOMINANT', 'The Tank controlled the pace all night', '2026-04-04 10:53:06', 1, 0),
(20, 5, 14, 'CONTROVERSIAL', 'Should have been a split decision at least', '2026-04-04 10:53:06', 0, 0),
(21, 5, 15, 'FIRE', 'War of attrition! Both guys left it all in the ring', '2026-04-04 10:53:06', 0, 0),
(22, 5, 6, 'RESPECT', 'Incredible display of heart from both fighters', '2026-04-04 10:53:06', 0, 0),
(23, 4, 21, 'FIRE', 'TEST', '2026-04-04 18:37:56', 0, 1);

-- --------------------------------------------------------

--
-- Table structure for table `fighter`
--

CREATE TABLE `fighter` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `nickname` varchar(100) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `weight_class_id` int(11) DEFAULT NULL,
  `wins` int(11) NOT NULL DEFAULT 0,
  `losses` int(11) NOT NULL DEFAULT 0,
  `draws` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `elo_rating` double NOT NULL DEFAULT 1500,
  `performance_score` double NOT NULL DEFAULT 0,
  `win_streak` int(11) NOT NULL DEFAULT 0,
  `strength_of_schedule` double NOT NULL DEFAULT 1500,
  `ko_wins` int(11) NOT NULL DEFAULT 0,
  `submission_wins` int(11) NOT NULL DEFAULT 0,
  `decision_wins` int(11) NOT NULL DEFAULT 0,
  `champions_event_win_streak` int(11) NOT NULL DEFAULT 0,
  `title_defenses` int(11) NOT NULL DEFAULT 0,
  `height` int(11) DEFAULT NULL,
  `reach` int(11) DEFAULT NULL,
  `weight_class` varchar(50) DEFAULT NULL,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `country_code` varchar(2) DEFAULT NULL,
  `discipline_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fighter`
--

INSERT INTO `fighter` (`id`, `user_id`, `nickname`, `date_of_birth`, `nationality`, `photo_url`, `weight_class_id`, `wins`, `losses`, `draws`, `status`, `created_at`, `updated_at`, `elo_rating`, `performance_score`, `win_streak`, `strength_of_schedule`, `ko_wins`, `submission_wins`, `decision_wins`, `champions_event_win_streak`, `title_defenses`, `height`, `reach`, `weight_class`, `first_name`, `last_name`, `country_code`, `discipline_id`) VALUES
(1, 3, 'Bones', '1998-05-14', 'Tunisian', NULL, 3, 10, 2, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 83.4, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(2, 4, 'Iron Fist', '1997-11-22', 'Tunisian', NULL, 5, 8, 3, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 78.93, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(3, 7, 'The Cobra', '2000-03-08', 'Tunisian', NULL, 11, 5, 5, 2, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 76.08, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(4, 8, 'Tigress', '1999-07-30', 'Tunisian', NULL, 10, 7, 1, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 80.38, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(5, 16, 'The Tank', '1996-01-20', 'Tunisian', NULL, 1, 12, 1, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 85.33, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(6, 17, 'Lightning', '1999-09-10', 'Tunisian', NULL, 7, 9, 3, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 81.48, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(7, 18, 'The Ghost', '1998-06-25', 'Tunisian', NULL, 9, 6, 4, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 75, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(8, 19, 'Hammer', '1997-12-05', 'Tunisian', NULL, 2, 11, 2, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-13 01:17:50', 1500, 84.43, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(9, NULL, 'Bones', '2026-04-25', NULL, NULL, 1, 3, 0, 0, 'ACTIVE', '2026-04-25 21:16:35', '2026-04-25 21:16:35', 1500, 0, 0, 1500, 0, 0, 0, 0, 0, NULL, NULL, NULL, 'jon', 'jones', 'US', 1);

-- --------------------------------------------------------

--
-- Table structure for table `fight_result`
--

CREATE TABLE `fight_result` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `match_id` int(11) DEFAULT NULL,
  `fighter_red_id` int(11) NOT NULL,
  `fighter_blue_id` int(11) NOT NULL,
  `winner_id` int(11) DEFAULT NULL,
  `method` varchar(20) NOT NULL,
  `round_ended` int(11) DEFAULT NULL,
  `time_ended` time DEFAULT NULL,
  `notes` longtext DEFAULT NULL,
  `fight_date` date NOT NULL,
  `created_at` datetime NOT NULL,
  `fight_number` int(11) NOT NULL DEFAULT 1,
  `status` varchar(20) NOT NULL DEFAULT 'COMPLETED',
  `knockdowns_fighter_red` int(11) NOT NULL DEFAULT 0,
  `knockdowns_fighter_blue` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fight_result`
--

INSERT INTO `fight_result` (`id`, `event_id`, `match_id`, `fighter_red_id`, `fighter_blue_id`, `winner_id`, `method`, `round_ended`, `time_ended`, `notes`, `fight_date`, `created_at`, `fight_number`, `status`, `knockdowns_fighter_red`, `knockdowns_fighter_blue`) VALUES
(1, 3, NULL, 1, 2, 1, 'UD', 3, '03:00:00', NULL, '2026-03-16', '2026-04-04 10:53:06', 1, 'COMPLETED', 0, 0),
(2, 3, NULL, 3, 4, 3, 'KO', 2, '01:45:00', NULL, '2026-03-16', '2026-04-04 10:53:06', 1, 'COMPLETED', 0, 0),
(3, 3, NULL, 5, 2, 5, 'TKO', 4, '02:30:00', 'Doctor stoppage — cut above eye', '2026-03-16', '2026-04-04 10:53:06', 1, 'COMPLETED', 0, 0),
(4, 1, 1, 1, 2, 1, 'KO', 7, '01:22:00', 'Devastating right hook KO in round 7', '2026-04-12', '2026-04-04 10:53:06', 1, 'COMPLETED', 0, 0),
(5, 1, NULL, 5, 3, 5, 'UD', 12, '03:00:00', 'Unanimous decision 117-111, 116-112, 118-110', '2026-04-12', '2026-04-04 10:53:06', 1, 'COMPLETED', 0, 0),
(6, 1, NULL, 2, 1, 1, 'KO', 5, NULL, NULL, '2026-04-13', '2026-04-13 16:46:06', 1, 'COMPLETED', 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `fight_stat`
--

CREATE TABLE `fight_stat` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `round_number` int(11) NOT NULL DEFAULT 0,
  `strikes_thrown` int(11) NOT NULL DEFAULT 0,
  `strikes_landed` int(11) NOT NULL DEFAULT 0,
  `power_shots_thrown` int(11) NOT NULL DEFAULT 0,
  `power_shots_landed` int(11) NOT NULL DEFAULT 0,
  `jabs_thrown` int(11) NOT NULL DEFAULT 0,
  `jabs_landed` int(11) NOT NULL DEFAULT 0,
  `knockdowns` int(11) NOT NULL DEFAULT 0,
  `punches_thrown` int(11) NOT NULL DEFAULT 0,
  `punches_landed` int(11) NOT NULL DEFAULT 0,
  `head_shots_landed` int(11) NOT NULL DEFAULT 0,
  `body_shots_landed` int(11) NOT NULL DEFAULT 0,
  `clinch_strikes_thrown` int(11) NOT NULL DEFAULT 0,
  `clinch_strikes_landed` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `fight_statistic`
--

CREATE TABLE `fight_statistic` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `strikes_landed` int(11) NOT NULL DEFAULT 0,
  `strikes_attempted` int(11) NOT NULL DEFAULT 0,
  `takedowns_landed` int(11) NOT NULL DEFAULT 0,
  `takedowns_attempted` int(11) NOT NULL DEFAULT 0,
  `submission_attempts` int(11) NOT NULL DEFAULT 0,
  `knockdowns` int(11) NOT NULL DEFAULT 0,
  `control_time_seconds` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `punches_thrown` int(11) NOT NULL DEFAULT 0,
  `punches_landed` int(11) NOT NULL DEFAULT 0,
  `round_number` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fight_statistic`
--

INSERT INTO `fight_statistic` (`id`, `fight_result_id`, `fighter_id`, `strikes_landed`, `strikes_attempted`, `takedowns_landed`, `takedowns_attempted`, `submission_attempts`, `knockdowns`, `control_time_seconds`, `created_at`, `updated_at`, `punches_thrown`, `punches_landed`, `round_number`) VALUES
(1, 4, 1, 2, 4, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 13:07:23', 0, 0, NULL),
(2, 4, 2, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(3, 5, 5, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(4, 5, 3, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(5, 1, 1, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(6, 1, 2, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(7, 2, 3, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(8, 2, 4, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(9, 3, 5, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(10, 3, 2, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 01:17:50', '2026-04-13 01:17:50', 0, 0, NULL),
(11, 6, 1, 0, 0, 0, 0, 0, 0, 0, '2026-04-13 16:53:03', '2026-04-13 16:54:24', 0, 0, NULL),
(12, 6, 2, 4, 8, 0, 0, 0, 0, 0, '2026-04-13 16:54:24', '2026-04-13 16:54:24', 0, 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `match_proposal`
--

CREATE TABLE `match_proposal` (
  `id` int(11) NOT NULL,
  `event_id` int(11) DEFAULT NULL,
  `fighter1_id` int(11) NOT NULL,
  `fighter2_id` int(11) NOT NULL,
  `compatibility` decimal(5,2) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `proposed_at` datetime NOT NULL,
  `notes` longtext DEFAULT NULL,
  `scheduled_rounds` int(11) DEFAULT NULL,
  `is_title_fight` tinyint(1) NOT NULL DEFAULT 0,
  `weight_class_id` int(11) DEFAULT NULL,
  `odds_fighter1` decimal(5,2) DEFAULT NULL,
  `odds_fighter2` decimal(5,2) DEFAULT NULL,
  `card_position` int(11) DEFAULT NULL,
  `card_type` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `match_proposal`
--

INSERT INTO `match_proposal` (`id`, `event_id`, `fighter1_id`, `fighter2_id`, `compatibility`, `status`, `proposed_at`, `notes`, `scheduled_rounds`, `is_title_fight`, `weight_class_id`, `odds_fighter1`, `odds_fighter2`, `card_position`, `card_type`) VALUES
(1, 1, 1, 2, 87.50, 'COMPLETED', '2026-02-23 23:27:13', 'Same weight class, balanced record', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(2, 2, 3, 4, 79.30, 'COMPLETED', '2026-02-23 23:27:13', 'Good weight and experience match', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(3, 1, 1, 3, 45.00, 'CANCELLED', '2026-02-23 23:27:13', 'Weight class mismatch', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(4, 6, 3, 6, 82.00, 'COMPLETED', '2026-03-28 10:00:00', 'Lightweight clash — The Cobra vs Lightning', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(5, 6, 1, 5, 88.50, 'COMPLETED', '2026-03-28 10:30:00', 'Heavyweight showdown — Bones vs The Tank', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(6, 7, 1, 2, 87.50, 'COMPLETED', '2026-03-29 09:00:00', 'Rematch — Bones vs Iron Fist', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(7, 8, 7, 8, 80.00, 'COMPLETED', '2026-03-30 11:00:00', 'Kickboxing — The Ghost vs Hammer', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(8, 8, 3, 4, 79.30, 'COMPLETED', '2026-03-30 11:30:00', 'Kickboxing — The Cobra vs Tigress', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(9, 8, 7, 8, 80.00, 'COMPLETED', '2026-03-30 11:00:00', 'K-1: The Ghost vs Hammer', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(10, 8, 3, 4, 79.30, 'COMPLETED', '2026-03-30 11:30:00', 'K-1: The Cobra vs Tigress', NULL, 0, NULL, NULL, NULL, NULL, NULL),
(11, NULL, 1, 2, NULL, 'SCHEDULED', '2026-04-25 21:33:46', NULL, NULL, 1, 1, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `performance_score`
--

CREATE TABLE `performance_score` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `score` double NOT NULL DEFAULT 0,
  `aggression` double NOT NULL DEFAULT 0,
  `defense` double NOT NULL DEFAULT 0,
  `technique` double NOT NULL DEFAULT 0,
  `experience` double NOT NULL DEFAULT 0,
  `season` varchar(20) NOT NULL,
  `computed_at` datetime NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `performance_score`
--

INSERT INTO `performance_score` (`id`, `fighter_id`, `score`, `aggression`, `defense`, `technique`, `experience`, `season`, `computed_at`, `created_at`, `updated_at`) VALUES
(1, 1, 83.4, 0, 84.62, 0, 65, 'CURRENT', '2026-04-13 16:54:24', '2026-04-13 01:17:50', '2026-04-13 16:54:24'),
(2, 2, 78.93, 0, 72.73, 0, 55, 'CURRENT', '2026-04-13 16:54:24', '2026-04-13 01:17:50', '2026-04-13 16:54:24'),
(3, 3, 76.08, 0, 58.33, 0, 60, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50'),
(4, 4, 80.38, 0, 87.5, 0, 40, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50'),
(5, 5, 85.33, 0, 92.31, 0, 65, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50'),
(6, 6, 81.48, 0, 76.92, 0, 65, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50'),
(7, 7, 75, 0, 60, 0, 50, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50'),
(8, 8, 84.43, 0, 85.71, 0, 70, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', '2026-04-13 01:17:50');

-- --------------------------------------------------------

--
-- Table structure for table `ranking`
--

CREATE TABLE `ranking` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `weight_class` varchar(50) NOT NULL,
  `rank_position` int(11) NOT NULL,
  `points` double NOT NULL DEFAULT 0,
  `season` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `weight_class_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `ranking`
--

INSERT INTO `ranking` (`id`, `fighter_id`, `weight_class`, `rank_position`, `points`, `season`, `created_at`, `updated_at`, `weight_class_id`) VALUES
(4, 1, 'UNSPECIFIED', 1, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(5, 2, 'UNSPECIFIED', 2, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(6, 3, 'UNSPECIFIED', 3, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(7, 4, 'UNSPECIFIED', 4, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(8, 5, 'UNSPECIFIED', 5, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(9, 6, 'UNSPECIFIED', 6, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(10, 7, 'UNSPECIFIED', 7, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL),
(11, 8, 'UNSPECIFIED', 8, 1500, 'CURRENT', '2026-04-13 01:17:50', '2026-04-13 01:17:50', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `system_meta`
--

CREATE TABLE `system_meta` (
  `id` int(11) NOT NULL,
  `meta_key` varchar(100) NOT NULL,
  `value` varchar(255) NOT NULL,
  `updatedAt` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `system_meta`
--

INSERT INTO `system_meta` (`id`, `meta_key`, `value`, `updatedAt`) VALUES
(1, 'last_status_refresh', '2026-04-26 22:29:13', '2026-04-26 22:29:13');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `username` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `first_name`, `last_name`, `email`, `password`, `phone`, `role_id`, `is_active`, `created_at`, `updated_at`, `username`) VALUES
(1, 'Nassir', 'Admin', 'nassir@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 001', 1, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(2, 'Ayoub', 'Org', 'ayoub@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 002', 2, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(3, 'Ali', 'Hammami', 'ali@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 003', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(4, 'Mohamed', 'Trabelsi', 'mohamed@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 004', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(5, 'Karim', 'Coach', 'karim@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 005', 4, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(6, 'Sana', 'Fan', 'sana@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 006', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(7, 'Omar', 'Belhaj', 'omar@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 007', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(8, 'Hana', 'Mansour', 'hana@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 008', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(9, 'Youssef', 'Coach2', 'youssef@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 009', 4, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(10, 'Leila', 'Fan2', 'leila@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 010', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(11, 'Yassine', 'Bouazizi', 'yassine@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 011', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(12, 'Nour', 'Chaouch', 'nour@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 012', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(13, 'Rami', 'Jebali', 'rami@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 013', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(14, 'Amira', 'Selmi', 'amira@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 014', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(15, 'Karim', 'Gharbi', 'karim.fan@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 015', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(16, 'Fares', 'Khelifi', 'fares@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 016', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(17, 'Bilel', 'Sassi', 'bilel@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 017', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(18, 'Anis', 'Mrad', 'anis@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 018', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(19, 'Wael', 'Haddad', 'wael@smartfight.tn', '$2y$13$7LbxaT3vbDsRHy0Dhvqu/uFhcO2BrVBRgfO/p2nx3oq6AaCY8ePdC', '+216 20 111 019', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06', NULL),
(20, 'ahmed', 'ahmed', 'ahmed@ahmed.com', '$2y$10$1Vhqr7a/0T2Z4LeRXkMO5uoqhT7NhSJwSSCFwwJPGGXvpFRW5P.da', '12345678', 1, 1, '2026-04-04 18:33:21', '2026-04-09 22:46:04', NULL),
(21, 'test', 'test', 'test@fan.com', '$2y$10$/3eecYdEiGABj7mRKSVB5OqMjW4XpHo5/nGevKxqjAbUC3Svskzcy', '112233445566', 5, 1, '2026-04-04 18:35:26', '2026-04-09 22:46:04', NULL),
(22, 'Ahmed', 'Ismail', 'ahmedismail2321@gmail.com', '$2y$13$3IlH8sUktI.cz06r/kwWQuLT1vWKSQxu07DHb7kiA5uPVEpRd65fW', NULL, 5, 1, '2026-04-25 00:50:27', '2026-04-25 00:50:27', NULL),
(23, 'ahmed', 'ismail', 'ahmed.ismail.fac@gmail.com', '$2y$13$yNWyfPw8lDr0BWUi1i.w/.b3/a9J5HfV.fYxIwRBKzeeZfOvjIIK6', NULL, 5, 1, '2026-04-25 00:52:40', '2026-04-25 00:52:40', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `user_role`
--

CREATE TABLE `user_role` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user_role`
--

INSERT INTO `user_role` (`id`, `name`, `description`) VALUES
(1, 'ADMIN', 'Full system access'),
(2, 'ORGANIZER', 'Can manage events'),
(3, 'FIGHTER', 'Registered fighter'),
(4, 'COACH', 'Fighter coach'),
(5, 'FAN', 'Public viewer');

-- --------------------------------------------------------

--
-- Table structure for table `weight_class`
--

CREATE TABLE `weight_class` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `min_weight` decimal(5,2) DEFAULT NULL,
  `max_weight` decimal(5,2) DEFAULT NULL,
  `discipline_id` int(11) NOT NULL,
  `champion_id` int(11) DEFAULT NULL,
  `displayOrder` int(11) NOT NULL DEFAULT 0,
  `slug` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `weight_class`
--

INSERT INTO `weight_class` (`id`, `name`, `min_weight`, `max_weight`, `discipline_id`, `champion_id`, `displayOrder`, `slug`) VALUES
(1, 'Heavyweight', 91.00, 120.00, 1, NULL, 1, 'heavyweight'),
(2, 'Cruiserweight', 86.00, 90.72, 1, NULL, 2, 'cruiserweight'),
(3, 'Light Heavyweight', 76.00, 79.38, 1, NULL, 3, 'light-heavyweight'),
(4, 'Super Middleweight', 73.00, 76.20, 1, NULL, 4, 'super-middleweight'),
(5, 'Middleweight', 69.00, 72.57, 1, NULL, 5, 'middleweight'),
(6, 'Super Welterweight', 66.00, 69.85, 1, NULL, 6, 'super-welterweight'),
(7, 'Welterweight', 63.00, 66.68, 1, NULL, 7, 'welterweight'),
(8, 'Super Lightweight', 61.00, 63.50, 1, NULL, 8, 'super-lightweight'),
(9, 'Lightweight', 58.00, 61.23, 1, NULL, 9, 'lightweight'),
(10, 'Super Featherweight', 56.00, 58.97, 1, NULL, 10, 'super-featherweight'),
(11, 'Featherweight', 54.00, 57.15, 1, NULL, 11, 'featherweight'),
(12, 'Super Bantamweight', 52.00, 55.34, 1, NULL, 12, 'super-bantamweight'),
(13, 'Bantamweight', 50.00, 53.52, 1, NULL, 13, 'bantamweight'),
(14, 'Super Flyweight', 49.00, 52.16, 1, NULL, 14, 'super-flyweight'),
(15, 'Flyweight', 48.00, 50.80, 1, NULL, 15, 'flyweight'),
(16, 'Light Flyweight', 46.00, 48.99, 1, NULL, 16, 'light-flyweight'),
(17, 'Minimumweight', 0.00, 47.63, 1, NULL, 17, 'minimumweight');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_EECCB3E512469DE2` (`category_id`),
  ADD KEY `IDX_EECCB3E5F675F31B` (`author_id`);

--
-- Indexes for table `blog_category`
--
ALTER TABLE `blog_category`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_72113DE65E237E06` (`name`),
  ADD UNIQUE KEY `UNIQ_72113DE6989D9B62` (`slug`);

--
-- Indexes for table `classement`
--
ALTER TABLE `classement`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_55EE9D6D4B0BCC96` (`combattant_id`);

--
-- Indexes for table `combat`
--
ALTER TABLE `combat`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_8D51E398CF4B102E` (`combattant1_id`),
  ADD KEY `IDX_8D51E398DDFEBFC0` (`combattant2_id`);

--
-- Indexes for table `combattant`
--
ALTER TABLE `combattant`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `discipline`
--
ALTER TABLE `discipline`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_75BEEE3F5E237E06` (`name`);

--
-- Indexes for table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Indexes for table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_3BAE0AA7A5522701` (`discipline_id`);

--
-- Indexes for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_eb_reference` (`booking_reference`),
  ADD UNIQUE KEY `uq_eb_event_user` (`event_id`,`user_id`),
  ADD KEY `IDX_655B4471A76ED395` (`user_id`);

--
-- Indexes for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D093B975D774A626` (`related_event_id`);

--
-- Indexes for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_prediction` (`match_proposal_id`,`fan_id`),
  ADD KEY `IDX_89572FC989C48F0B` (`fan_id`),
  ADD KEY `IDX_89572FC919EAE00D` (`predicted_winner_id`);

--
-- Indexes for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_preference` (`fan_id`),
  ADD KEY `IDX_E207F05278B04DCA` (`favorite_discipline_id`),
  ADD KEY `IDX_E207F0522A58A1BC` (`favorite_fighter_id`);

--
-- Indexes for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_E95F4CF4A76ED395` (`user_id`);

--
-- Indexes for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_8ED024852F087A36` (`fight_result_id`),
  ADD KEY `IDX_8ED0248589C48F0B` (`fan_id`);

--
-- Indexes for table `fighter`
--
ALTER TABLE `fighter`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_7A08C3FCA76ED395` (`user_id`),
  ADD KEY `IDX_7A08C3FCA0206A65` (`weight_class_id`),
  ADD KEY `IDX_7A08C3FCA5522701` (`discipline_id`);

--
-- Indexes for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_1C8CFD0B71F7E88B` (`event_id`),
  ADD KEY `IDX_1C8CFD0B2ABEACD6` (`match_id`),
  ADD KEY `IDX_1C8CFD0B72DD592F` (`fighter_red_id`),
  ADD KEY `IDX_1C8CFD0B24F12652` (`fighter_blue_id`),
  ADD KEY `IDX_1C8CFD0B5DFCD4B8` (`winner_id`);

--
-- Indexes for table `fight_stat`
--
ALTER TABLE `fight_stat`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_fight_stat_result` (`fight_result_id`),
  ADD KEY `IDX_fight_stat_fighter` (`fighter_id`),
  ADD KEY `IDX_fight_stat_round` (`fight_result_id`,`fighter_id`,`round_number`);

--
-- Indexes for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_629F4EE471F7E88B` (`event_id`),
  ADD KEY `IDX_629F4EE4D783CFD6` (`fighter1_id`),
  ADD KEY `IDX_629F4EE4C5366038` (`fighter2_id`),
  ADD KEY `IDX_629F4EE4A0206A65` (`weight_class_id`);

--
-- Indexes for table `performance_score`
--
ALTER TABLE `performance_score`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `ranking`
--
ALTER TABLE `ranking`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_80B839D0A0206A65` (`weight_class_id`);

--
-- Indexes for table `system_meta`
--
ALTER TABLE `system_meta`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_C63EA7C3251C7524` (`meta_key`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8D93D649E7927C74` (`email`),
  ADD UNIQUE KEY `UNIQ_8D93D649F85E0677` (`username`),
  ADD KEY `IDX_8D93D649D60322AC` (`role_id`);

--
-- Indexes for table `user_role`
--
ALTER TABLE `user_role`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `weight_class`
--
ALTER TABLE `weight_class`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_D2EC4C2F989D9B62` (`slug`),
  ADD KEY `IDX_D2EC4C2FA5522701` (`discipline_id`),
  ADD KEY `IDX_D2EC4C2FFA7FD7EB` (`champion_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `blog_article`
--
ALTER TABLE `blog_article`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `blog_category`
--
ALTER TABLE `blog_category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `classement`
--
ALTER TABLE `classement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `combat`
--
ALTER TABLE `combat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `combattant`
--
ALTER TABLE `combattant`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `discipline`
--
ALTER TABLE `discipline`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `event`
--
ALTER TABLE `event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `event_booking`
--
ALTER TABLE `event_booking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fan_notification`
--
ALTER TABLE `fan_notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- AUTO_INCREMENT for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- AUTO_INCREMENT for table `fan_preference`
--
ALTER TABLE `fan_preference`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `fan_profile`
--
ALTER TABLE `fan_profile`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `fighter`
--
ALTER TABLE `fighter`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `fight_result`
--
ALTER TABLE `fight_result`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `fight_stat`
--
ALTER TABLE `fight_stat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `match_proposal`
--
ALTER TABLE `match_proposal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `performance_score`
--
ALTER TABLE `performance_score`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `ranking`
--
ALTER TABLE `ranking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `system_meta`
--
ALTER TABLE `system_meta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `user_role`
--
ALTER TABLE `user_role`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `weight_class`
--
ALTER TABLE `weight_class`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD CONSTRAINT `FK_EECCB3E512469DE2` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`),
  ADD CONSTRAINT `FK_EECCB3E5F675F31B` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `classement`
--
ALTER TABLE `classement`
  ADD CONSTRAINT `FK_55EE9D6D4B0BCC96` FOREIGN KEY (`combattant_id`) REFERENCES `combattant` (`id`);

--
-- Constraints for table `combat`
--
ALTER TABLE `combat`
  ADD CONSTRAINT `FK_8D51E398CF4B102E` FOREIGN KEY (`combattant1_id`) REFERENCES `combattant` (`id`),
  ADD CONSTRAINT `FK_8D51E398DDFEBFC0` FOREIGN KEY (`combattant2_id`) REFERENCES `combattant` (`id`);

--
-- Constraints for table `event`
--
ALTER TABLE `event`
  ADD CONSTRAINT `FK_3BAE0AA7A5522701` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`);

--
-- Constraints for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD CONSTRAINT `FK_655B447171F7E88B` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `FK_655B4471A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD CONSTRAINT `FK_D093B97589C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_D093B975D774A626` FOREIGN KEY (`related_event_id`) REFERENCES `event` (`id`);

--
-- Constraints for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD CONSTRAINT `FK_89572FC919EAE00D` FOREIGN KEY (`predicted_winner_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_89572FC989C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_89572FC9C6E05170` FOREIGN KEY (`match_proposal_id`) REFERENCES `match_proposal` (`id`);

--
-- Constraints for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD CONSTRAINT `FK_E207F0522A58A1BC` FOREIGN KEY (`favorite_fighter_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_E207F05278B04DCA` FOREIGN KEY (`favorite_discipline_id`) REFERENCES `discipline` (`id`),
  ADD CONSTRAINT `FK_E207F05289C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD CONSTRAINT `FK_E95F4CF4A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD CONSTRAINT `FK_8ED024852F087A36` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`),
  ADD CONSTRAINT `FK_8ED0248589C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `fighter`
--
ALTER TABLE `fighter`
  ADD CONSTRAINT `FK_7A08C3FCA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_FIGHTER_DISCIPLINE` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`),
  ADD CONSTRAINT `fk_fighter_weight` FOREIGN KEY (`weight_class_id`) REFERENCES `weight_class` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD CONSTRAINT `FK_1C8CFD0B24F12652` FOREIGN KEY (`fighter_blue_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B2ABEACD6` FOREIGN KEY (`match_id`) REFERENCES `match_proposal` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B5DFCD4B8` FOREIGN KEY (`winner_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B72DD592F` FOREIGN KEY (`fighter_red_id`) REFERENCES `fighter` (`id`);

--
-- Constraints for table `fight_stat`
--
ALTER TABLE `fight_stat`
  ADD CONSTRAINT `FK_fight_stat_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_fight_stat_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD CONSTRAINT `FK_629F4EE471F7E88B` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `FK_629F4EE4C5366038` FOREIGN KEY (`fighter2_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_629F4EE4D783CFD6` FOREIGN KEY (`fighter1_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_MP_WEIGHT_CLASS` FOREIGN KEY (`weight_class_id`) REFERENCES `weight_class` (`id`);

--
-- Constraints for table `ranking`
--
ALTER TABLE `ranking`
  ADD CONSTRAINT `FK_80B839D0A0206A65` FOREIGN KEY (`weight_class_id`) REFERENCES `weight_class` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `FK_8D93D649D60322AC` FOREIGN KEY (`role_id`) REFERENCES `user_role` (`id`);

--
-- Constraints for table `weight_class`
--
ALTER TABLE `weight_class`
  ADD CONSTRAINT `FK_WC_CHAMPION` FOREIGN KEY (`champion_id`) REFERENCES `fighter` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `FK_WC_DISCIPLINE` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
