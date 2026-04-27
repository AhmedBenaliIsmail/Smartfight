<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Fan Experience Entities
 */
final class Version20260424000001 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create tables for Blog, Booking, Reactions, and Fan Profiles';
    }

    public function up(Schema $schema): void
    {
        // Blog Category
        $this->addSql('CREATE TABLE blog_category (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(120) NOT NULL, description TEXT DEFAULT NULL, slug VARCHAR(150) NOT NULL, image_url VARCHAR(255) DEFAULT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, UNIQUE INDEX UNIQ_NAME (name), UNIQUE INDEX UNIQ_SLUG (slug), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Blog Article
        $this->addSql('CREATE TABLE blog_article (id INT AUTO_INCREMENT NOT NULL, category_id INT NOT NULL, author_id INT NOT NULL, title VARCHAR(220) NOT NULL, content TEXT NOT NULL, summary TEXT DEFAULT NULL, status VARCHAR(20) NOT NULL, view_count INT DEFAULT 0 NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, image_path VARCHAR(255) DEFAULT NULL, video_path VARCHAR(255) DEFAULT NULL, INDEX IDX_BLOG_CATEGORY (category_id), INDEX IDX_BLOG_AUTHOR (author_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Discipline
        $this->addSql('CREATE TABLE discipline (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(50) NOT NULL, description TEXT DEFAULT NULL, UNIQUE INDEX UNIQ_DISC_NAME (name), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Event Booking
        $this->addSql('CREATE TABLE event_booking (id INT AUTO_INCREMENT NOT NULL, event_id INT NOT NULL, user_id INT NOT NULL, booking_status VARCHAR(20) NOT NULL, ticket_quantity INT DEFAULT 1 NOT NULL, total_price NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL, ticket_type VARCHAR(20) NOT NULL, booking_date DATE NOT NULL, booking_reference VARCHAR(64) NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, INDEX IDX_EB_EVENT (event_id), INDEX IDX_EB_USER (user_id), UNIQUE INDEX uq_eb_event_user (event_id, user_id), UNIQUE INDEX uq_eb_reference (booking_reference), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Fan Preference
        $this->addSql('CREATE TABLE fan_preference (id INT AUTO_INCREMENT NOT NULL, fan_id INT NOT NULL, favorite_discipline_id INT DEFAULT NULL, favorite_fighter_id INT DEFAULT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, INDEX IDX_FP_FAN (fan_id), INDEX IDX_FP_DISC (favorite_discipline_id), INDEX IDX_FP_FIGHTER (favorite_fighter_id), UNIQUE INDEX uq_fan_preference (fan_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Fan Profile
        $this->addSql('CREATE TABLE fan_profile (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, favorite_sport VARCHAR(100) DEFAULT NULL, country VARCHAR(100) DEFAULT NULL, bio TEXT DEFAULT NULL, UNIQUE INDEX UNIQ_FP_USER (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Fan Reaction
        $this->addSql('CREATE TABLE fan_reaction (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, fan_id INT NOT NULL, reaction_type VARCHAR(20) NOT NULL, comment VARCHAR(140) DEFAULT NULL, reacted_at DATETIME NOT NULL, is_pinned TINYINT(1) DEFAULT 0 NOT NULL, is_deleted TINYINT(1) DEFAULT 0 NOT NULL, INDEX IDX_FR_FIGHT (fight_result_id), INDEX IDX_FR_FAN (fan_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Match Proposal
        $this->addSql('CREATE TABLE match_proposal (id INT AUTO_INCREMENT NOT NULL, event_id INT DEFAULT NULL, fighter1_id INT NOT NULL, fighter2_id INT NOT NULL, compatibility NUMERIC(5, 2) DEFAULT NULL, status VARCHAR(20) NOT NULL, proposed_at DATETIME NOT NULL, notes TEXT DEFAULT NULL, INDEX IDX_MP_EVENT (event_id), INDEX IDX_MP_F1 (fighter1_id), INDEX IDX_MP_F2 (fighter2_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        // Foreign Keys
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT FK_BLOG_CATEGORY FOREIGN KEY (category_id) REFERENCES blog_category (id)');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT FK_BLOG_AUTHOR FOREIGN KEY (author_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT FK_EB_EVENT FOREIGN KEY (event_id) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT FK_EB_USER FOREIGN KEY (user_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_FP_FAN FOREIGN KEY (fan_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_FP_DISC FOREIGN KEY (favorite_discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_FP_FIGHTER FOREIGN KEY (favorite_fighter_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT FK_FP_USER FOREIGN KEY (user_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_FR_FIGHT FOREIGN KEY (fight_result_id) REFERENCES fight_results (resultId)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_FR_FAN FOREIGN KEY (fan_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_MP_EVENT FOREIGN KEY (event_id) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_MP_F1 FOREIGN KEY (fighter1_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_MP_F2 FOREIGN KEY (fighter2_id) REFERENCES fighters (fighterId)');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_BLOG_CATEGORY');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_BLOG_AUTHOR');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY FK_EB_EVENT');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY FK_EB_USER');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_FP_FAN');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_FP_DISC');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_FP_FIGHTER');
        $this->addSql('ALTER TABLE fan_profile DROP FOREIGN KEY FK_FP_USER');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_FR_FIGHT');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_FR_FAN');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_MP_EVENT');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_MP_F1');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_MP_F2');
        $this->addSql('DROP TABLE blog_category');
        $this->addSql('DROP TABLE blog_article');
        $this->addSql('DROP TABLE discipline');
        $this->addSql('DROP TABLE event_booking');
        $this->addSql('DROP TABLE fan_preference');
        $this->addSql('DROP TABLE fan_profile');
        $this->addSql('DROP TABLE fan_reaction');
        $this->addSql('DROP TABLE match_proposal');
    }
}
