-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 26, 2026 at 11:01 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smartfight`

CREATE DATABASE IF NOT EXISTS `smartfight` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `smartfight`;
--

-- --------------------------------------------------------

--
-- Table structure for table `blog_article`
--

CREATE TABLE `blog_article` (
  `id` int(11) NOT NULL,
  `title` varchar(220) NOT NULL,
  `content` longtext NOT NULL,
  `summary` longtext DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `view_count` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL,
  `category_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `blog_article`
--

INSERT INTO `blog_article` (`id`, `title`, `content`, `summary`, `status`, `view_count`, `created_at`, `updated_at`, `image_path`, `video_path`, `category_id`, `author_id`) VALUES
(1, 'The Rise. The Punch. The Fall of a Giant.', 'dqsdqs', 'dqsdqs', 'PUBLISHED', 2, '2026-04-25 11:16:10', '2026-04-25 11:16:55', '69ec86875c79e344452734.jpg', '69ec86875d785275240906.mp4', 1, 1);

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `blog_category`
--

INSERT INTO `blog_category` (`id`, `name`, `description`, `slug`, `image_url`, `created_at`, `updated_at`) VALUES
(1, 'News', NULL, 'news', NULL, '0000-00-00 00:00:00', '0000-00-00 00:00:00'),
(2, 'Technical Analysis', NULL, 'technical-analysis', NULL, '0000-00-00 00:00:00', '0000-00-00 00:00:00'),
(3, 'Events', NULL, 'events', NULL, '0000-00-00 00:00:00', '0000-00-00 00:00:00'),
(4, 'Fighter Interviews', NULL, 'fighter-interviews', NULL, '0000-00-00 00:00:00', '0000-00-00 00:00:00'),
(5, 'Fight Highlights', NULL, 'fight-highlights', NULL, '0000-00-00 00:00:00', '0000-00-00 00:00:00');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\\\Version20260424000000', '2026-04-24 19:28:33', 0),
('DoctrineMigrations\\\\Version20260424000001', '2026-04-24 19:28:33', 0);

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE `events` (
  `eventId` int(11) NOT NULL,
  `eventName` varchar(200) NOT NULL,
  `eventDate` date DEFAULT NULL,
  `organization` varchar(20) NOT NULL DEFAULT 'INDEPENDENT',
  `venue` varchar(200) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `country` varchar(2) DEFAULT NULL,
  `seat_capacity` int(11) DEFAULT NULL,
  `poster_filename` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'SCHEDULED',
  `visibility` varchar(20) NOT NULL DEFAULT 'PUBLIC',
  `is_champions_event` tinyint(4) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `events`
--

INSERT INTO `events` (`eventId`, `eventName`, `eventDate`, `organization`, `venue`, `city`, `country`, `seat_capacity`, `poster_filename`, `status`, `visibility`, `is_champions_event`) VALUES
(1, 'Boxing Night: Abu Dhabi Series', '2025-10-06', 'INDEPENDENT', 'Etihad Arena', 'Abu Dhabi', 'AE', 18000, NULL, 'COMPLETED', 'PUBLIC', 0),
(2, 'Boxing Night: Las Vegas Series', '2025-07-21', 'INDEPENDENT', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, NULL, 'COMPLETED', 'PUBLIC', 0),
(3, 'Boxing Night: Riyadh Series', '2025-05-12', 'INDEPENDENT', 'Kingdom Arena', 'Riyadh', 'SA', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(4, 'Boxing Night: London Series', '2026-02-26', 'INDEPENDENT', 'Wembley Stadium', 'London', 'GB', 90000, NULL, 'COMPLETED', 'PUBLIC', 0),
(5, 'Boxing Night: Los Angeles Series', '2026-02-28', 'INDEPENDENT', 'BMO Stadium', 'Los Angeles', 'US', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(6, 'Boxing Night: Las Vegas Series', '2026-02-08', 'INDEPENDENT', 'MGM Grand', 'Las Vegas', 'US', 16000, NULL, 'COMPLETED', 'PUBLIC', 0),
(7, 'Boxing Night: Riyadh Series', '2025-05-28', 'INDEPENDENT', 'Kingdom Arena', 'Riyadh', 'SA', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(8, 'Boxing Night: Tokyo Series', '2025-10-05', 'INDEPENDENT', 'Tokyo Dome', 'Tokyo', 'JP', 55000, NULL, 'COMPLETED', 'PUBLIC', 0),
(9, 'Boxing Night: Perth Series', '2026-05-16', 'INDEPENDENT', 'RAC Arena', 'Perth', 'AU', 15000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(10, 'World Championship #3865', '2025-12-22', 'WBC', 'Tokyo Dome', 'Tokyo', 'JP', 55000, NULL, 'COMPLETED', 'PUBLIC', 0),
(11, 'Boxing Night: Los Angeles Series', '2026-01-25', 'INDEPENDENT', 'BMO Stadium', 'Los Angeles', 'US', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(12, 'Boxing Night: Riyadh Series', '2025-09-19', 'INDEPENDENT', 'Kingdom Arena', 'Riyadh', 'SA', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(13, 'World Championship #4857', '2025-06-14', 'WBO', 'BMO Stadium', 'Los Angeles', 'US', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(14, 'World Championship #6244', '2025-07-03', 'WBC', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, NULL, 'COMPLETED', 'PUBLIC', 0),
(15, 'Boxing Night: Brooklyn Series', '2025-11-16', 'INDEPENDENT', 'Barclays Center', 'Brooklyn', 'US', 19000, NULL, 'COMPLETED', 'PUBLIC', 0),
(16, 'Boxing Night: Las Vegas Series', '2026-09-14', 'INDEPENDENT', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(17, 'Boxing Night: Perth Series', '2025-07-10', 'INDEPENDENT', 'RAC Arena', 'Perth', 'AU', 15000, NULL, 'COMPLETED', 'PUBLIC', 0),
(18, 'Boxing Night: New York Series', '2025-07-19', 'INDEPENDENT', 'Madison Square Garden', 'New York', 'US', 20000, NULL, 'COMPLETED', 'PUBLIC', 0),
(19, 'Boxing Night: Las Vegas Series', '2025-08-24', 'INDEPENDENT', 'T-Mobile Arena', 'Las Vegas', 'US', 20000, NULL, 'COMPLETED', 'PUBLIC', 0),
(20, 'Boxing Night: London Series', '2026-03-29', 'INDEPENDENT', 'Wembley Stadium', 'London', 'GB', 90000, NULL, 'COMPLETED', 'PUBLIC', 0),
(21, 'Boxing Night: Abu Dhabi Series', '2026-07-27', 'INDEPENDENT', 'Etihad Arena', 'Abu Dhabi', 'AE', 18000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(22, 'Boxing Night: Brooklyn Series', '2026-03-28', 'INDEPENDENT', 'Barclays Center', 'Brooklyn', 'US', 19000, NULL, 'COMPLETED', 'PUBLIC', 0),
(23, 'Boxing Night: Perth Series', '2025-04-28', 'INDEPENDENT', 'RAC Arena', 'Perth', 'AU', 15000, NULL, 'COMPLETED', 'PUBLIC', 0),
(24, 'Boxing Night: Abu Dhabi Series', '2026-07-14', 'INDEPENDENT', 'Etihad Arena', 'Abu Dhabi', 'AE', 18000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(25, 'World Championship #9382', '2026-04-18', 'IBF', 'Wembley Stadium', 'London', 'GB', 90000, NULL, 'COMPLETED', 'PUBLIC', 0),
(26, 'Boxing Night: Riyadh Series', '2026-09-16', 'INDEPENDENT', 'Kingdom Arena', 'Riyadh', 'SA', 22000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(27, 'World Championship #4855', '2026-06-19', 'WBC', 'BMO Stadium', 'Los Angeles', 'US', 22000, NULL, 'SCHEDULED', 'PUBLIC', 0),
(28, 'Boxing Night: Riyadh Series', '2026-01-05', 'INDEPENDENT', 'Kingdom Arena', 'Riyadh', 'SA', 22000, NULL, 'COMPLETED', 'PUBLIC', 0),
(29, 'Boxing Night: Tokyo Series', '2025-11-26', 'INDEPENDENT', 'Tokyo Dome', 'Tokyo', 'JP', 55000, NULL, 'COMPLETED', 'PUBLIC', 0),
(30, 'Boxing Night: Las Vegas Series', '2025-08-20', 'INDEPENDENT', 'MGM Grand', 'Las Vegas', 'US', 16000, NULL, 'COMPLETED', 'PUBLIC', 0),
(31, 'fightnightManchester', '2026-12-16', 'WBC', 'Manchester Arena', 'Manchester, UK', NULL, NULL, NULL, 'SCHEDULED', 'PUBLIC', 0);

-- --------------------------------------------------------

--
-- Table structure for table `event_booking`
--

CREATE TABLE `event_booking` (
  `id` int(11) NOT NULL,
  `booking_status` varchar(20) NOT NULL,
  `ticket_quantity` int(11) NOT NULL DEFAULT 1,
  `total_price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `ticket_type` varchar(30) NOT NULL,
  `booking_date` date NOT NULL,
  `booking_reference` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `event_booking`
--

INSERT INTO `event_booking` (`id`, `booking_status`, `ticket_quantity`, `total_price`, `ticket_type`, `booking_date`, `booking_reference`, `created_at`, `updated_at`, `event_id`, `user_id`) VALUES
(1, 'CONFIRMED', 4, 266.68, 'VIP_RINGSIDE', '2026-04-25', 'SF-E4CF6719', '2026-04-25 11:34:31', '2026-04-25 11:34:31', 26, 9),
(2, 'CONFIRMED', 1, 300.00, 'VIP_RINGSIDE', '2026-04-26', 'SF-EBB26DA8', '2026-04-26 22:31:18', '2026-04-26 22:31:18', 27, 9);

-- --------------------------------------------------------

--
-- Table structure for table `fan_preference`
--

CREATE TABLE `fan_preference` (
  `id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `fan_id` int(11) NOT NULL,
  `favorite_discipline_id` int(11) DEFAULT NULL,
  `favorite_fighter_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `fan_profile`
--

CREATE TABLE `fan_profile` (
  `id` int(11) NOT NULL,
  `favorite_sport` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `bio` longtext DEFAULT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `fan_reaction`
--

CREATE TABLE `fan_reaction` (
  `id` int(11) NOT NULL,
  `reaction_type` varchar(20) NOT NULL,
  `comment` varchar(140) DEFAULT NULL,
  `reacted_at` datetime NOT NULL,
  `is_pinned` tinyint(4) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0,
  `fight_result_id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fan_reaction`
--

INSERT INTO `fan_reaction` (`id`, `reaction_type`, `comment`, `reacted_at`, `is_pinned`, `is_deleted`, `fight_result_id`, `fan_id`) VALUES
(1, 'FIRE', 'what a fight', '2026-04-26 19:24:18', 0, 0, 5, 9),
(2, 'DOMINANT', 'wow', '2026-04-26 20:07:59', 0, 0, 1, 9);

-- --------------------------------------------------------

--
-- Table structure for table `fan_vote`
--

CREATE TABLE `fan_vote` (
  `id` int(11) NOT NULL,
  `voted_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `match_proposal_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fan_vote`
--

INSERT INTO `fan_vote` (`id`, `voted_at`, `user_id`, `match_proposal_id`) VALUES
(1, '2026-04-26 20:06:50', 9, 1);

-- --------------------------------------------------------

--
-- Table structure for table `fighters`
--

CREATE TABLE `fighters` (
  `fighterId` int(11) NOT NULL,
  `firstName` varchar(100) NOT NULL,
  `lastName` varchar(100) NOT NULL,
  `nickname` varchar(100) DEFAULT NULL,
  `nationality` varchar(2) DEFAULT NULL,
  `photo_filename` varchar(255) DEFAULT NULL,
  `wins` int(11) NOT NULL DEFAULT 0,
  `losses` int(11) NOT NULL DEFAULT 0,
  `draws` int(11) NOT NULL DEFAULT 0,
  `koWins` int(11) NOT NULL DEFAULT 0,
  `technical_wins` int(11) NOT NULL DEFAULT 0,
  `decisionWins` int(11) NOT NULL DEFAULT 0,
  `eloRating` double NOT NULL DEFAULT 1500,
  `performanceScore` double NOT NULL DEFAULT 0,
  `winStreak` int(11) NOT NULL DEFAULT 0,
  `strengthOfSchedule` double NOT NULL DEFAULT 1500,
  `last_fight_date` date DEFAULT NULL,
  `titleDefenses` int(11) NOT NULL DEFAULT 0,
  `height` int(11) DEFAULT NULL,
  `reach` int(11) DEFAULT NULL,
  `weight_division_id` int(11) DEFAULT NULL,
  `strikes_thrown` int(11) NOT NULL DEFAULT 0,
  `strikes_landed` int(11) NOT NULL DEFAULT 0,
  `ai_style_tag` varchar(100) DEFAULT NULL,
  `ai_description` longtext DEFAULT NULL,
  `strength` varchar(255) DEFAULT NULL,
  `weakness` varchar(255) DEFAULT NULL,
  `weight` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fighters`
--

INSERT INTO `fighters` (`fighterId`, `firstName`, `lastName`, `nickname`, `nationality`, `photo_filename`, `wins`, `losses`, `draws`, `koWins`, `technical_wins`, `decisionWins`, `eloRating`, `performanceScore`, `winStreak`, `strengthOfSchedule`, `last_fight_date`, `titleDefenses`, `height`, `reach`, `weight_division_id`, `strikes_thrown`, `strikes_landed`, `ai_style_tag`, `ai_description`, `strength`, `weakness`, `weight`) VALUES
(1, 'Oleksandr', 'Usyk', 'The Cat', 'UA', '69ec7c0008473.jpg', 23, 0, 0, 15, 0, 0, 524, 26.05, 1, 585.6, '2026-04-26', 0, 191, 198, 1, 4800, 2300, NULL, NULL, NULL, NULL, NULL),
(2, 'Tyson', 'Fury', 'The Gypsy King', 'GB', '69ec7cae2c64d.jpg', 35, 1, 1, 24, 0, 1, 512, 0, 1, 585.6, '2026-04-26', 0, 206, 216, 1, 5000, 2100, NULL, NULL, NULL, NULL, NULL),
(3, 'Anthony', 'Joshua', 'AJ', 'GB', '69ec7ba64334b.jpg', 28, 3, 0, 25, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 198, 208, 1, 4200, 1900, NULL, NULL, NULL, NULL, NULL),
(4, 'Daniel', 'Dubois', 'Dynamite', 'GB', NULL, 22, 2, 0, 20, 0, 1, 514.4, 35.15, 1, 585.6, '2026-04-26', 0, 196, 198, 1, 3200, 1400, NULL, NULL, NULL, NULL, NULL),
(5, 'Zhilei', 'Zhang', 'Big Bang', 'CN', '69ee15449e873.jpg', 27, 3, 1, 22, 0, 0, 488, 0, 0, 409.6, '2026-04-26', 0, 198, 198, 1, 3800, 1600, NULL, NULL, NULL, NULL, NULL),
(6, 'Joseph', 'Parker', 'Joe', 'NZ', NULL, 35, 3, 0, 23, 0, 0, 500, 0, 0, 1000, '2025-11-25', 0, 193, 193, 1, 4100, 1800, NULL, NULL, NULL, NULL, NULL),
(7, 'Martin', 'Bakole', 'Bakole', 'CD', NULL, 21, 2, 0, 16, 0, 0, 488, 34.81, 0, 411.52, '2026-04-26', 0, 198, 203, 1, 3100, 1300, NULL, NULL, NULL, NULL, NULL),
(8, 'Agit', 'Kabayel', 'Kabayel', 'DE', '69ee36d809916.jpg', 25, 1, 0, 17, 0, 0, 488, 36.32, 0, 419.2, '2026-04-26', 0, 191, 191, 1, 3500, 1500, NULL, NULL, NULL, NULL, NULL),
(9, 'Artur', 'Beterbiev', 'Artur', 'RU', '69ee15263084c.jpg', 20, 1, 0, 20, 0, 0, 488, 0, 0, 411.52, '2026-04-26', 0, 182, 185, 3, 3100, 1400, NULL, NULL, NULL, NULL, NULL),
(10, 'Dmitry', 'Bivol', 'Bivol', 'RU', '69ee1585b2828.jpg', 24, 0, 0, 12, 0, 1, 514.4, 0, 1, 585.6, '2026-04-26', 0, 183, 183, 3, 4200, 1900, NULL, NULL, NULL, NULL, NULL),
(11, 'David', 'Benavidez', 'The Mexican Monster', 'US', NULL, 29, 0, 0, 24, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 188, 189, 3, 4500, 2100, NULL, NULL, NULL, NULL, NULL),
(12, 'Canelo', 'Alvarez', 'Canelo', 'MX', '69ec7bbcbfa87.jpg', 61, 2, 2, 39, 0, 0, 500, 0, 0, 1000, '2025-11-25', 0, 173, 179, 4, 7000, 3200, NULL, NULL, NULL, NULL, NULL),
(13, 'Caleb', 'Plant', 'Sweethands', 'US', NULL, 22, 2, 0, 13, 0, 0, 500, 0, 0, 1000, '2025-12-25', 0, 185, 188, 4, 4100, 1700, NULL, NULL, NULL, NULL, NULL),
(14, 'Christian', 'Mbilli', 'Solid', 'FR', NULL, 27, 0, 0, 23, 0, 0, 500, 0, 0, 1000, '2025-11-25', 0, 174, 178, 4, 3200, 1300, NULL, NULL, NULL, NULL, NULL),
(15, 'Janibek', 'Alimkhanuly', 'Qazaq Style', 'KZ', NULL, 15, 0, 0, 10, 0, 0, 500, 0, 0, 1000, '2026-03-25', 0, 182, 182, 5, 2800, 1100, NULL, NULL, NULL, NULL, NULL),
(16, 'Carlos', 'Adames', 'The Caballo', 'DO', NULL, 23, 1, 0, 18, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 180, 180, 5, 3100, 1300, NULL, NULL, NULL, NULL, NULL),
(17, 'Terence', 'Crawford', 'Bud', 'US', NULL, 41, 0, 0, 31, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 173, 188, 6, 5200, 2400, NULL, NULL, NULL, NULL, NULL),
(18, 'Sebastian', 'Fundora', 'The Towering Inferno', 'US', NULL, 21, 1, 1, 13, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 197, 203, 6, 3500, 1500, NULL, NULL, NULL, NULL, NULL),
(19, 'Tim', 'Tszyu', 'The Soul Taker', 'AU', NULL, 24, 1, 0, 17, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 174, 179, 6, 4200, 1800, NULL, NULL, NULL, NULL, NULL),
(20, 'Jaron', 'Ennis', 'Boots', 'US', NULL, 32, 0, 0, 29, 0, 0, 500, 0, 0, 1000, '2026-03-25', 0, 178, 188, 7, 3900, 1800, NULL, NULL, NULL, NULL, NULL),
(21, 'Errol', 'Spence Jr.', 'The Truth', 'US', NULL, 28, 1, 0, 22, 0, 0, 500, 0, 0, 1000, '2025-12-25', 0, 177, 183, 7, 4500, 1900, NULL, NULL, NULL, NULL, NULL),
(22, 'Gervonta', 'Davis', 'Tank', 'US', '69ec7bdd2882c.jpg', 30, 0, 0, 28, 0, 0, 500, 0, 0, 1000, '2025-10-25', 0, 166, 171, 9, 3800, 1600, NULL, NULL, NULL, NULL, NULL),
(23, 'Shakur', 'Stevenson', 'Sugar', 'US', '69ec7c7c15ec9.jpg', 22, 0, 0, 10, 0, 0, 500, 0, 0, 1000, '2025-11-25', 0, 173, 173, 9, 4200, 1800, NULL, NULL, NULL, NULL, NULL),
(24, 'Vasiliy', 'Lomachenko', 'The Matrix', 'UA', '69ee1570ad943.jpg', 18, 3, 0, 12, 0, 0, 500, 0, 0, 1000, '2025-10-25', 0, 170, 166, 9, 5500, 2600, NULL, NULL, NULL, NULL, NULL),
(25, 'Teofimo', 'Lopez', 'The Takeover', 'US', NULL, 21, 1, 0, 13, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 173, 174, 8, 3800, 1500, NULL, NULL, NULL, NULL, NULL),
(26, 'Devin', 'Haney', 'The Dream', 'US', NULL, 31, 0, 0, 15, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 173, 180, 8, 4500, 1900, NULL, NULL, NULL, NULL, NULL),
(27, 'Ryan', 'Garcia', 'KingRy', 'US', NULL, 24, 1, 0, 20, 0, 0, 500, 0, 0, 1000, '2025-10-25', 0, 174, 178, 8, 3800, 1500, NULL, NULL, NULL, NULL, NULL),
(28, 'Isaac', 'Cruz', 'Pitbull', 'MX', NULL, 26, 2, 1, 18, 0, 0, 500, 0, 0, 1000, '2026-01-25', 0, 163, 160, 8, 3200, 1400, NULL, NULL, NULL, NULL, NULL),
(29, 'Naoya', 'Inoue', 'The Monster', 'JP', NULL, 27, 0, 0, 24, 0, 0, 500, 0, 0, 1000, '2025-11-25', 0, 165, 171, 12, 3500, 1700, NULL, NULL, NULL, NULL, NULL),
(30, 'Jesse', 'Rodriguez', 'Bam', 'US', NULL, 20, 0, 0, 13, 0, 0, 500, 0, 0, 1000, '2026-02-25', 0, 163, 160, 14, 3200, 1400, NULL, NULL, NULL, NULL, NULL),
(31, 'Junto', 'Nakatani', 'Junto', 'JP', NULL, 28, 0, 0, 21, 0, 0, 500, 0, 0, 1000, '2025-12-25', 0, 172, 170, 13, 3500, 1600, NULL, NULL, NULL, NULL, NULL),
(32, 'Emanuel', 'Navarrete', 'El Vaquero', 'MX', NULL, 38, 2, 1, 31, 0, 0, 500, 0, 0, 1000, '2025-12-25', 0, 170, 183, 10, 5500, 2200, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `fighter_contract`
--

CREATE TABLE `fighter_contract` (
  `id` int(11) NOT NULL,
  `base_pay` double NOT NULL,
  `win_bonus` double NOT NULL,
  `calculated_payout` double DEFAULT NULL,
  `is_paid` tinyint(4) NOT NULL DEFAULT 0,
  `fighter_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fighter_contract`
--

INSERT INTO `fighter_contract` (`id`, `base_pay`, `win_bonus`, `calculated_payout`, `is_paid`, `fighter_id`, `event_id`) VALUES
(2, 20, 25, NULL, 0, 7, 4),
(3, 100, 150, NULL, 0, 2, 5),
(4, 1, 2, NULL, 0, 9, 31);

-- --------------------------------------------------------

--
-- Table structure for table `fight_results`
--

CREATE TABLE `fight_results` (
  `resultId` int(11) NOT NULL,
  `fightNumber` int(11) NOT NULL,
  `methodOfVictory` varchar(50) DEFAULT NULL,
  `decision_type` varchar(10) DEFAULT NULL,
  `knockdown_round` int(11) DEFAULT NULL,
  `roundNumber` int(11) DEFAULT NULL,
  `scheduled_rounds` int(11) NOT NULL DEFAULT 12,
  `is_belt_fight` tinyint(4) NOT NULL DEFAULT 0,
  `belt_organization` varchar(20) DEFAULT NULL,
  `fighter1_odds` double DEFAULT NULL,
  `fighter2_odds` double DEFAULT NULL,
  `fightDate` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'SCHEDULED',
  `eventId` int(11) NOT NULL,
  `fighter1Id` int(11) NOT NULL,
  `fighter2Id` int(11) NOT NULL,
  `winnerId` int(11) DEFAULT NULL,
  `inside_the_numbers` longtext DEFAULT NULL,
  `highlight_video_url` varchar(255) DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fight_results`
--

INSERT INTO `fight_results` (`resultId`, `fightNumber`, `methodOfVictory`, `decision_type`, `knockdown_round`, `roundNumber`, `scheduled_rounds`, `is_belt_fight`, `belt_organization`, `fighter1_odds`, `fighter2_odds`, `fightDate`, `status`, `eventId`, `fighter1Id`, `fighter2Id`, `winnerId`, `inside_the_numbers`, `highlight_video_url`, `video_path`, `updated_at`) VALUES
(1, 1, 'KO', NULL, NULL, 9, 12, 0, NULL, NULL, NULL, '2026-04-26 17:04:28', 'COMPLETED', 26, 1, 8, 1, NULL, NULL, NULL, NULL),
(2, 2, 'DECISION', 'UD', NULL, 12, 12, 0, NULL, NULL, NULL, '2026-04-26 20:05:00', 'COMPLETED', 26, 4, 7, 4, NULL, '', '69ee53ccab6b1867745517.mp4', '2026-04-26 20:05:00'),
(3, 3, NULL, NULL, NULL, NULL, 12, 0, NULL, NULL, NULL, NULL, 'SCHEDULED', 26, 3, 5, NULL, NULL, NULL, NULL, NULL),
(5, 1, 'DECISION', 'SD', NULL, 12, 12, 0, NULL, NULL, NULL, '2026-04-26 19:12:58', 'COMPLETED', 31, 10, 9, 10, NULL, '', '69ee479a6f2f9739898809.mp4', '2026-04-26 19:12:58'),
(6, 2, 'DECISION', 'SD', NULL, 12, 12, 0, NULL, NULL, NULL, '2026-04-26 22:37:03', 'COMPLETED', 31, 2, 5, 2, NULL, '', '69ee776fbdf7f818355088.mp4', '2026-04-26 22:37:03');

-- --------------------------------------------------------

--
-- Table structure for table `fight_statistic`
--

CREATE TABLE `fight_statistic` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `round` int(11) DEFAULT NULL,
  `punches_landed` int(11) NOT NULL DEFAULT 0,
  `punches_thrown` int(11) NOT NULL DEFAULT 0,
  `jabs_landed` int(11) NOT NULL DEFAULT 0,
  `jabs_thrown` int(11) NOT NULL DEFAULT 0,
  `power_punches_landed` int(11) NOT NULL DEFAULT 0,
  `power_punches_thrown` int(11) NOT NULL DEFAULT 0,
  `knockdowns` int(11) NOT NULL DEFAULT 0,
  `body_shots_landed` int(11) NOT NULL DEFAULT 0,
  `body_jabs_landed` int(11) NOT NULL DEFAULT 0,
  `body_power_landed` int(11) NOT NULL DEFAULT 0,
  `uppercuts_landed` int(11) NOT NULL DEFAULT 0,
  `uppercuts_thrown` int(11) NOT NULL DEFAULT 0,
  `right_hand_landed` int(11) NOT NULL DEFAULT 0,
  `right_hand_thrown` int(11) NOT NULL DEFAULT 0,
  `left_hand_landed` int(11) NOT NULL DEFAULT 0,
  `left_hand_thrown` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fight_statistic`
--

INSERT INTO `fight_statistic` (`id`, `fight_result_id`, `fighter_id`, `round`, `punches_landed`, `punches_thrown`, `jabs_landed`, `jabs_thrown`, `power_punches_landed`, `power_punches_thrown`, `knockdowns`, `body_shots_landed`, `body_jabs_landed`, `body_power_landed`, `uppercuts_landed`, `uppercuts_thrown`, `right_hand_landed`, `right_hand_thrown`, `left_hand_landed`, `left_hand_thrown`) VALUES
(1, 1, 1, 1, 17, 54, 6, 22, 11, 32, 0, 3, 0, 0, 1, 5, 8, 28, 9, 26),
(2, 1, 8, 1, 29, 68, 9, 28, 20, 40, 0, 5, 0, 0, 2, 6, 16, 40, 13, 28),
(3, 1, 1, 2, 19, 70, 6, 28, 13, 42, 0, 3, 0, 0, 1, 7, 11, 37, 8, 33),
(4, 1, 8, 2, 15, 49, 5, 20, 10, 29, 0, 3, 0, 0, 1, 4, 7, 29, 8, 20),
(5, 1, 1, 3, 23, 53, 7, 22, 16, 31, 0, 4, 0, 0, 2, 5, 12, 26, 11, 27),
(6, 1, 8, 3, 28, 69, 9, 28, 19, 41, 0, 5, 0, 0, 2, 6, 15, 38, 13, 31),
(7, 1, 1, 5, 20, 48, 6, 20, 14, 28, 0, 4, 0, 0, 2, 4, 10, 24, 10, 24),
(8, 1, 8, 5, 33, 77, 10, 31, 23, 46, 0, 6, 0, 0, 3, 7, 17, 42, 16, 35),
(9, 1, 1, 7, 21, 68, 7, 28, 14, 40, 0, 4, 0, 0, 2, 6, 11, 34, 10, 34),
(10, 1, 8, 7, 20, 52, 6, 21, 14, 31, 0, 4, 0, 0, 2, 5, 11, 30, 9, 22),
(11, 1, 1, 9, 18, 61, 6, 25, 12, 36, 0, 3, 0, 0, 1, 6, 10, 35, 8, 26),
(12, 1, 8, 9, 25, 63, 8, 26, 17, 37, 0, 5, 0, 0, 2, 6, 14, 34, 11, 29),
(13, 2, 4, 1, 28, 67, 9, 27, 19, 40, 0, 5, 0, 0, 2, 6, 15, 36, 13, 31),
(14, 2, 7, 1, 34, 82, 11, 33, 23, 49, 0, 6, 0, 0, 3, 8, 18, 49, 16, 33),
(15, 2, 4, 2, 31, 78, 10, 32, 21, 46, 0, 6, 0, 0, 3, 7, 17, 46, 14, 32),
(16, 2, 7, 2, 24, 69, 8, 28, 16, 41, 0, 4, 0, 0, 2, 6, 13, 38, 11, 31),
(17, 2, 4, 4, 19, 71, 6, 29, 13, 42, 0, 3, 0, 0, 1, 7, 10, 36, 9, 35),
(18, 2, 7, 4, 35, 79, 11, 32, 24, 47, 0, 7, 0, 0, 3, 7, 20, 45, 15, 34),
(19, 2, 4, 5, 25, 71, 8, 29, 17, 42, 0, 5, 0, 0, 2, 7, 14, 39, 11, 32),
(20, 2, 7, 5, 26, 64, 8, 26, 18, 38, 0, 5, 0, 0, 2, 6, 13, 37, 13, 27);

-- --------------------------------------------------------

--
-- Table structure for table `match_proposal`
--

CREATE TABLE `match_proposal` (
  `id` int(11) NOT NULL,
  `compatibility` decimal(5,2) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `proposed_at` datetime NOT NULL,
  `notes` longtext DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `fighter1_id` int(11) NOT NULL,
  `fighter2_id` int(11) NOT NULL,
  `vote_count` int(11) NOT NULL DEFAULT 0,
  `weight_division_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `match_proposal`
--

INSERT INTO `match_proposal` (`id`, `compatibility`, `status`, `proposed_at`, `notes`, `event_id`, `fighter1_id`, `fighter2_id`, `vote_count`, `weight_division_id`) VALUES
(1, NULL, 'PENDING', '2026-04-25 11:17:19', NULL, NULL, 9, 10, 1, 3),
(2, NULL, 'PENDING', '2026-04-26 15:25:55', NULL, NULL, 12, 14, 0, 4),
(3, NULL, 'PENDING', '2026-04-26 20:06:15', NULL, NULL, 1, 2, 0, 1),
(4, 95.00, 'PENDING', '2026-04-26 22:54:16', 'Zhilei Zhang (27-3, ELO 488) vs Martin Bakole (21-2, ELO 488) — Heavyweight division clash with 0 ELO gap. An extremely competitive matchup!', NULL, 5, 7, 0, NULL),
(5, 95.00, 'PENDING', '2026-04-26 22:54:16', 'Agit Kabayel (25-1, ELO 488) vs Anthony Joshua (28-3, ELO 500) — Heavyweight division clash with 12 ELO gap. An extremely competitive matchup!', NULL, 8, 3, 0, NULL),
(6, 95.00, 'PENDING', '2026-04-26 22:54:16', 'Joseph Parker (35-3, ELO 500) vs Tyson Fury (35-1, ELO 512) — Heavyweight division clash with 12 ELO gap. An extremely competitive matchup!', NULL, 6, 2, 0, NULL),
(7, 95.00, 'PENDING', '2026-04-26 22:54:16', 'Daniel Dubois (22-2, ELO 514) vs Oleksandr Usyk (23-0, ELO 524) — Heavyweight division clash with 9 ELO gap. An extremely competitive matchup!', NULL, 4, 1, 0, NULL),
(8, 95.00, 'PENDING', '2026-04-26 22:54:16', 'Artur Beterbiev (20-1, ELO 488) vs David Benavidez (29-0, ELO 500) — Light Heavyweight division clash with 12 ELO gap. An extremely competitive matchup!', NULL, 9, 11, 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notificationId` int(11) NOT NULL,
  `message` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `is_read` tinyint(4) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `userId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notificationId`, `message`, `created_at`, `is_read`, `type`, `userId`) VALUES
(1, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 1),
(2, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 2),
(3, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 3),
(4, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 4),
(5, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 5),
(6, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 6),
(7, 'New article published: The Rise. The Punch. The Fall of a Giant.. Check it out!', '2026-04-25 11:16:10', 0, 'NEW_ARTICLE', 7),
(9, 'Your booking for Boxing Night: Riyadh Series (Ref: SF-E4CF6719) is confirmed!', '2026-04-25 11:34:31', 0, 'BOOKING', 9),
(10, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 1),
(11, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 2),
(12, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 3),
(13, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 4),
(14, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 5),
(15, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 6),
(16, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 7),
(18, '🥊 World Rankings have been updated following Usyk vs Kabayel!', '2026-04-26 17:04:30', 0, 'RANKING', 9),
(19, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 1),
(20, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 2),
(21, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 3),
(22, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 4),
(23, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 5),
(24, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 6),
(25, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 7),
(27, '🥊 World Rankings have been updated following Bivol vs Beterbiev!', '2026-04-26 19:10:31', 0, 'RANKING', 9),
(28, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 1),
(29, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 2),
(30, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 3),
(31, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 4),
(32, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 5),
(33, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 6),
(34, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 7),
(36, '🥊 World Rankings have been updated following Dubois vs Bakole!', '2026-04-26 20:05:01', 0, 'RANKING', 9),
(37, 'Your booking for World Championship #4855 (Ref: SF-EBB26DA8) is confirmed!', '2026-04-26 22:31:19', 0, 'BOOKING', 9),
(38, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 1),
(39, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 2),
(40, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 3),
(41, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 4),
(42, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 5),
(43, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 6),
(44, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 7),
(45, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 8),
(46, '🥊 World Rankings have been updated following Fury vs Zhang!', '2026-04-26 22:37:04', 0, 'RANKING', 9);

-- --------------------------------------------------------

--
-- Table structure for table `performance_score`
--

CREATE TABLE `performance_score` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `score` double NOT NULL DEFAULT 0,
  `aggression` double DEFAULT NULL,
  `defense` double DEFAULT NULL,
  `technique` double DEFAULT NULL,
  `experience` double DEFAULT NULL,
  `calculated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `performance_score`
--

INSERT INTO `performance_score` (`id`, `fighter_id`, `score`, `aggression`, `defense`, `technique`, `experience`, `calculated_at`) VALUES
(1, 1, 0, NULL, NULL, NULL, NULL, '2026-04-26 17:04:29'),
(2, 8, 0, NULL, NULL, NULL, NULL, '2026-04-26 17:04:30'),
(3, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:24:16'),
(4, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:24:16'),
(5, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:24:23'),
(6, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:24:23'),
(7, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:24:27'),
(8, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:24:27'),
(9, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:24:52'),
(10, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:24:52'),
(11, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:25:00'),
(12, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:25:00'),
(13, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:25:15'),
(14, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:25:15'),
(15, 1, 26.05, NULL, NULL, NULL, NULL, '2026-04-26 17:25:22'),
(16, 8, 36.32, NULL, NULL, NULL, NULL, '2026-04-26 17:25:22'),
(17, 10, 0, NULL, NULL, NULL, NULL, '2026-04-26 19:10:31'),
(18, 9, 0, NULL, NULL, NULL, NULL, '2026-04-26 19:10:31'),
(19, 4, 0, NULL, NULL, NULL, NULL, '2026-04-26 20:05:00'),
(20, 7, 0, NULL, NULL, NULL, NULL, '2026-04-26 20:05:01'),
(21, 2, 0, NULL, NULL, NULL, NULL, '2026-04-26 22:37:04'),
(22, 5, 0, NULL, NULL, NULL, NULL, '2026-04-26 22:37:04'),
(23, 4, 35.15, NULL, NULL, NULL, NULL, '2026-04-26 22:40:47'),
(24, 7, 34.81, NULL, NULL, NULL, NULL, '2026-04-26 22:40:47'),
(25, 4, 35.15, NULL, NULL, NULL, NULL, '2026-04-26 22:40:59'),
(26, 7, 34.81, NULL, NULL, NULL, NULL, '2026-04-26 22:40:59'),
(27, 4, 35.15, NULL, NULL, NULL, NULL, '2026-04-26 22:41:15'),
(28, 7, 34.81, NULL, NULL, NULL, NULL, '2026-04-26 22:41:15'),
(29, 4, 35.15, NULL, NULL, NULL, NULL, '2026-04-26 22:42:15'),
(30, 7, 34.81, NULL, NULL, NULL, NULL, '2026-04-26 22:42:15');

-- --------------------------------------------------------

--
-- Table structure for table `predictions`
--

CREATE TABLE `predictions` (
  `predictionId` int(11) NOT NULL,
  `predicted_method` varchar(50) NOT NULL,
  `predicted_round` int(11) DEFAULT NULL,
  `is_processed` tinyint(4) NOT NULL DEFAULT 0,
  `points_awarded` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `userId` int(11) NOT NULL,
  `fightId` int(11) NOT NULL,
  `predictedWinnerId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `predictions`
--

INSERT INTO `predictions` (`predictionId`, `predicted_method`, `predicted_round`, `is_processed`, `points_awarded`, `created_at`, `userId`, `fightId`, `predictedWinnerId`) VALUES
(1, 'KO', NULL, 0, 0, '2026-04-26 20:08:25', 9, 3, 3);

-- --------------------------------------------------------

--
-- Table structure for table `public_key_credential_source`
--

CREATE TABLE `public_key_credential_source` (
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ranking`
--

CREATE TABLE `ranking` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `rank_position` int(11) NOT NULL DEFAULT 999,
  `points` double NOT NULL DEFAULT 0,
  `organization` varchar(20) NOT NULL DEFAULT 'MEDIA',
  `is_champion` tinyint(4) NOT NULL DEFAULT 0,
  `last_fight_date` date DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `weight_division_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ranking`
--

INSERT INTO `ranking` (`id`, `fighter_id`, `rank_position`, `points`, `organization`, `is_champion`, `last_fight_date`, `updated_at`, `weight_division_id`) VALUES
(481, 8, 0, 56.2, 'WBC', 1, '2026-04-26', '2026-04-26 22:37:04', 1),
(482, 1, 1, 52.03, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(483, 3, 2, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 1),
(484, 6, 3, 36.5, 'WBC', 0, '2025-11-25', '2026-04-26 22:37:04', 1),
(485, 4, 4, 31, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(486, 2, 5, 30.95, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(487, 7, 6, 26.99, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(488, 5, 7, 26.95, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(489, 8, 0, 56.2, 'WBA', 1, '2026-04-26', '2026-04-26 22:37:04', 1),
(490, 1, 1, 52.03, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(491, 3, 2, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 1),
(492, 6, 3, 36.5, 'WBA', 0, '2025-11-25', '2026-04-26 22:37:04', 1),
(493, 4, 4, 31, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(494, 2, 5, 30.95, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(495, 7, 6, 26.99, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(496, 5, 7, 26.95, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(497, 8, 0, 56.2, 'IBF', 1, '2026-04-26', '2026-04-26 22:37:04', 1),
(498, 1, 1, 52.03, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(499, 3, 2, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 1),
(500, 6, 3, 36.5, 'IBF', 0, '2025-11-25', '2026-04-26 22:37:04', 1),
(501, 4, 4, 31, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(502, 2, 5, 30.95, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(503, 7, 6, 26.99, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(504, 5, 7, 26.95, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(505, 8, 0, 56.2, 'WBO', 1, '2026-04-26', '2026-04-26 22:37:04', 1),
(506, 1, 1, 52.03, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(507, 3, 2, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 1),
(508, 6, 3, 36.5, 'WBO', 0, '2025-11-25', '2026-04-26 22:37:04', 1),
(509, 4, 4, 31, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(510, 2, 5, 30.95, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(511, 7, 6, 26.99, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(512, 5, 7, 26.95, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(513, 8, 0, 56.2, 'MEDIA', 1, '2026-04-26', '2026-04-26 22:37:04', 1),
(514, 1, 1, 52.03, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(515, 3, 2, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 1),
(516, 6, 3, 36.5, 'MEDIA', 0, '2025-11-25', '2026-04-26 22:37:04', 1),
(517, 4, 4, 31, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(518, 2, 5, 30.95, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(519, 7, 6, 26.99, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(520, 5, 7, 26.95, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 1),
(521, 11, 0, 37.5, 'WBC', 1, '2026-01-25', '2026-04-26 22:37:04', 3),
(522, 10, 1, 31, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(523, 9, 2, 26.99, 'WBC', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(524, 11, 0, 37.5, 'WBA', 1, '2026-01-25', '2026-04-26 22:37:04', 3),
(525, 10, 1, 31, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(526, 9, 2, 26.99, 'WBA', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(527, 11, 0, 37.5, 'IBF', 1, '2026-01-25', '2026-04-26 22:37:04', 3),
(528, 10, 1, 31, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(529, 9, 2, 26.99, 'IBF', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(530, 11, 0, 37.5, 'WBO', 1, '2026-01-25', '2026-04-26 22:37:04', 3),
(531, 10, 1, 31, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(532, 9, 2, 26.99, 'WBO', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(533, 11, 0, 37.5, 'MEDIA', 1, '2026-01-25', '2026-04-26 22:37:04', 3),
(534, 10, 1, 31, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(535, 9, 2, 26.99, 'MEDIA', 0, '2026-04-26', '2026-04-26 22:37:04', 3),
(536, 13, 0, 37, 'WBC', 1, '2025-12-25', '2026-04-26 22:37:04', 4),
(537, 12, 1, 36.5, 'WBC', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(538, 14, 2, 36.5, 'WBC', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(539, 13, 0, 37, 'WBA', 1, '2025-12-25', '2026-04-26 22:37:04', 4),
(540, 12, 1, 36.5, 'WBA', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(541, 14, 2, 36.5, 'WBA', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(542, 13, 0, 37, 'IBF', 1, '2025-12-25', '2026-04-26 22:37:04', 4),
(543, 12, 1, 36.5, 'IBF', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(544, 14, 2, 36.5, 'IBF', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(545, 13, 0, 37, 'WBO', 1, '2025-12-25', '2026-04-26 22:37:04', 4),
(546, 12, 1, 36.5, 'WBO', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(547, 14, 2, 36.5, 'WBO', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(548, 13, 0, 37, 'MEDIA', 1, '2025-12-25', '2026-04-26 22:37:04', 4),
(549, 12, 1, 36.5, 'MEDIA', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(550, 14, 2, 36.5, 'MEDIA', 0, '2025-11-25', '2026-04-26 22:37:04', 4),
(551, 15, 0, 38.5, 'WBC', 1, '2026-03-25', '2026-04-26 22:37:04', 5),
(552, 16, 1, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 5),
(553, 15, 0, 38.5, 'WBA', 1, '2026-03-25', '2026-04-26 22:37:04', 5),
(554, 16, 1, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 5),
(555, 15, 0, 38.5, 'IBF', 1, '2026-03-25', '2026-04-26 22:37:04', 5),
(556, 16, 1, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 5),
(557, 15, 0, 38.5, 'WBO', 1, '2026-03-25', '2026-04-26 22:37:04', 5),
(558, 16, 1, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 5),
(559, 15, 0, 38.5, 'MEDIA', 1, '2026-03-25', '2026-04-26 22:37:04', 5),
(560, 16, 1, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 5),
(561, 17, 0, 37.5, 'WBC', 1, '2026-01-25', '2026-04-26 22:37:04', 6),
(562, 18, 1, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(563, 19, 2, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(564, 17, 0, 37.5, 'WBA', 1, '2026-01-25', '2026-04-26 22:37:04', 6),
(565, 18, 1, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(566, 19, 2, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(567, 17, 0, 37.5, 'IBF', 1, '2026-01-25', '2026-04-26 22:37:04', 6),
(568, 18, 1, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(569, 19, 2, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(570, 17, 0, 37.5, 'WBO', 1, '2026-01-25', '2026-04-26 22:37:04', 6),
(571, 18, 1, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(572, 19, 2, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(573, 17, 0, 37.5, 'MEDIA', 1, '2026-01-25', '2026-04-26 22:37:04', 6),
(574, 18, 1, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(575, 19, 2, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 6),
(576, 20, 0, 38.5, 'WBC', 1, '2026-03-25', '2026-04-26 22:37:04', 7),
(577, 21, 1, 37, 'WBC', 0, '2025-12-25', '2026-04-26 22:37:04', 7),
(578, 20, 0, 38.5, 'WBA', 1, '2026-03-25', '2026-04-26 22:37:04', 7),
(579, 21, 1, 37, 'WBA', 0, '2025-12-25', '2026-04-26 22:37:04', 7),
(580, 20, 0, 38.5, 'IBF', 1, '2026-03-25', '2026-04-26 22:37:04', 7),
(581, 21, 1, 37, 'IBF', 0, '2025-12-25', '2026-04-26 22:37:04', 7),
(582, 20, 0, 38.5, 'WBO', 1, '2026-03-25', '2026-04-26 22:37:04', 7),
(583, 21, 1, 37, 'WBO', 0, '2025-12-25', '2026-04-26 22:37:04', 7),
(584, 20, 0, 38.5, 'MEDIA', 1, '2026-03-25', '2026-04-26 22:37:04', 7),
(585, 21, 1, 37, 'MEDIA', 0, '2025-12-25', '2026-04-26 22:37:04', 7),
(586, 23, 0, 36.5, 'WBC', 1, '2025-11-25', '2026-04-26 22:37:04', 9),
(587, 22, 1, 36, 'WBC', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(588, 24, 2, 36, 'WBC', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(589, 23, 0, 36.5, 'WBA', 1, '2025-11-25', '2026-04-26 22:37:04', 9),
(590, 22, 1, 36, 'WBA', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(591, 24, 2, 36, 'WBA', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(592, 23, 0, 36.5, 'IBF', 1, '2025-11-25', '2026-04-26 22:37:04', 9),
(593, 22, 1, 36, 'IBF', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(594, 24, 2, 36, 'IBF', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(595, 23, 0, 36.5, 'WBO', 1, '2025-11-25', '2026-04-26 22:37:04', 9),
(596, 22, 1, 36, 'WBO', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(597, 24, 2, 36, 'WBO', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(598, 23, 0, 36.5, 'MEDIA', 1, '2025-11-25', '2026-04-26 22:37:04', 9),
(599, 22, 1, 36, 'MEDIA', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(600, 24, 2, 36, 'MEDIA', 0, '2025-10-25', '2026-04-26 22:37:04', 9),
(601, 25, 0, 37.5, 'WBC', 1, '2026-01-25', '2026-04-26 22:37:04', 8),
(602, 26, 1, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(603, 28, 2, 37.5, 'WBC', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(604, 27, 3, 36, 'WBC', 0, '2025-10-25', '2026-04-26 22:37:04', 8),
(605, 25, 0, 37.5, 'WBA', 1, '2026-01-25', '2026-04-26 22:37:04', 8),
(606, 26, 1, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(607, 28, 2, 37.5, 'WBA', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(608, 27, 3, 36, 'WBA', 0, '2025-10-25', '2026-04-26 22:37:04', 8),
(609, 25, 0, 37.5, 'IBF', 1, '2026-01-25', '2026-04-26 22:37:04', 8),
(610, 26, 1, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(611, 28, 2, 37.5, 'IBF', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(612, 27, 3, 36, 'IBF', 0, '2025-10-25', '2026-04-26 22:37:04', 8),
(613, 25, 0, 37.5, 'WBO', 1, '2026-01-25', '2026-04-26 22:37:04', 8),
(614, 26, 1, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(615, 28, 2, 37.5, 'WBO', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(616, 27, 3, 36, 'WBO', 0, '2025-10-25', '2026-04-26 22:37:04', 8),
(617, 25, 0, 37.5, 'MEDIA', 1, '2026-01-25', '2026-04-26 22:37:04', 8),
(618, 26, 1, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(619, 28, 2, 37.5, 'MEDIA', 0, '2026-01-25', '2026-04-26 22:37:04', 8),
(620, 27, 3, 36, 'MEDIA', 0, '2025-10-25', '2026-04-26 22:37:04', 8),
(621, 29, 0, 36.5, 'WBC', 1, '2025-11-25', '2026-04-26 22:37:04', 12),
(622, 29, 0, 36.5, 'WBA', 1, '2025-11-25', '2026-04-26 22:37:04', 12),
(623, 29, 0, 36.5, 'IBF', 1, '2025-11-25', '2026-04-26 22:37:04', 12),
(624, 29, 0, 36.5, 'WBO', 1, '2025-11-25', '2026-04-26 22:37:04', 12),
(625, 29, 0, 36.5, 'MEDIA', 1, '2025-11-25', '2026-04-26 22:37:04', 12),
(626, 30, 0, 38, 'WBC', 1, '2026-02-25', '2026-04-26 22:37:04', 14),
(627, 30, 0, 38, 'WBA', 1, '2026-02-25', '2026-04-26 22:37:04', 14),
(628, 30, 0, 38, 'IBF', 1, '2026-02-25', '2026-04-26 22:37:04', 14),
(629, 30, 0, 38, 'WBO', 1, '2026-02-25', '2026-04-26 22:37:04', 14),
(630, 30, 0, 38, 'MEDIA', 1, '2026-02-25', '2026-04-26 22:37:04', 14),
(631, 31, 0, 37, 'WBC', 1, '2025-12-25', '2026-04-26 22:37:04', 13),
(632, 31, 0, 37, 'WBA', 1, '2025-12-25', '2026-04-26 22:37:04', 13),
(633, 31, 0, 37, 'IBF', 1, '2025-12-25', '2026-04-26 22:37:04', 13),
(634, 31, 0, 37, 'WBO', 1, '2025-12-25', '2026-04-26 22:37:04', 13),
(635, 31, 0, 37, 'MEDIA', 1, '2025-12-25', '2026-04-26 22:37:04', 13),
(636, 32, 0, 37, 'WBC', 1, '2025-12-25', '2026-04-26 22:37:04', 10),
(637, 32, 0, 37, 'WBA', 1, '2025-12-25', '2026-04-26 22:37:04', 10),
(638, 32, 0, 37, 'IBF', 1, '2025-12-25', '2026-04-26 22:37:04', 10),
(639, 32, 0, 37, 'WBO', 1, '2025-12-25', '2026-04-26 22:37:04', 10),
(640, 32, 0, 37, 'MEDIA', 1, '2025-12-25', '2026-04-26 22:37:04', 10);

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `roleId` int(11) NOT NULL,
  `roleName` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`roleId`, `roleName`) VALUES
(1, 'ADMIN'),
(2, 'ADMIN'),
(3, 'USER'),
(4, 'ADMIN'),
(5, 'USER'),
(6, 'ADMIN'),
(7, 'USER'),
(8, 'ADMIN'),
(9, 'USER'),
(10, 'ADMIN'),
(11, 'USER'),
(12, 'ADMIN'),
(13, 'USER'),
(14, 'ADMIN'),
(15, 'USER'),
(16, 'ADMIN'),
(17, 'USER'),
(18, 'ADMIN'),
(19, 'USER'),
(20, 'ADMIN'),
(21, 'USER'),
(22, 'ADMIN'),
(23, 'USER'),
(24, 'ADMIN'),
(25, 'USER'),
(26, 'ADMIN'),
(27, 'USER'),
(28, 'ADMIN'),
(29, 'USER');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `userId` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(200) DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `predictionPoints` int(11) NOT NULL DEFAULT 0,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expires_at` datetime DEFAULT NULL,
  `is_verified` tinyint(4) NOT NULL DEFAULT 0,
  `verification_token` varchar(255) DEFAULT NULL,
  `webauthn_credential_id` varchar(255) DEFAULT NULL,
  `webauthn_public_key` longtext DEFAULT NULL,
  `face_photo` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`userId`, `username`, `password`, `email`, `createdDate`, `predictionPoints`, `reset_token`, `reset_token_expires_at`, `is_verified`, `verification_token`, `webauthn_credential_id`, `webauthn_public_key`, `face_photo`) VALUES
(1, 'admin', '$2y$13$9drKDJXvvs9fcbohPk3CIucMy9yOMLMOlzM4mDKdpTucHxtFD3cei', 'admin@smartfight.com', '2026-04-25 10:14:00', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(2, 'john_doe', '$2y$13$xkCLO2vgcyiuIuzjnHHcUOeGJcrbe9VXkfam0wEpuvYqMtSQ9Gphm', 'john@example.com', '2026-04-25 10:14:01', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(3, 'jane_smith', '$2y$13$fZmHmFgzeUo1mUSnwGXYeOcOQ5okqbuOGQJkjOwhuHIjkcSpAuYkK', 'jane@example.com', '2026-04-25 10:14:01', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(4, 'mike_tyson', '$2y$13$krgL4Pst0suSi/GIru2eVeFYDONy4eOslOB4JGHhc/Y88oQA5Q85C', 'mike@example.com', '2026-04-25 10:14:02', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(5, 'ali_fan', '$2y$13$/8NXIOPmzA/fXzyra93E/.kP2E2H9eu0NqXbXSSnlNJTyQglqTZWS', 'ali@example.com', '2026-04-25 10:14:02', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(6, 'boxerfan1', '$2y$13$Hq6SbgSU7/tqfwcyySoO1uB1x0x3g2MGkpQ/DcVEEm.53yGwrrOfa', 'fan1@smartfight.com', '2026-04-25 10:14:03', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(7, 'boxerfan2', '$2y$13$OqV.wA8BJk4ZqRdMQlPnN.2dVvv5HFwdpJOUSsbt6nyiNirRJ3YmK', 'fan2@smartfight.com', '2026-04-25 10:14:03', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(8, 'mahdi', '$2y$13$9ZpwpjkpRJf16l/xkBYdLOBa9P4UhpY0..w6V/qu2mBj9GiPxGj42', 'mahdi@smartfight.com', '2026-04-25 10:14:03', 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(9, 'Daly Mahdi', '$2y$13$PGUuPpblNMUaixgiHKTE9eq5XzAIVkPeM11ZlssBS6pB0Sp8qDef2', 'mahdidaly24@gmail.com', '2026-04-25 11:33:43', 0, '55c6349fc069723b8fafb6191cba03028d89dbe4e45a88d504780ac379b12736', '2026-04-26 21:32:10', 1, NULL, 'PHOTO_VERIFIED_69ee24dff0966', NULL, 'data:image/jpeg;base64,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');

-- --------------------------------------------------------

--
-- Table structure for table `user_roles`
--

CREATE TABLE `user_roles` (
  `userId` int(11) NOT NULL,
  `roleId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_roles`
--

INSERT INTO `user_roles` (`userId`, `roleId`) VALUES
(1, 28),
(2, 29),
(3, 29),
(4, 29),
(5, 29),
(6, 29),
(7, 29),
(8, 28),
(9, 3);

-- --------------------------------------------------------

--
-- Table structure for table `weight_division`
--

CREATE TABLE `weight_division` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `max_weight_lbs` int(11) NOT NULL,
  `slug` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `weight_division`
--

INSERT INTO `weight_division` (`id`, `name`, `max_weight_lbs`, `slug`) VALUES
(1, 'Heavyweight', 999, 'heavyweight'),
(2, 'Cruiserweight', 200, 'cruiserweight'),
(3, 'Light Heavyweight', 175, 'light-heavyweight'),
(4, 'Super Middleweight', 168, 'super-middleweight'),
(5, 'Middleweight', 160, 'middleweight'),
(6, 'Super Welterweight', 154, 'super-welterweight'),
(7, 'Welterweight', 147, 'welterweight'),
(8, 'Super Lightweight', 140, 'super-lightweight'),
(9, 'Lightweight', 135, 'lightweight'),
(10, 'Super Featherweight', 130, 'super-featherweight'),
(11, 'Featherweight', 126, 'featherweight'),
(12, 'Super Bantamweight', 122, 'super-bantamweight'),
(13, 'Bantamweight', 118, 'bantamweight'),
(14, 'Super Flyweight', 115, 'super-flyweight'),
(15, 'Flyweight', 112, 'flyweight'),
(16, 'Light Flyweight', 108, 'light-flyweight'),
(17, 'Minimumweight', 105, 'minimumweight');

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
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`eventId`);

--
-- Indexes for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_eb_event_user` (`event_id`,`user_id`),
  ADD UNIQUE KEY `uq_eb_reference` (`booking_reference`),
  ADD KEY `IDX_655B447171F7E88B` (`event_id`),
  ADD KEY `IDX_655B4471A76ED395` (`user_id`);

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
-- Indexes for table `fan_vote`
--
ALTER TABLE `fan_vote`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_proposal_vote` (`user_id`,`match_proposal_id`),
  ADD KEY `IDX_AD8E5D4CA76ED395` (`user_id`),
  ADD KEY `IDX_AD8E5D4CC6E05170` (`match_proposal_id`);

--
-- Indexes for table `fighters`
--
ALTER TABLE `fighters`
  ADD PRIMARY KEY (`fighterId`),
  ADD KEY `IDX_AF7F79FF54374256` (`weight_division_id`);

--
-- Indexes for table `fighter_contract`
--
ALTER TABLE `fighter_contract`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_DAF27DA334934341` (`fighter_id`),
  ADD KEY `IDX_DAF27DA371F7E88B` (`event_id`);

--
-- Indexes for table `fight_results`
--
ALTER TABLE `fight_results`
  ADD PRIMARY KEY (`resultId`),
  ADD KEY `IDX_8CC09A7E2B2EBB6C` (`eventId`),
  ADD KEY `IDX_8CC09A7EE35E81C9` (`fighter1Id`),
  ADD KEY `IDX_8CC09A7EE1183F90` (`fighter2Id`),
  ADD KEY `IDX_8CC09A7EF98C03D2` (`winnerId`);

--
-- Indexes for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_4C348AE92F087A36` (`fight_result_id`),
  ADD KEY `IDX_4C348AE934934341` (`fighter_id`);

--
-- Indexes for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_629F4EE471F7E88B` (`event_id`),
  ADD KEY `IDX_629F4EE4D783CFD6` (`fighter1_id`),
  ADD KEY `IDX_629F4EE4C5366038` (`fighter2_id`),
  ADD KEY `IDX_629F4EE454374256` (`weight_division_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notificationId`),
  ADD KEY `IDX_6000B0D364B64DCC` (`userId`);

--
-- Indexes for table `performance_score`
--
ALTER TABLE `performance_score`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_6A3980CE34934341` (`fighter_id`);

--
-- Indexes for table `predictions`
--
ALTER TABLE `predictions`
  ADD PRIMARY KEY (`predictionId`),
  ADD KEY `IDX_8E87BCE664B64DCC` (`userId`),
  ADD KEY `IDX_8E87BCE6B7ED1D03` (`fightId`),
  ADD KEY `IDX_8E87BCE6DA9DBCA0` (`predictedWinnerId`);

--
-- Indexes for table `public_key_credential_source`
--
ALTER TABLE `public_key_credential_source`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `ranking`
--
ALTER TABLE `ranking`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_80B839D054374256` (`weight_division_id`),
  ADD KEY `IDX_80B839D034934341` (`fighter_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`roleId`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`userId`),
  ADD UNIQUE KEY `UNIQ_1483A5E9F85E0677` (`username`);

--
-- Indexes for table `user_roles`
--
ALTER TABLE `user_roles`
  ADD PRIMARY KEY (`userId`,`roleId`),
  ADD KEY `IDX_54FCD59F64B64DCC` (`userId`),
  ADD KEY `IDX_54FCD59FB8C2FD88` (`roleId`);

--
-- Indexes for table `weight_division`
--
ALTER TABLE `weight_division`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_5A372661989D9B62` (`slug`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `blog_article`
--
ALTER TABLE `blog_article`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `blog_category`
--
ALTER TABLE `blog_category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `discipline`
--
ALTER TABLE `discipline`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `eventId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `event_booking`
--
ALTER TABLE `event_booking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `fan_preference`
--
ALTER TABLE `fan_preference`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `fan_profile`
--
ALTER TABLE `fan_profile`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `fan_vote`
--
ALTER TABLE `fan_vote`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `fighters`
--
ALTER TABLE `fighters`
  MODIFY `fighterId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `fighter_contract`
--
ALTER TABLE `fighter_contract`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fight_results`
--
ALTER TABLE `fight_results`
  MODIFY `resultId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `match_proposal`
--
ALTER TABLE `match_proposal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notificationId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `performance_score`
--
ALTER TABLE `performance_score`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `predictions`
--
ALTER TABLE `predictions`
  MODIFY `predictionId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `public_key_credential_source`
--
ALTER TABLE `public_key_credential_source`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ranking`
--
ALTER TABLE `ranking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=641;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `roleId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `userId` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `weight_division`
--
ALTER TABLE `weight_division`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD CONSTRAINT `FK_EECCB3E512469DE2` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`),
  ADD CONSTRAINT `FK_EECCB3E5F675F31B` FOREIGN KEY (`author_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD CONSTRAINT `FK_655B447171F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`eventId`),
  ADD CONSTRAINT `FK_655B4471A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD CONSTRAINT `FK_E207F0522A58A1BC` FOREIGN KEY (`favorite_fighter_id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_E207F05278B04DCA` FOREIGN KEY (`favorite_discipline_id`) REFERENCES `discipline` (`id`),
  ADD CONSTRAINT `FK_E207F05289C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD CONSTRAINT `FK_E95F4CF4A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD CONSTRAINT `FK_8ED024852F087A36` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_results` (`resultId`),
  ADD CONSTRAINT `FK_8ED0248589C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `users` (`userId`);

--
-- Constraints for table `fan_vote`
--
ALTER TABLE `fan_vote`
  ADD CONSTRAINT `FK_AD8E5D4CA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`userId`),
  ADD CONSTRAINT `FK_AD8E5D4CC6E05170` FOREIGN KEY (`match_proposal_id`) REFERENCES `match_proposal` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `fighters`
--
ALTER TABLE `fighters`
  ADD CONSTRAINT `FK_AF7F79FF54374256` FOREIGN KEY (`weight_division_id`) REFERENCES `weight_division` (`id`);

--
-- Constraints for table `fighter_contract`
--
ALTER TABLE `fighter_contract`
  ADD CONSTRAINT `FK_DAF27DA334934341` FOREIGN KEY (`fighter_id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_DAF27DA371F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`eventId`);

--
-- Constraints for table `fight_results`
--
ALTER TABLE `fight_results`
  ADD CONSTRAINT `FK_8CC09A7E2B2EBB6C` FOREIGN KEY (`eventId`) REFERENCES `events` (`eventId`),
  ADD CONSTRAINT `FK_8CC09A7EE1183F90` FOREIGN KEY (`fighter2Id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_8CC09A7EE35E81C9` FOREIGN KEY (`fighter1Id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_8CC09A7EF98C03D2` FOREIGN KEY (`winnerId`) REFERENCES `fighters` (`fighterId`);

--
-- Constraints for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  ADD CONSTRAINT `FK_4C348AE92F087A36` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_results` (`resultId`),
  ADD CONSTRAINT `FK_4C348AE934934341` FOREIGN KEY (`fighter_id`) REFERENCES `fighters` (`fighterId`);

--
-- Constraints for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD CONSTRAINT `FK_629F4EE454374256` FOREIGN KEY (`weight_division_id`) REFERENCES `weight_division` (`id`),
  ADD CONSTRAINT `FK_629F4EE471F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`eventId`),
  ADD CONSTRAINT `FK_629F4EE4C5366038` FOREIGN KEY (`fighter2_id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_629F4EE4D783CFD6` FOREIGN KEY (`fighter1_id`) REFERENCES `fighters` (`fighterId`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FK_6000B0D364B64DCC` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`);

--
-- Constraints for table `performance_score`
--
ALTER TABLE `performance_score`
  ADD CONSTRAINT `FK_6A3980CE34934341` FOREIGN KEY (`fighter_id`) REFERENCES `fighters` (`fighterId`);

--
-- Constraints for table `predictions`
--
ALTER TABLE `predictions`
  ADD CONSTRAINT `FK_8E87BCE664B64DCC` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`),
  ADD CONSTRAINT `FK_8E87BCE6B7ED1D03` FOREIGN KEY (`fightId`) REFERENCES `fight_results` (`resultId`),
  ADD CONSTRAINT `FK_8E87BCE6DA9DBCA0` FOREIGN KEY (`predictedWinnerId`) REFERENCES `fighters` (`fighterId`);

--
-- Constraints for table `ranking`
--
ALTER TABLE `ranking`
  ADD CONSTRAINT `FK_80B839D034934341` FOREIGN KEY (`fighter_id`) REFERENCES `fighters` (`fighterId`),
  ADD CONSTRAINT `FK_80B839D054374256` FOREIGN KEY (`weight_division_id`) REFERENCES `weight_division` (`id`);

--
-- Constraints for table `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `FK_54FCD59F64B64DCC` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`),
  ADD CONSTRAINT `FK_54FCD59FB8C2FD88` FOREIGN KEY (`roleId`) REFERENCES `roles` (`roleId`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
