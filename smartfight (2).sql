-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 12, 2026 at 11:58 AM
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
(1, 1, 1, 'Welcome to SmartFight Fan Module', 'This is the first published article for the SmartFight fan experience module.', 'Kickoff article for the fan module.', 'PUBLISHED', 52, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_1.jpg', 'vid_1.mp4'),
(3, 2, 1, 'Islam In Tunisia?', 'Islam Makhachev will be fighting in Tunisia.', 'Islam Makhachev will be fighting in Tunisia.', 'PUBLISHED', 54, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_3.jpeg', 'vid_3.mp4'),
(4, 1, 1, 'Judo Cup Sousse Recap: The Tank Emerges', 'The Judo Cup Sousse delivered an electrifying night of combat sports. The breakout star was undoubtedly Fares \"The Tank\" Khelifi, who scored a devastating TKO over Mohamed \"Iron Fist\" Trabelsi in the co-main event. The doctor stoppage came in round 4 after a vicious cut opened above Iron Fist\'s left eye.', 'The Tank emerges as a star at Judo Cup Sousse.', 'PUBLISHED', 0, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_4.jpg', 'vid_4.mp4'),
(5, 2, 1, 'Fighter Profile: Ali \"Bones\" Hammami', 'At 28 years old, Ali \"Bones\" Hammami stands as the #1 ranked heavyweight in SmartFight. With a record of 10-2-1, Bones has established himself as the most complete fighter in the organization. Training out of Tunis under coach Karim, Bones combines devastating striking power with underrated ground game.', '#1 ranked heavyweight profile.', 'PUBLISHED', 2, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_5.jpg', 'vid_5.mp4'),
(6, 4, 1, 'FightSphere FC 1 Predictions: Bones vs The Tank', 'The main event of FightSphere FC 1 pits the #1 ranked Bones against the #3 ranked Tank in what promises to be an explosive heavyweight clash. Bones brings superior technique and experience with a 10-2-1 record. Our prediction: Bones by decision in a war of attrition.', 'Expert predictions for FC 1 main event.', 'PUBLISHED', 3, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_6.avif', 'vid_6.mp4'),
(7, 5, 1, 'Exclusive: Coach Karim on Training Bones for FC 1', '\"We\'ve completely overhauled Ali\'s training camp for this fight,\" Coach Karim revealed in an exclusive interview. \"The Tank is the most physically imposing fighter Ali has faced. We\'ve added 20 pounds of focus to his conditioning program.\"', 'Coach Karim exclusive interview ahead of FC 1.', 'PUBLISHED', 1, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_7.jpg', 'vid_7.mp4'),
(8, 3, 1, '5 Boxing Drills to Improve Your Power', 'Whether you\'re a professional fighter or a weekend warrior, these five drills will help you develop knockout power: 1. Heavy Bag Rounds, 2. Medicine Ball Throws, 3. Plyometric Push-Ups, 4. Shadow Boxing with Resistance Bands, 5. Pad Work Combos.', 'Five essential drills for knockout power.', 'PUBLISHED', 2, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_8.jpg', 'vid_8.mp4'),
(9, 1, 1, 'SmartFight Open 2026: Full Preview', 'The SmartFight Open 2026 kicks off April 10 at Salle Omnisports de Tunis with 4,500 seats and a stacked card. Main Event: Bones vs Iron Fist III. Co-Main: The Tank vs The Cobra.', 'Full preview of SmartFight Open 2026.', 'PUBLISHED', 6, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_9.jpeg', 'vid_9.mp4'),
(10, 2, 1, 'Rising Star: Fares \"The Tank\" Khelifi', 'With a 12-1 record and devastating power, Fares \"The Tank\" Khelifi is the most feared heavyweight contender in SmartFight. At just 30 years old, The Tank has already scored 9 KOs/TKOs in his 12 victories.', 'Profile of rising heavyweight The Tank.', 'PUBLISHED', 1, '2026-04-04 10:53:06', '2026-04-11 23:26:18', 'uploads/blog/img_10.jpg', 'vid_10.mp4'),
(11, 1, 20, 'test', '', 'test', 'PUBLISHED', 7, '2026-04-04 18:34:35', '2026-04-11 23:26:18', 'uploads/blog/img_11.jpg', 'vid_11.mp4'),
(13, 1, 20, 'TEST3 EDIT', '', 'TEST3', 'PUBLISHED', 3, '2026-04-06 08:37:34', '2026-04-11 23:26:18', 'uploads/blog/img_13.jpg', 'vid_13.mp4'),
(14, 1, 20, 'AA', 'AAAAAA', 'AA', 'PUBLISHED', 2, '2026-04-06 10:01:03', '2026-04-11 23:26:18', 'uploads/blog/img_14.jpg', 'vid_14.mp4'),
(15, 1, 20, 'test after img storage changed', 'test after img storage changed', 'test after img storage changed', 'PUBLISHED', 2, '2026-04-06 20:03:22', '2026-04-11 23:26:18', 'uploads/blog/img_15.png', 'vid_15.mp4'),
(16, 1, 20, 'test symfony', 'test symfony', 'test symfony', 'PUBLISHED', 2, '2026-04-12 01:08:18', '2026-04-12 01:09:05', 'bivol-69dad4625fc02306254220.png', NULL);

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
('DoctrineMigrations\\Version20260411222346', '2026-04-12 01:00:48', 782);

-- --------------------------------------------------------

--
-- Table structure for table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` longtext DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` varchar(20) NOT NULL,
  `visibility` varchar(10) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 0,
  `venue_id` int(11) DEFAULT NULL,
  `discipline_id` int(11) DEFAULT NULL,
  `organizer_id` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL
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
(47, 21, 'ADMIN_BROADCAST', 'Test Symfony', 'Welcome to the symfony app', NULL, NULL, 0, '2026-04-12 11:40:20');

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
(7, 15, 'MMA', 'Tunisia', 'UFC and SmartFight hardcore fan');

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
  `user_id` int(11) NOT NULL,
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
  `updated_at` datetime NOT NULL
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
  `created_at` datetime NOT NULL
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
  `notes` longtext DEFAULT NULL
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
  `updated_at` datetime NOT NULL
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
(20, 'ahmed', 'ahmed', 'ahmed@ahmed.com', '$2y$10$yC3nkrsjFDe.61uNByzQrOlcFGvgBwRLdw3LldXKDLcwwrACV1/Pu', '12345678', 1, 1, '2026-04-04 18:33:21', '2026-04-09 22:46:04'),
(21, 'test', 'test', 'test@fan.com', '$2y$10$/3eecYdEiGABj7mRKSVB5OqMjW4XpHo5/nGevKxqjAbUC3Svskzcy', '112233445566', 5, 1, '2026-04-04 18:35:26', '2026-04-09 22:46:04');

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
-- Indexes for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_blog_article_category` (`category_id`),
  ADD KEY `fk_blog_article_author` (`author_id`),
  ADD KEY `IDX_EECCB3E512469DE2` (`category_id`),
  ADD KEY `IDX_EECCB3E5F675F31B` (`author_id`);

--
-- Indexes for table `blog_category`
--
ALTER TABLE `blog_category`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_blog_category_name` (`name`),
  ADD UNIQUE KEY `uq_blog_category_slug` (`slug`),
  ADD UNIQUE KEY `UNIQ_72113DE65E237E06` (`name`),
  ADD UNIQUE KEY `UNIQ_72113DE6989D9B62` (`slug`);

--
-- Indexes for table `discipline`
--
ALTER TABLE `discipline`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_discipline_name` (`name`),
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
  ADD KEY `fk_event_venue` (`venue_id`),
  ADD KEY `fk_event_discipline` (`discipline_id`),
  ADD KEY `fk_event_organizer` (`organizer_id`),
  ADD KEY `IDX_3BAE0AA7A5522701` (`discipline_id`);

--
-- Indexes for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_fn_fan_read` (`fan_id`,`is_read`),
  ADD KEY `idx_fn_type` (`type`),
  ADD KEY `idx_fn_event` (`related_event_id`),
  ADD KEY `IDX_D093B975D774A626` (`related_event_id`);

--
-- Indexes for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_prediction` (`match_proposal_id`,`fan_id`),
  ADD KEY `idx_fp_fan` (`fan_id`),
  ADD KEY `idx_fp_season` (`season`),
  ADD KEY `idx_fp_scored` (`is_scored`),
  ADD KEY `fk_fp_winner` (`predicted_winner_id`),
  ADD KEY `IDX_89572FC989C48F0B` (`fan_id`),
  ADD KEY `IDX_89572FC919EAE00D` (`predicted_winner_id`);

--
-- Indexes for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_preference` (`fan_id`),
  ADD KEY `idx_fpref_discipline` (`favorite_discipline_id`),
  ADD KEY `idx_fpref_fighter` (`favorite_fighter_id`),
  ADD KEY `IDX_E207F05289C48F0B` (`fan_id`),
  ADD KEY `IDX_E207F05278B04DCA` (`favorite_discipline_id`),
  ADD KEY `IDX_E207F0522A58A1BC` (`favorite_fighter_id`);

--
-- Indexes for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_user` (`user_id`),
  ADD UNIQUE KEY `UNIQ_E95F4CF4A76ED395` (`user_id`);

--
-- Indexes for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fan_reaction` (`fight_result_id`,`fan_id`),
  ADD KEY `idx_fr_fight` (`fight_result_id`),
  ADD KEY `idx_fr_fan` (`fan_id`),
  ADD KEY `idx_fr_pinned` (`is_pinned`),
  ADD KEY `idx_fr_deleted` (`is_deleted`),
  ADD KEY `IDX_8ED024852F087A36` (`fight_result_id`),
  ADD KEY `IDX_8ED0248589C48F0B` (`fan_id`);

--
-- Indexes for table `fighter`
--
ALTER TABLE `fighter`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_fighter_user` (`user_id`),
  ADD KEY `fk_fighter_weight` (`weight_class_id`),
  ADD KEY `IDX_7A08C3FCA76ED395` (`user_id`);

--
-- Indexes for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_fr_event` (`event_id`),
  ADD KEY `fk_fr_match` (`match_id`),
  ADD KEY `fk_fr_red` (`fighter_red_id`),
  ADD KEY `fk_fr_blue` (`fighter_blue_id`),
  ADD KEY `fk_fr_winner` (`winner_id`),
  ADD KEY `IDX_1C8CFD0B71F7E88B` (`event_id`),
  ADD KEY `IDX_1C8CFD0B2ABEACD6` (`match_id`),
  ADD KEY `IDX_1C8CFD0B72DD592F` (`fighter_red_id`),
  ADD KEY `IDX_1C8CFD0B24F12652` (`fighter_blue_id`),
  ADD KEY `IDX_1C8CFD0B5DFCD4B8` (`winner_id`);

--
-- Indexes for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mp_event` (`event_id`),
  ADD KEY `fk_mp_fighter1` (`fighter1_id`),
  ADD KEY `fk_mp_fighter2` (`fighter2_id`),
  ADD KEY `IDX_629F4EE471F7E88B` (`event_id`),
  ADD KEY `IDX_629F4EE4D783CFD6` (`fighter1_id`),
  ADD KEY `IDX_629F4EE4C5366038` (`fighter2_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_email` (`email`),
  ADD UNIQUE KEY `UNIQ_8D93D649E7927C74` (`email`),
  ADD KEY `fk_user_role` (`role_id`),
  ADD KEY `IDX_8D93D649D60322AC` (`role_id`);

--
-- Indexes for table `user_role`
--
ALTER TABLE `user_role`
  ADD PRIMARY KEY (`id`);

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
-- AUTO_INCREMENT for table `blog_article`
--
ALTER TABLE `blog_article`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `blog_category`
--
ALTER TABLE `blog_category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

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
-- AUTO_INCREMENT for table `fan_notification`
--
ALTER TABLE `fan_notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

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
-- AUTO_INCREMENT for table `fight_result`
--
ALTER TABLE `fight_result`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `match_proposal`
--
ALTER TABLE `match_proposal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

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
-- Constraints for table `blog_article`
--
ALTER TABLE `blog_article`
  ADD CONSTRAINT `FK_EECCB3E512469DE2` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`),
  ADD CONSTRAINT `FK_EECCB3E5F675F31B` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_blog_article_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_blog_article_category` FOREIGN KEY (`category_id`) REFERENCES `blog_category` (`id`) ON UPDATE CASCADE;

--
-- Constraints for table `event`
--
ALTER TABLE `event`
  ADD CONSTRAINT `FK_3BAE0AA7A5522701` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`),
  ADD CONSTRAINT `fk_event_discipline` FOREIGN KEY (`discipline_id`) REFERENCES `discipline` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_organizer` FOREIGN KEY (`organizer_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fan_notification`
--
ALTER TABLE `fan_notification`
  ADD CONSTRAINT `FK_D093B97589C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_D093B975D774A626` FOREIGN KEY (`related_event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `fk_fn_event` FOREIGN KEY (`related_event_id`) REFERENCES `event` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fn_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_prediction`
--
ALTER TABLE `fan_prediction`
  ADD CONSTRAINT `FK_89572FC919EAE00D` FOREIGN KEY (`predicted_winner_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_89572FC989C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_89572FC9C6E05170` FOREIGN KEY (`match_proposal_id`) REFERENCES `match_proposal` (`id`),
  ADD CONSTRAINT `fk_fp_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fp_match` FOREIGN KEY (`match_proposal_id`) REFERENCES `match_proposal` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fp_winner` FOREIGN KEY (`predicted_winner_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_preference`
--
ALTER TABLE `fan_preference`
  ADD CONSTRAINT `FK_E207F0522A58A1BC` FOREIGN KEY (`favorite_fighter_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_E207F05278B04DCA` FOREIGN KEY (`favorite_discipline_id`) REFERENCES `discipline` (`id`),
  ADD CONSTRAINT `FK_E207F05289C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_fpref_discipline` FOREIGN KEY (`favorite_discipline_id`) REFERENCES `discipline` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fpref_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fpref_fighter` FOREIGN KEY (`favorite_fighter_id`) REFERENCES `fighter` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fan_profile`
--
ALTER TABLE `fan_profile`
  ADD CONSTRAINT `FK_E95F4CF4A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_fan_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fan_reaction`
--
ALTER TABLE `fan_reaction`
  ADD CONSTRAINT `FK_8ED024852F087A36` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`),
  ADD CONSTRAINT `FK_8ED0248589C48F0B` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_frx_fan` FOREIGN KEY (`fan_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_frx_result` FOREIGN KEY (`fight_result_id`) REFERENCES `fight_result` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fighter`
--
ALTER TABLE `fighter`
  ADD CONSTRAINT `FK_7A08C3FCA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_fighter_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fighter_weight` FOREIGN KEY (`weight_class_id`) REFERENCES `weight_class` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `fight_result`
--
ALTER TABLE `fight_result`
  ADD CONSTRAINT `FK_1C8CFD0B24F12652` FOREIGN KEY (`fighter_blue_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B2ABEACD6` FOREIGN KEY (`match_id`) REFERENCES `match_proposal` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B5DFCD4B8` FOREIGN KEY (`winner_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `FK_1C8CFD0B72DD592F` FOREIGN KEY (`fighter_red_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `fk_fr_blue` FOREIGN KEY (`fighter_blue_id`) REFERENCES `fighter` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_match` FOREIGN KEY (`match_id`) REFERENCES `match_proposal` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_red` FOREIGN KEY (`fighter_red_id`) REFERENCES `fighter` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fr_winner` FOREIGN KEY (`winner_id`) REFERENCES `fighter` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `match_proposal`
--
ALTER TABLE `match_proposal`
  ADD CONSTRAINT `FK_629F4EE471F7E88B` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  ADD CONSTRAINT `FK_629F4EE4C5366038` FOREIGN KEY (`fighter2_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `FK_629F4EE4D783CFD6` FOREIGN KEY (`fighter1_id`) REFERENCES `fighter` (`id`),
  ADD CONSTRAINT `fk_mp_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_mp_fighter1` FOREIGN KEY (`fighter1_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_mp_fighter2` FOREIGN KEY (`fighter2_id`) REFERENCES `fighter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `FK_8D93D649D60322AC` FOREIGN KEY (`role_id`) REFERENCES `user_role` (`id`),
  ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `user_role` (`id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
