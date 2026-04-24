-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 07, 2026 at 01:25 PM
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
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `access_level` enum('SUPER','STANDARD') NOT NULL DEFAULT 'STANDARD',
  `department` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id`, `user_id`, `access_level`, `department`) VALUES
(1, 1, 'SUPER', 'System Administration');

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
  `summary` text DEFAULT NULL,
  `status` enum('PUBLISHED','DRAFT','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  `view_count` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `image_path` varchar(255) DEFAULT NULL,
  `video_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `blog_article`
--

INSERT INTO `blog_article` (`id`, `category_id`, `author_id`, `title`, `content`, `summary`, `status`, `view_count`, `created_at`, `updated_at`, `image_path`, `video_path`) VALUES
(1, 1, 1, 'Welcome to SmartFight Fan Module', 'This is the first published article for the SmartFight fan experience module.', 'Kickoff article for the fan module.', 'PUBLISHED', 50, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_1.jpg', 'vid_1.mp4'),
(3, 2, 1, 'Islam In Tunisia?', 'Islam Makhachev will be fighting in Tunisia.', 'Islam Makhachev will be fighting in Tunisia.', 'PUBLISHED', 54, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_3.jpeg', 'vid_3.mp4'),
(4, 1, 1, 'Judo Cup Sousse Recap: The Tank Emerges', 'The Judo Cup Sousse delivered an electrifying night of combat sports. The breakout star was undoubtedly Fares \"The Tank\" Khelifi, who scored a devastating TKO over Mohamed \"Iron Fist\" Trabelsi in the co-main event. The doctor stoppage came in round 4 after a vicious cut opened above Iron Fist\'s left eye.', 'The Tank emerges as a star at Judo Cup Sousse.', 'PUBLISHED', 0, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_4.jpg', 'vid_4.mp4'),
(5, 2, 1, 'Fighter Profile: Ali \"Bones\" Hammami', 'At 28 years old, Ali \"Bones\" Hammami stands as the #1 ranked heavyweight in SmartFight. With a record of 10-2-1, Bones has established himself as the most complete fighter in the organization. Training out of Tunis under coach Karim, Bones combines devastating striking power with underrated ground game.', '#1 ranked heavyweight profile.', 'PUBLISHED', 2, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_5.jpg', 'vid_5.mp4'),
(6, 4, 1, 'FightSphere FC 1 Predictions: Bones vs The Tank', 'The main event of FightSphere FC 1 pits the #1 ranked Bones against the #3 ranked Tank in what promises to be an explosive heavyweight clash. Bones brings superior technique and experience with a 10-2-1 record. Our prediction: Bones by decision in a war of attrition.', 'Expert predictions for FC 1 main event.', 'PUBLISHED', 3, '2026-04-04 10:53:06', '2026-04-06 20:04:38', 'img_6.avif', 'vid_6.mp4'),
(7, 5, 1, 'Exclusive: Coach Karim on Training Bones for FC 1', '\"We\'ve completely overhauled Ali\'s training camp for this fight,\" Coach Karim revealed in an exclusive interview. \"The Tank is the most physically imposing fighter Ali has faced. We\'ve added 20 pounds of focus to his conditioning program.\"', 'Coach Karim exclusive interview ahead of FC 1.', 'PUBLISHED', 1, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_7.jpg', 'vid_7.mp4'),
(8, 3, 1, '5 Boxing Drills to Improve Your Power', 'Whether you\'re a professional fighter or a weekend warrior, these five drills will help you develop knockout power: 1. Heavy Bag Rounds, 2. Medicine Ball Throws, 3. Plyometric Push-Ups, 4. Shadow Boxing with Resistance Bands, 5. Pad Work Combos.', 'Five essential drills for knockout power.', 'PUBLISHED', 2, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_8.jpg', 'vid_8.mp4'),
(9, 1, 1, 'SmartFight Open 2026: Full Preview', 'The SmartFight Open 2026 kicks off April 10 at Salle Omnisports de Tunis with 4,500 seats and a stacked card. Main Event: Bones vs Iron Fist III. Co-Main: The Tank vs The Cobra.', 'Full preview of SmartFight Open 2026.', 'PUBLISHED', 6, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_9.jpeg', 'vid_9.mp4'),
(10, 2, 1, 'Rising Star: Fares \"The Tank\" Khelifi', 'With a 12-1 record and devastating power, Fares \"The Tank\" Khelifi is the most feared heavyweight contender in SmartFight. At just 30 years old, The Tank has already scored 9 KOs/TKOs in his 12 victories.', 'Profile of rising heavyweight The Tank.', 'PUBLISHED', 1, '2026-04-04 10:53:06', '2026-04-06 19:58:09', 'img_10.jpg', 'vid_10.mp4'),
(11, 1, 20, 'test', '', 'test', 'PUBLISHED', 7, '2026-04-04 18:34:35', '2026-04-06 19:58:09', 'img_11.jpg', 'vid_11.mp4'),
(13, 1, 20, 'TEST3 EDIT', '', 'TEST3', 'PUBLISHED', 3, '2026-04-06 08:37:34', '2026-04-06 20:00:35', 'img_13.jpg', 'vid_13.mp4'),
(14, 1, 20, 'AA', 'AAAAAA', 'AA', 'PUBLISHED', 2, '2026-04-06 10:01:03', '2026-04-06 20:00:28', 'img_14.jpg', 'vid_14.mp4'),
(15, 1, 20, 'test after img storage changed', 'test after img storage changed', 'test after img storage changed', 'PUBLISHED', 2, '2026-04-06 20:03:22', '2026-04-06 22:06:28', 'img_15.png', 'vid_15.mp4');

-- --------------------------------------------------------

--
-- Table structure for table `blog_category`
--

CREATE TABLE `blog_category` (
  `id` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `description` text DEFAULT NULL,
  `slug` varchar(150) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
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
-- Table structure for table `coach`
--

CREATE TABLE `coach` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `speciality` varchar(150) DEFAULT NULL,
  `experience_years` int(11) NOT NULL DEFAULT 0,
  `certification` varchar(200) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `coach`
--

INSERT INTO `coach` (`id`, `user_id`, `speciality`, `experience_years`, `certification`, `status`, `created_at`, `updated_at`) VALUES
(1, 5, 'Boxing & Strength Conditioning', 10, 'AIBA Level 3', 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 9, 'MMA Ground Game & Submissions', 7, 'IMMAF Level 2', 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `discipline`
--

CREATE TABLE `discipline` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `weight_class` varchar(60) DEFAULT NULL,
  `round_duration` int(11) NOT NULL DEFAULT 3,
  `max_rounds` int(11) NOT NULL DEFAULT 3,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
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
-- Table structure for table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` enum('SCHEDULED','ONGOING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
  `visibility` enum('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
  `capacity` int(11) NOT NULL DEFAULT 0,
  `venue_id` int(11) DEFAULT NULL,
  `discipline_id` int(11) DEFAULT NULL,
  `organizer_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event`
--

INSERT INTO `event` (`id`, `name`, `description`, `start_date`, `end_date`, `status`, `visibility`, `capacity`, `venue_id`, `discipline_id`, `organizer_id`, `created_at`, `updated_at`) VALUES
(1, 'SmartFight Open 2026', 'Annual open boxing championship', '2026-04-10', '2026-04-12', 'SCHEDULED', 'PUBLIC', 4500, 1, 1, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 'MMA Night Sfax 2026', 'Professional MMA showcase', '2026-05-20', '2026-05-20', 'SCHEDULED', 'PUBLIC', 3000, 2, 2, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 'Judo Cup Sousse', 'Regional judo cup — U18 and senior', '2026-03-15', '2026-03-16', 'COMPLETED', 'PUBLIC', 2000, 3, 3, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 'Kickboxing Gala Monastir', 'Charity gala — exhibition bouts', '2026-06-01', '2026-06-01', 'SCHEDULED', 'PRIVATE', 1600, 4, 4, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 'Cairo Champions League', 'International tournament — 5 disciplines', '2026-07-10', '2026-07-15', 'SCHEDULED', 'PUBLIC', 7000, 5, 1, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(6, 'FightSphere FC 1: Origins', 'Inaugural FightSphere MMA card — 8 bouts', '2026-04-25', '2026-04-25', 'SCHEDULED', 'PUBLIC', 3500, 1, 2, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(7, 'Boxing Thunder Sousse', 'Pro boxing doubleheader under the stars', '2026-05-05', '2026-05-05', 'SCHEDULED', 'PUBLIC', 2500, 3, 1, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(8, 'Kickboxing Clash Tunis', 'K-1 rules, 6 bouts main card', '2026-05-15', '2026-05-15', 'SCHEDULED', 'PUBLIC', 4000, 1, 4, 2, '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `event_booking`
--

CREATE TABLE `event_booking` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `booking_status` enum('CONFIRMED','PENDING','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `ticket_quantity` int(11) NOT NULL DEFAULT 1,
  `total_price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `ticket_type` enum('VIP','REGULAR','STANDING') NOT NULL DEFAULT 'REGULAR',
  `booking_date` date NOT NULL,
  `booking_reference` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event_booking`
--

INSERT INTO `event_booking` (`id`, `event_id`, `user_id`, `booking_status`, `ticket_quantity`, `total_price`, `ticket_type`, `booking_date`, `booking_reference`, `created_at`, `updated_at`) VALUES
(1, 1, 6, 'CANCELLED', 1, 50.00, 'VIP', '2026-03-30', 'SF-9D046AB1', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 2, 6, 'CANCELLED', 2, 50.00, 'REGULAR', '2026-03-30', 'SF-D46475DA', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 2, 6, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-03-31', 'SF-D3783EA5', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 1, 6, 'CONFIRMED', 1, 50.00, 'VIP', '2026-03-31', 'SF-E6E13DC0', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 6, 6, 'CONFIRMED', 2, 100.00, 'VIP', '2026-04-01', 'SF-A1B2C3D4', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(6, 6, 10, 'CONFIRMED', 1, 25.00, 'REGULAR', '2026-04-02', 'SF-E5F6G7H8', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(7, 6, 11, 'CONFIRMED', 3, 75.00, 'REGULAR', '2026-04-03', 'SF-I9J0K1L2', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(8, 6, 12, 'CONFIRMED', 1, 50.00, 'VIP', '2026-04-03', 'SF-M3N4O5P6', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(9, 7, 6, 'CONFIRMED', 2, 50.00, 'REGULAR', '2026-04-05', 'SF-Q7R8S9T0', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(10, 7, 13, 'CONFIRMED', 4, 40.00, 'STANDING', '2026-04-06', 'SF-U1V2W3X4', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(11, 7, 14, 'CONFIRMED', 1, 50.00, 'VIP', '2026-04-07', 'SF-Y5Z6A7B8', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(12, 8, 6, 'CONFIRMED', 2, 100.00, 'VIP', '2026-04-10', 'SF-C9D0E1F2', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(13, 8, 15, 'CONFIRMED', 2, 50.00, 'REGULAR', '2026-04-11', 'SF-G3H4I5J6', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(14, 8, 11, 'PENDING', 1, 25.00, 'REGULAR', '2026-04-12', 'SF-K7L8M9N0', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(15, 2, 11, 'CONFIRMED', 2, 100.00, 'VIP', '2026-04-01', 'SF-O1P2Q3R4', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(16, 2, 12, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-02', 'SF-S5T6U7V8', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(17, 5, 13, 'CONFIRMED', 2, 50.00, 'REGULAR', '2026-04-08', 'SF-W9X0Y1Z2', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(18, 5, 14, 'CONFIRMED', 1, 10.00, 'STANDING', '2026-04-09', 'SF-A3B4C5D6', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(19, 1, 15, 'CONFIRMED', 1, 50.00, 'VIP', '2026-04-01', 'SF-E7F8G9H0', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(20, 1, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-04', 'SF-E7714EDD', '2026-04-04 18:36:39', '2026-04-04 18:36:47'),
(21, 1, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-04', 'SF-17BD85A2', '2026-04-04 18:37:28', '2026-04-05 22:01:56'),
(22, 6, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-05', 'SF-060D110D', '2026-04-05 22:01:47', '2026-04-06 10:01:44'),
(23, 1, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-06', 'SF-76AB4DBD', '2026-04-06 08:44:55', '2026-04-06 08:48:12'),
(24, 6, 21, 'CANCELLED', 1, 25.00, 'REGULAR', '2026-04-06', 'SF-306CA102', '2026-04-06 10:03:20', '2026-04-06 22:07:44');

-- --------------------------------------------------------

--
-- Table structure for table `event_fighter`
--

CREATE TABLE `event_fighter` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `registration_date` date NOT NULL DEFAULT curdate(),
  `weight_at_event` decimal(5,2) DEFAULT NULL,
  `corner` enum('RED','BLUE') NOT NULL DEFAULT 'RED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event_fighter`
--

INSERT INTO `event_fighter` (`id`, `event_id`, `fighter_id`, `registration_date`, `weight_at_event`, `corner`) VALUES
(1, 1, 1, '2026-03-20', 91.50, 'RED'),
(2, 1, 2, '2026-03-21', 90.80, 'BLUE'),
(3, 2, 3, '2026-04-15', 70.10, 'RED'),
(4, 2, 4, '2026-04-16', 69.75, 'BLUE'),
(5, 3, 1, '2026-02-28', 90.00, 'RED'),
(6, 6, 3, '2026-04-01', 70.20, 'RED'),
(7, 6, 6, '2026-04-01', 62.50, 'BLUE'),
(8, 6, 1, '2026-04-02', 92.00, 'RED'),
(9, 6, 5, '2026-04-02', 93.50, 'BLUE'),
(10, 7, 1, '2026-04-10', 91.80, 'RED'),
(11, 7, 2, '2026-04-10', 90.50, 'BLUE'),
(12, 7, 5, '2026-04-11', 93.00, 'RED'),
(13, 8, 7, '2026-04-20', 74.50, 'RED'),
(14, 8, 8, '2026-04-20', 68.00, 'BLUE'),
(15, 8, 3, '2026-04-21', 63.00, 'RED'),
(16, 8, 4, '2026-04-21', 69.50, 'BLUE');

-- --------------------------------------------------------

--
-- Table structure for table `event_schedule`
--

CREATE TABLE `event_schedule` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `scheduled_time` datetime NOT NULL,
  `duration_min` int(11) NOT NULL DEFAULT 60,
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `event_schedule`
--

INSERT INTO `event_schedule` (`id`, `event_id`, `title`, `scheduled_time`, `duration_min`, `notes`) VALUES
(1, 1, 'Fighter Weigh-In', '2026-04-09 16:00:00', 120, 'Official weigh-in'),
(2, 1, 'Opening Ceremony', '2026-04-10 09:00:00', 60, 'Welcome address'),
(3, 1, 'Preliminary Bouts', '2026-04-10 10:00:00', 240, 'All preliminary rounds'),
(4, 1, 'Semi-Finals', '2026-04-11 14:00:00', 180, 'Top 4 fighters'),
(5, 1, 'Finals & Award Ceremony', '2026-04-12 18:00:00', 150, 'Championship finals'),
(6, 2, 'Doors Open', '2026-05-20 18:00:00', 30, 'Ticket check'),
(7, 2, 'Undercard Bouts', '2026-05-20 19:00:00', 120, '4 preliminary MMA fights'),
(8, 2, 'Main Card', '2026-05-20 21:00:00', 150, '3 main event bouts'),
(9, 3, 'U18 Rounds', '2026-03-15 09:00:00', 300, 'Under-18 category'),
(10, 3, 'Senior Finals', '2026-03-16 15:00:00', 180, 'Senior division finals'),
(11, 6, 'Doors Open', '2026-04-25 17:00:00', 60, 'Fan zone open'),
(12, 6, 'Prelims', '2026-04-25 18:00:00', 120, '4 preliminary bouts'),
(13, 6, 'Main Card', '2026-04-25 20:30:00', 150, '4 main card fights'),
(14, 7, 'Weigh-In', '2026-05-04 16:00:00', 90, 'Official weigh-in'),
(15, 7, 'Fight Night', '2026-05-05 19:00:00', 180, 'Boxing doubleheader'),
(16, 8, 'Fan Meet & Greet', '2026-05-15 16:00:00', 60, 'Meet the fighters'),
(17, 8, 'Kickboxing Main Card', '2026-05-15 19:30:00', 180, '6 K-1 bouts');

-- --------------------------------------------------------

--
-- Table structure for table `fan_notification`
--

CREATE TABLE `fan_notification` (
  `id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `type` enum('NEW_EVENT','PREDICTION_SCORED','LEADERBOARD_CHANGE','ADMIN_BROADCAST') NOT NULL,
  `title` varchar(200) NOT NULL,
  `message` text NOT NULL,
  `related_event_id` int(11) DEFAULT NULL,
  `related_fight_id` int(11) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
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
(39, 21, 'ADMIN_BROADCAST', 'test brodcast', 'test brodcast', NULL, NULL, 1, '2026-04-06 22:07:15');

-- --------------------------------------------------------

--
-- Table structure for table `fan_prediction`
--

CREATE TABLE `fan_prediction` (
  `id` int(11) NOT NULL,
  `match_proposal_id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `predicted_winner_id` int(11) NOT NULL,
  `predicted_method` enum('KO','TKO','SUBMISSION','DECISION','DRAW') NOT NULL,
  `submitted_at` datetime NOT NULL DEFAULT current_timestamp(),
  `is_locked` tinyint(1) NOT NULL DEFAULT 1,
  `points_earned` int(11) DEFAULT NULL,
  `is_scored` tinyint(1) NOT NULL DEFAULT 0,
  `season` varchar(10) NOT NULL DEFAULT '2026'
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
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
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
  `bio` text DEFAULT NULL
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
(7, 15, 'MMA', 'Tunisia', 'UFC and SmartFight hardcore fan');

-- --------------------------------------------------------

--
-- Table structure for table `fan_reaction`
--

CREATE TABLE `fan_reaction` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fan_id` int(11) NOT NULL,
  `reaction_type` enum('FIRE','SHOCK','RESPECT','DOMINANT','CONTROVERSIAL') NOT NULL,
  `comment` varchar(140) DEFAULT NULL,
  `reacted_at` datetime NOT NULL DEFAULT current_timestamp(),
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Community Reaction Wall — one reaction per fan per fight';

--
-- Dumping data for table `fan_reaction`
--

INSERT INTO `fan_reaction` (`id`, `fight_result_id`, `fan_id`, `reaction_type`, `comment`, `reacted_at`, `is_pinned`, `is_deleted`) VALUES
(1, 1, 6, 'RESPECT', 'The Lion deserved every point of that decision', '2026-04-04 10:53:06', 0, 0),
(2, 1, 10, 'CONTROVERSIAL', 'Should have been stopped earlier in round 2', '2026-04-04 10:53:06', 0, 0),
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
(23, 4, 21, 'FIRE', 'TEST', '2026-04-04 18:37:56', 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `fighter`
--

CREATE TABLE `fighter` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `nickname` varchar(100) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `weight_class_id` int(11) DEFAULT NULL,
  `wins` int(11) NOT NULL DEFAULT 0,
  `losses` int(11) NOT NULL DEFAULT 0,
  `draws` int(11) NOT NULL DEFAULT 0,
  `status` enum('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fighter`
--

INSERT INTO `fighter` (`id`, `user_id`, `nickname`, `date_of_birth`, `nationality`, `photo_url`, `weight_class_id`, `wins`, `losses`, `draws`, `status`, `created_at`, `updated_at`) VALUES
(1, 3, 'Bones', '1998-05-14', 'Tunisian', NULL, 6, 10, 2, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 4, 'Iron Fist', '1997-11-22', 'Tunisian', NULL, 6, 8, 3, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 7, 'The Cobra', '2000-03-08', 'Tunisian', NULL, 3, 5, 5, 2, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 8, 'Tigress', '1999-07-30', 'Tunisian', NULL, 4, 7, 1, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 16, 'The Tank', '1996-01-20', 'Tunisian', NULL, 6, 12, 1, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(6, 17, 'Lightning', '1999-09-10', 'Tunisian', NULL, 3, 9, 3, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(7, 18, 'The Ghost', '1998-06-25', 'Tunisian', NULL, 5, 6, 4, 0, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(8, 19, 'Hammer', '1997-12-05', 'Tunisian', NULL, 4, 11, 2, 1, 'ACTIVE', '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `fighter_coach`
--

CREATE TABLE `fighter_coach` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `coach_id` int(11) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fighter_coach`
--

INSERT INTO `fighter_coach` (`id`, `fighter_id`, `coach_id`, `start_date`, `end_date`, `is_active`) VALUES
(1, 1, 1, '2024-01-10', NULL, 1),
(2, 2, 1, '2024-02-15', NULL, 1),
(3, 3, 2, '2023-09-01', NULL, 1),
(4, 4, 2, '2023-11-20', NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `fight_highlight`
--

CREATE TABLE `fight_highlight` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `video_local_path` varchar(512) DEFAULT NULL,
  `video_url` varchar(512) DEFAULT NULL,
  `normalized_source` varchar(512) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fight_highlight`
--

INSERT INTO `fight_highlight` (`id`, `fight_result_id`, `video_local_path`, `video_url`, `normalized_source`, `updated_at`) VALUES
(1, 1, NULL, 'https://www.youtube.com/watch?v=TIamABjO8sw', 'https://www.youtube.com/watch?v=TIamABjO8sw', '2026-04-04 10:53:06'),
(2, 2, NULL, 'https://www.youtube.com/watch?v=TIamABjO8sw', 'https://www.youtube.com/watch?v=TIamABjO8sw', '2026-04-04 10:53:06'),
(3, 3, NULL, 'https://www.youtube.com/watch?v=TIamABjO8sw', 'https://www.youtube.com/watch?v=TIamABjO8sw', '2026-04-04 10:53:06'),
(4, 4, NULL, 'https://www.youtube.com/watch?v=TIamABjO8sw', 'https://www.youtube.com/watch?v=TIamABjO8sw', '2026-04-04 10:53:06');

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
  `method` enum('KO','TKO','SUBMISSION','DECISION','DRAW','NO_CONTEST') NOT NULL,
  `round_ended` int(11) DEFAULT NULL,
  `time_ended` time DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `fight_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fight_result`
--

INSERT INTO `fight_result` (`id`, `event_id`, `match_id`, `fighter_red_id`, `fighter_blue_id`, `winner_id`, `method`, `round_ended`, `time_ended`, `notes`, `fight_date`, `created_at`) VALUES
(1, 3, NULL, 1, 2, 1, 'DECISION', 3, '03:00:00', NULL, '2026-03-16', '2026-04-04 10:53:06'),
(2, 3, NULL, 3, 4, 3, 'KO', 2, '01:45:00', NULL, '2026-03-16', '2026-04-04 10:53:06'),
(3, 3, NULL, 5, 2, 5, 'TKO', 4, '02:30:00', 'Doctor stoppage — cut above eye', '2026-03-16', '2026-04-04 10:53:06'),
(4, 1, 1, 1, 2, 1, 'KO', 7, '01:22:00', 'Devastating right hook KO in round 7', '2026-04-12', '2026-04-04 10:53:06'),
(5, 1, NULL, 5, 3, 5, 'DECISION', 12, '03:00:00', 'Unanimous decision 117-111, 116-112, 118-110', '2026-04-12', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `fight_statistic`
--

CREATE TABLE `fight_statistic` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `strikes_landed` int(11) NOT NULL DEFAULT 0,
  `strikes_thrown` int(11) NOT NULL DEFAULT 0,
  `takedowns` int(11) NOT NULL DEFAULT 0,
  `submissions` int(11) NOT NULL DEFAULT 0,
  `knockdowns` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fight_statistic`
--

INSERT INTO `fight_statistic` (`id`, `fight_result_id`, `fighter_id`, `strikes_landed`, `strikes_thrown`, `takedowns`, `submissions`, `knockdowns`) VALUES
(1, 1, 1, 145, 210, 3, 0, 1),
(2, 1, 2, 112, 185, 1, 0, 0),
(3, 2, 3, 87, 130, 5, 1, 2),
(4, 2, 4, 54, 110, 0, 0, 0),
(5, 3, 5, 167, 230, 0, 0, 2),
(6, 3, 2, 98, 175, 1, 0, 0),
(7, 4, 1, 189, 280, 2, 0, 3),
(8, 4, 2, 134, 240, 0, 0, 1),
(9, 5, 5, 201, 310, 1, 0, 0),
(10, 5, 3, 156, 290, 4, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `judge_score`
--

CREATE TABLE `judge_score` (
  `id` int(11) NOT NULL,
  `fight_result_id` int(11) NOT NULL,
  `judge_name` varchar(100) NOT NULL,
  `score_red` int(11) NOT NULL DEFAULT 0,
  `score_blue` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `judge_score`
--

INSERT INTO `judge_score` (`id`, `fight_result_id`, `judge_name`, `score_red`, `score_blue`) VALUES
(1, 1, 'Judge Hassan', 29, 28),
(2, 1, 'Judge Salma', 29, 27),
(3, 1, 'Judge Tarek', 28, 28),
(4, 5, 'Judge Hassan', 117, 111),
(5, 5, 'Judge Salma', 116, 112),
(6, 5, 'Judge Tarek', 118, 110);

-- --------------------------------------------------------

--
-- Table structure for table `matchmaking_rule`
--

CREATE TABLE `matchmaking_rule` (
  `id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `weight` decimal(4,2) NOT NULL DEFAULT 1.00,
  `is_active` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `matchmaking_rule`
--

INSERT INTO `matchmaking_rule` (`id`, `name`, `description`, `weight`, `is_active`) VALUES
(1, 'Weight Class Match', 'Fighters must be in same weight class', 1.00, 1),
(2, 'Win Rate Balance', 'Win rate difference must be less than 30%', 0.80, 1),
(3, 'Experience Level', 'Total fights difference must be less than 10', 0.70, 1),
(4, 'Recent Performance', 'Based on last 5 fights performance score', 0.90, 1);

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
  `status` enum('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
  `proposed_at` datetime NOT NULL DEFAULT current_timestamp(),
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `match_proposal`
--

INSERT INTO `match_proposal` (`id`, `event_id`, `fighter1_id`, `fighter2_id`, `compatibility`, `status`, `proposed_at`, `notes`) VALUES
(1, 1, 1, 2, 87.50, 'ACCEPTED', '2026-02-23 23:27:13', 'Same weight class, balanced record'),
(2, 2, 3, 4, 79.30, 'ACCEPTED', '2026-02-23 23:27:13', 'Good weight and experience match'),
(3, 1, 1, 3, 45.00, 'REJECTED', '2026-02-23 23:27:13', 'Weight class mismatch'),
(4, 6, 3, 6, 82.00, 'ACCEPTED', '2026-03-28 10:00:00', 'Lightweight clash — The Cobra vs Lightning'),
(5, 6, 1, 5, 88.50, 'ACCEPTED', '2026-03-28 10:30:00', 'Heavyweight showdown — Bones vs The Tank'),
(6, 7, 1, 2, 87.50, 'ACCEPTED', '2026-03-29 09:00:00', 'Rematch — Bones vs Iron Fist'),
(7, 8, 7, 8, 80.00, 'ACCEPTED', '2026-03-30 11:00:00', 'Kickboxing — The Ghost vs Hammer'),
(8, 8, 3, 4, 79.30, 'ACCEPTED', '2026-03-30 11:30:00', 'Kickboxing — The Cobra vs Tigress'),
(9, 8, 7, 8, 80.00, 'ACCEPTED', '2026-03-30 11:00:00', 'K-1: The Ghost vs Hammer'),
(10, 8, 3, 4, 79.30, 'ACCEPTED', '2026-03-30 11:30:00', 'K-1: The Cobra vs Tigress');

-- --------------------------------------------------------

--
-- Table structure for table `performance_score`
--

CREATE TABLE `performance_score` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `score` decimal(6,2) NOT NULL DEFAULT 0.00,
  `aggression` decimal(4,2) DEFAULT NULL,
  `defense` decimal(4,2) DEFAULT NULL,
  `technique` decimal(4,2) DEFAULT NULL,
  `experience` decimal(4,2) DEFAULT NULL,
  `calculated_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `performance_score`
--

INSERT INTO `performance_score` (`id`, `fighter_id`, `score`, `aggression`, `defense`, `technique`, `experience`, `calculated_at`) VALUES
(1, 1, 88.50, 9.00, 8.50, 8.80, 9.00, '2026-04-04 10:53:06'),
(2, 2, 82.00, 8.50, 8.00, 8.20, 8.00, '2026-04-04 10:53:06'),
(3, 3, 76.50, 7.50, 7.80, 7.50, 7.80, '2026-04-04 10:53:06'),
(4, 4, 80.00, 8.00, 8.20, 8.00, 7.50, '2026-04-04 10:53:06'),
(5, 5, 91.00, 9.50, 8.80, 9.00, 9.20, '2026-04-04 10:53:06'),
(6, 6, 78.00, 8.00, 7.50, 8.00, 7.50, '2026-04-04 10:53:06'),
(7, 7, 74.00, 7.00, 8.00, 7.50, 7.00, '2026-04-04 10:53:06'),
(8, 8, 85.00, 8.80, 8.50, 8.50, 8.00, '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `ranking`
--

CREATE TABLE `ranking` (
  `id` int(11) NOT NULL,
  `fighter_id` int(11) NOT NULL,
  `discipline_id` int(11) DEFAULT NULL,
  `rank_position` int(11) NOT NULL DEFAULT 0,
  `points` decimal(8,2) NOT NULL DEFAULT 0.00,
  `season` varchar(20) NOT NULL DEFAULT '2026',
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `ranking`
--

INSERT INTO `ranking` (`id`, `fighter_id`, `discipline_id`, `rank_position`, `points`, `season`, `updated_at`) VALUES
(1, 1, 1, 1, 950.00, '2026', '2026-04-04 10:53:06'),
(2, 2, 1, 2, 870.00, '2026', '2026-04-04 10:53:06'),
(3, 3, 2, 1, 820.00, '2026', '2026-04-04 10:53:06'),
(4, 4, 2, 2, 760.00, '2026', '2026-04-04 10:53:06'),
(5, 5, 1, 3, 900.00, '2026', '2026-04-04 10:53:06'),
(6, 6, 2, 3, 780.00, '2026', '2026-04-04 10:53:06'),
(7, 7, 4, 1, 740.00, '2026', '2026-04-04 10:53:06'),
(8, 8, 4, 2, 710.00, '2026', '2026-04-04 10:53:06');

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
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `first_name`, `last_name`, `email`, `password`, `phone`, `role_id`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'Nassir', 'Admin', 'nassir@smartfight.tn', 'hashed_pwd_1', '+216 20 111 001', 1, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 'Ayoub', 'Org', 'ayoub@smartfight.tn', 'hashed_pwd_2', '+216 20 111 002', 2, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 'Ali', 'Hammami', 'ali@smartfight.tn', 'hashed_pwd_3', '+216 20 111 003', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 'Mohamed', 'Trabelsi', 'mohamed@smartfight.tn', 'hashed_pwd_4', '+216 20 111 004', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 'Karim', 'Coach', 'karim@smartfight.tn', 'hashed_pwd_5', '+216 20 111 005', 4, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(6, 'Sana', 'Fan', 'sana@smartfight.tn', 'hashed_pwd_6', '+216 20 111 006', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(7, 'Omar', 'Belhaj', 'omar@smartfight.tn', 'hashed_pwd_7', '+216 20 111 007', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(8, 'Hana', 'Mansour', 'hana@smartfight.tn', 'hashed_pwd_8', '+216 20 111 008', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(9, 'Youssef', 'Coach2', 'youssef@smartfight.tn', 'hashed_pwd_9', '+216 20 111 009', 4, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(10, 'Leila', 'Fan2', 'leila@smartfight.tn', 'hashed_pwd_10', '+216 20 111 010', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(11, 'Yassine', 'Bouazizi', 'yassine@smartfight.tn', 'hashed_pwd_11', '+216 20 111 011', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(12, 'Nour', 'Chaouch', 'nour@smartfight.tn', 'hashed_pwd_12', '+216 20 111 012', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(13, 'Rami', 'Jebali', 'rami@smartfight.tn', 'hashed_pwd_13', '+216 20 111 013', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(14, 'Amira', 'Selmi', 'amira@smartfight.tn', 'hashed_pwd_14', '+216 20 111 014', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(15, 'Karim', 'Gharbi', 'karim.fan@smartfight.tn', 'hashed_pwd_15', '+216 20 111 015', 5, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(16, 'Fares', 'Khelifi', 'fares@smartfight.tn', 'hashed_pwd_16', '+216 20 111 016', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(17, 'Bilel', 'Sassi', 'bilel@smartfight.tn', 'hashed_pwd_17', '+216 20 111 017', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(18, 'Anis', 'Mrad', 'anis@smartfight.tn', 'hashed_pwd_18', '+216 20 111 018', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(19, 'Wael', 'Haddad', 'wael@smartfight.tn', 'hashed_pwd_19', '+216 20 111 019', 3, 1, '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(20, 'ahmed', 'ahmed', 'ahmed@ahmed.com', 'azerty1+Q', '12345678', 1, 1, '2026-04-04 18:33:21', '2026-04-04 18:33:21'),
(21, 'test', 'test', 'test@fan.com', 'azerty1+Q', '112233445566', 5, 1, '2026-04-04 18:35:26', '2026-04-04 18:35:26');

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
-- Table structure for table `venue`
--

CREATE TABLE `venue` (
  `id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL DEFAULT 'Tunisia',
  `capacity` int(11) NOT NULL DEFAULT 0,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `venue`
--

INSERT INTO `venue` (`id`, `name`, `address`, `city`, `country`, `capacity`, `contact_email`, `contact_phone`, `created_at`, `updated_at`) VALUES
(1, 'Salle Omnisports de Tunis', 'Avenue Habib Bourguiba', 'Tunis', 'Tunisia', 5000, 'contact@sot.tn', '+216 71 100 001', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(2, 'Palais des Sports de Sfax', 'Rue du Sport', 'Sfax', 'Tunisia', 3200, 'info@palaissfax.tn', '+216 74 200 002', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(3, 'Mohamed Ali Sports Hall', 'Place Mohamed Ali', 'Sousse', 'Tunisia', 2500, 'admin@mas.tn', '+216 73 300 003', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(4, 'Salle Polyvalente Monastir', 'Avenue 7 Novembre', 'Monastir', 'Tunisia', 1800, 'contact@spm.tn', '+216 73 400 004', '2026-04-04 10:53:06', '2026-04-04 10:53:06'),
(5, 'Cairo International Arena', '6th of October City', 'Cairo', 'Egypt', 8000, 'info@cairoarena.eg', '+20 2 5500 050', '2026-04-04 10:53:06', '2026-04-04 10:53:06');

-- --------------------------------------------------------

--
-- Table structure for table `weight_class`
--

CREATE TABLE `weight_class` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `min_weight` decimal(5,2) NOT NULL,
  `max_weight` decimal(5,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `weight_class`
--

INSERT INTO `weight_class` (`id`, `name`, `min_weight`, `max_weight`) VALUES
(1, 'Flyweight', 48.00, 51.00),
(2, 'Featherweight', 54.00, 57.00),
(3, 'Lightweight', 57.00, 63.50),
(4, 'Welterweight', 63.50, 69.00),
(5, 'Middleweight', 69.00, 75.00),
(6, 'Heavyweight', 91.00, 120.00);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_admin_user` (`user_id`);

--
-- Indexes for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_blog_article_category` (`category_id`),
  ADD KEY `fk_blog_article_author` (`author_id`);

--
-- Indexes for table `blog_category`
--
ALTER TABLE `blog_category`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_blog_category_name` (`name`),
  ADD UNIQUE KEY `uq_blog_category_slug` (`slug`);

--
-- Indexes for table `coach`
--
ALTER TABLE `coach`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_coach_user` (`user_id`);

--
-- Indexes for table `discipline`
--
ALTER TABLE `discipline`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_discipline_name` (`name`);

--
-- Indexes for table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_event_venue` (`venue_id`),
  ADD KEY `fk_event_discipline` (`discipline_id`),
  ADD KEY `fk_event_organizer` (`organizer_id`);

--
-- Indexes for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_event_booking_reference` (`booking_reference`),
  ADD KEY `fk_event_booking_event` (`event_id`),
  ADD KEY `fk_event_booking_user` (`user_id`);

--
-- Indexes for table `event_fighter`
--
ALTER TABLE `event_fighter`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_event_fighter` (`event_id`,`fighter_id`),
  ADD KEY `fk_ef_fighter` (`fighter_id`);

--
-- Indexes for table `event_schedule`
--
ALTER TABLE `event_schedule`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_schedule_event` (`event_id`);

--
-- Indexes for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_fn_fan_read` (`fan_id`,`is_read`),
  ADD KEY `idx_fn_type` (`type`),
  ADD KEY `idx_fn_event` (`related_event_id`);

--
-- Indexes for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_prediction` (`match_proposal_id`,`fan_id`),
  ADD KEY `idx_fp_fan` (`fan_id`),
  ADD KEY `idx_fp_season` (`season`),
  ADD KEY `idx_fp_scored` (`is_scored`),
  ADD KEY `fk_fp_winner` (`predicted_winner_id`);

--
-- Indexes for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_preference` (`fan_id`),
  ADD KEY `idx_fpref_discipline` (`favorite_discipline_id`),
  ADD KEY `idx_fpref_fighter` (`favorite_fighter_id`);

--
-- Indexes for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_user` (`user_id`);

--
-- Indexes for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_reaction` (`fight_result_id`,`fan_id`),
  ADD KEY `idx_fr_fight` (`fight_result_id`),
  ADD KEY `idx_fr_fan` (`fan_id`),
  ADD KEY `idx_fr_pinned` (`is_pinned`),
  ADD KEY `idx_fr_deleted` (`is_deleted`);

--
-- Indexes for table `fighter`
--
ALTER TABLE `fighter`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fighter_user` (`user_id`),
  ADD KEY `fk_fighter_weight` (`weight_class_id`);

--
-- Indexes for table `fighter_coach`
--
ALTER TABLE `fighter_coach`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fighter_coach` (`fighter_id`,`coach_id`),
  ADD KEY `fk_fc_coach` (`coach_id`);

--
-- Indexes for table `fight_highlight`
--
ALTER TABLE `fight_highlight`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_highlight_result` (`fight_result_id`);

--
-- Indexes for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_fr_event` (`event_id`),
  ADD KEY `fk_fr_match` (`match_id`),
  ADD KEY `fk_fr_red` (`fighter_red_id`),
  ADD KEY `fk_fr_blue` (`fighter_blue_id`),
  ADD KEY `fk_fr_winner` (`winner_id`);

--
-- Indexes for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_stat` (`fight_result_id`,`fighter_id`),
  ADD KEY `fk_fs_fighter` (`fighter_id`);

--
-- Indexes for table `judge_score`
--
ALTER TABLE `judge_score`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_js_result` (`fight_result_id`);

--
-- Indexes for table `matchmaking_rule`
--
ALTER TABLE `matchmaking_rule`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mp_event` (`event_id`),
  ADD KEY `fk_mp_fighter1` (`fighter1_id`),
  ADD KEY `fk_mp_fighter2` (`fighter2_id`);

--
-- Indexes for table `performance_score`
--
ALTER TABLE `performance_score`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ps_fighter` (`fighter_id`);

--
-- Indexes for table `ranking`
--
ALTER TABLE `ranking`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_ranking` (`fighter_id`,`discipline_id`,`season`),
  ADD KEY `fk_rank_discipline` (`discipline_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_email` (`email`),
  ADD KEY `fk_user_role` (`role_id`);

--
-- Indexes for table `user_role`
--
ALTER TABLE `user_role`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_role_name` (`name`);

--
-- Indexes for table `venue`
--
ALTER TABLE `venue`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `weight_class`
--
ALTER TABLE `weight_class`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_weight_class_name` (`name`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `blog_article`
--
ALTER TABLE `blog_article`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `blog_category`
--
ALTER TABLE `blog_category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `coach`
--
ALTER TABLE `coach`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `discipline`
--
ALTER TABLE `discipline`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `event`
--
ALTER TABLE `event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `event_booking`
--
ALTER TABLE `event_booking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `event_fighter`
--
ALTER TABLE `event_fighter`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `event_schedule`
--
ALTER TABLE `event_schedule`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `fan_notification`
--
ALTER TABLE `fan_notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `fighter`
--
ALTER TABLE `fighter`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `fighter_coach`
--
ALTER TABLE `fighter_coach`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fight_highlight`
--
ALTER TABLE `fight_highlight`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `fight_result`
--
ALTER TABLE `fight_result`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `judge_score`
--
ALTER TABLE `judge_score`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `matchmaking_rule`
--
ALTER TABLE `matchmaking_rule`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `match_proposal`
--
ALTER TABLE `match_proposal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `performance_score`
--
ALTER TABLE `performance_score`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `ranking`
--
ALTER TABLE `ranking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `user_role`
--
ALTER TABLE `user_role`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `venue`
--
ALTER TABLE `venue`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `weight_class`
--
ALTER TABLE `weight_class`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD CONSTRAINT `fk_blog_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_blog_article_category` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`) ON UPDATE CASCADE;

--
-- Constraints for table `coach`
--
ALTER TABLE `coach`
  ADD CONSTRAINT `fk_coach_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `event`
--
ALTER TABLE `event`
  ADD CONSTRAINT `fk_event_discipline` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `event_booking`
--
ALTER TABLE `event_booking`
  ADD CONSTRAINT `fk_event_booking_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_booking_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `event_fighter`
--
ALTER TABLE `event_fighter`
  ADD CONSTRAINT `fk_ef_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_ef_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `event_schedule`
--
ALTER TABLE `event_schedule`
  ADD CONSTRAINT `fk_schedule_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD CONSTRAINT `fk_fn_event` FOREIGN KEY (`related_event_id`) REFERENCES `event` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fn_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD CONSTRAINT `fk_fp_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fp_match` FOREIGN KEY (`match_proposal_id`) REFERENCES `match_proposal` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fp_winner` FOREIGN KEY (`predicted_winner_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD CONSTRAINT `fk_fpref_discipline` FOREIGN KEY (`favorite_discipline_id`) REFERENCES `discipline` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fpref_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fpref_fighter` FOREIGN KEY (`favorite_fighter_id`) REFERENCES `fighter` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD CONSTRAINT `fk_fan_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD CONSTRAINT `fk_frx_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_frx_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fighter`
--
ALTER TABLE `fighter`
  ADD CONSTRAINT `fk_fighter_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fighter_weight` FOREIGN KEY (`weight_class_id`) REFERENCES `weight_class` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fighter_coach`
--
ALTER TABLE `fighter_coach`
  ADD CONSTRAINT `fk_fc_coach` FOREIGN KEY (`coach_id`) REFERENCES `coach` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fc_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fight_highlight`
--
ALTER TABLE `fight_highlight`
  ADD CONSTRAINT `fk_fh_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD CONSTRAINT `fk_fr_blue` FOREIGN KEY (`fighter_blue_id`) REFERENCES `fighter` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_match` FOREIGN KEY (`match_id`) REFERENCES `match_proposal` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_red` FOREIGN KEY (`fighter_red_id`) REFERENCES `fighter` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_winner` FOREIGN KEY (`winner_id`) REFERENCES `fighter` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fight_statistic`
--
ALTER TABLE `fight_statistic`
  ADD CONSTRAINT `fk_fs_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fs_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `judge_score`
--
ALTER TABLE `judge_score`
  ADD CONSTRAINT `fk_js_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD CONSTRAINT `fk_mp_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_mp_fighter1` FOREIGN KEY (`fighter1_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_mp_fighter2` FOREIGN KEY (`fighter2_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `performance_score`
--
ALTER TABLE `performance_score`
  ADD CONSTRAINT `fk_ps_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `ranking`
--
ALTER TABLE `ranking`
  ADD CONSTRAINT `fk_rank_discipline` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rank_fighter` FOREIGN KEY (`fighter_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `user_role` (`id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
