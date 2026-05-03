<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260503134232 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('DROP TABLE event');
        $this->addSql('DROP TABLE fan_notification');
        $this->addSql('DROP TABLE fan_prediction');
        $this->addSql('DROP TABLE fighter');
        $this->addSql('DROP TABLE fight_result');
        $this->addSql('DROP TABLE fight_stat');
        $this->addSql('DROP TABLE system_meta');
        $this->addSql('DROP TABLE user');
        $this->addSql('DROP TABLE user_role');
        $this->addSql('DROP TABLE weight_class');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY `FK_EECCB3E5F675F31B`');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT FK_EECCB3E5F675F31B FOREIGN KEY (author_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY `FK_655B447171F7E88B`');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY `FK_655B4471A76ED395`');
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT 0 NOT NULL, CHANGE ticket_type ticket_type VARCHAR(30) NOT NULL');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT FK_655B447171F7E88B FOREIGN KEY (event_id) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT FK_655B4471A76ED395 FOREIGN KEY (user_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE events ADD latitude DOUBLE PRECISION DEFAULT NULL, ADD longitude DOUBLE PRECISION DEFAULT NULL');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY `FK_E207F0522A58A1BC`');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY `FK_E207F05289C48F0B`');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F0522A58A1BC FOREIGN KEY (favorite_fighter_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F05289C48F0B FOREIGN KEY (fan_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fan_profile DROP FOREIGN KEY `FK_E95F4CF4A76ED395`');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT FK_E95F4CF4A76ED395 FOREIGN KEY (user_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY `FK_8ED024852F087A36`');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY `FK_8ED0248589C48F0B`');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED024852F087A36 FOREIGN KEY (fight_result_id) REFERENCES fight_results (resultId)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED0248589C48F0B FOREIGN KEY (fan_id) REFERENCES users (userId)');
        $this->addSql('ALTER TABLE fight_statistic ADD body_shots_landed INT DEFAULT 0 NOT NULL, ADD body_jabs_landed INT DEFAULT 0 NOT NULL, ADD body_power_landed INT DEFAULT 0 NOT NULL, ADD jabs_landed INT DEFAULT 0 NOT NULL, ADD jabs_thrown INT DEFAULT 0 NOT NULL, ADD power_punches_landed INT DEFAULT 0 NOT NULL, ADD power_punches_thrown INT DEFAULT 0 NOT NULL, ADD uppercuts_landed INT DEFAULT 0 NOT NULL, ADD uppercuts_thrown INT DEFAULT 0 NOT NULL, ADD right_hand_landed INT DEFAULT 0 NOT NULL, ADD right_hand_thrown INT DEFAULT 0 NOT NULL, ADD left_hand_landed INT DEFAULT 0 NOT NULL, ADD left_hand_thrown INT DEFAULT 0 NOT NULL, ADD commentary LONGTEXT DEFAULT NULL, DROP strikes_landed, DROP strikes_attempted, DROP takedowns_landed, DROP takedowns_attempted, DROP submission_attempts, DROP control_time_seconds, DROP created_at, DROP updated_at, CHANGE round_number round INT DEFAULT NULL');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT FK_4C348AE92F087A36 FOREIGN KEY (fight_result_id) REFERENCES fight_results (resultId)');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT FK_4C348AE934934341 FOREIGN KEY (fighter_id) REFERENCES fighters (fighterId)');
        $this->addSql('CREATE INDEX IDX_4C348AE92F087A36 ON fight_statistic (fight_result_id)');
        $this->addSql('CREATE INDEX IDX_4C348AE934934341 ON fight_statistic (fighter_id)');
        $this->addSql('ALTER TABLE fighters CHANGE eloRating eloRating DOUBLE PRECISION DEFAULT 1500 NOT NULL, CHANGE performanceScore performanceScore DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE strengthOfSchedule strengthOfSchedule DOUBLE PRECISION DEFAULT 1500 NOT NULL');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY `FK_629F4EE471F7E88B`');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY `FK_629F4EE4C5366038`');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY `FK_629F4EE4D783CFD6`');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY `FK_MP_WEIGHT_CLASS`');
        $this->addSql('DROP INDEX IDX_629F4EE4A0206A65 ON match_proposal');
        $this->addSql('ALTER TABLE match_proposal ADD vote_count INT DEFAULT 0 NOT NULL, ADD weight_division_id INT DEFAULT NULL, DROP scheduled_rounds, DROP is_title_fight, DROP weight_class_id, DROP odds_fighter1, DROP odds_fighter2, DROP card_position, DROP card_type');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE471F7E88B FOREIGN KEY (event_id) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4C5366038 FOREIGN KEY (fighter2_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4D783CFD6 FOREIGN KEY (fighter1_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE454374256 FOREIGN KEY (weight_division_id) REFERENCES weight_division (id)');
        $this->addSql('CREATE INDEX IDX_629F4EE454374256 ON match_proposal (weight_division_id)');
        $this->addSql('ALTER TABLE performance_score ADD calculated_at DATETIME DEFAULT NULL, DROP season, DROP computed_at, DROP created_at, DROP updated_at, CHANGE score score DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT NULL');
        $this->addSql('ALTER TABLE performance_score ADD CONSTRAINT FK_6A3980CE34934341 FOREIGN KEY (fighter_id) REFERENCES fighters (fighterId)');
        $this->addSql('CREATE INDEX IDX_6A3980CE34934341 ON performance_score (fighter_id)');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY `FK_80B839D0A0206A65`');
        $this->addSql('DROP INDEX IDX_80B839D0A0206A65 ON ranking');
        $this->addSql('ALTER TABLE ranking ADD organization VARCHAR(20) DEFAULT \'MEDIA\' NOT NULL, ADD is_champion TINYINT DEFAULT 0 NOT NULL, ADD last_fight_date DATE DEFAULT NULL, DROP weight_class, DROP season, DROP created_at, CHANGE rank_position rank_position INT DEFAULT 999 NOT NULL, CHANGE points points DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT NULL, CHANGE weight_class_id weight_division_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_80B839D034934341 FOREIGN KEY (fighter_id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_80B839D054374256 FOREIGN KEY (weight_division_id) REFERENCES weight_division (id)');
        $this->addSql('CREATE INDEX IDX_80B839D034934341 ON ranking (fighter_id)');
        $this->addSql('CREATE INDEX IDX_80B839D054374256 ON ranking (weight_division_id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE event (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description LONGTEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, status VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, visibility VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, capacity INT DEFAULT 0 NOT NULL, discipline_id INT DEFAULT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, starts_at DATETIME DEFAULT NULL, ends_at DATETIME DEFAULT NULL, venue_name VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, city VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, country VARCHAR(2) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, poster_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, INDEX IDX_3BAE0AA7A5522701 (discipline_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fan_notification (id INT AUTO_INCREMENT NOT NULL, fan_id INT NOT NULL, type VARCHAR(30) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, title VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, message LONGTEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, related_event_id INT DEFAULT NULL, related_fight_id INT DEFAULT NULL, is_read TINYINT DEFAULT 0 NOT NULL, created_at DATETIME NOT NULL, INDEX IDX_D093B975D774A626 (related_event_id), INDEX FK_D093B97589C48F0B (fan_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'Fan Notification Center — inbox per fan\' ');
        $this->addSql('CREATE TABLE fan_prediction (id INT AUTO_INCREMENT NOT NULL, match_proposal_id INT NOT NULL, fan_id INT NOT NULL, predicted_winner_id INT NOT NULL, predicted_method VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, submitted_at DATETIME NOT NULL, is_locked TINYINT DEFAULT 1 NOT NULL, points_earned INT DEFAULT NULL, is_scored TINYINT DEFAULT 0 NOT NULL, season VARCHAR(10) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, INDEX IDX_89572FC989C48F0B (fan_id), INDEX IDX_89572FC919EAE00D (predicted_winner_id), UNIQUE INDEX uq_fan_prediction (match_proposal_id, fan_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'Fan Prediction League — one prediction per fan per fight\' ');
        $this->addSql('CREATE TABLE fighter (id INT AUTO_INCREMENT NOT NULL, user_id INT DEFAULT NULL, nickname VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_of_birth DATE DEFAULT NULL, nationality VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, photo_url VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, weight_class_id INT DEFAULT NULL, wins INT DEFAULT 0 NOT NULL, losses INT DEFAULT 0 NOT NULL, draws INT DEFAULT 0 NOT NULL, status VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, elo_rating DOUBLE PRECISION DEFAULT \'1500\' NOT NULL, performance_score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, win_streak INT DEFAULT 0 NOT NULL, strength_of_schedule DOUBLE PRECISION DEFAULT \'1500\' NOT NULL, ko_wins INT DEFAULT 0 NOT NULL, submission_wins INT DEFAULT 0 NOT NULL, decision_wins INT DEFAULT 0 NOT NULL, champions_event_win_streak INT DEFAULT 0 NOT NULL, title_defenses INT DEFAULT 0 NOT NULL, height INT DEFAULT NULL, reach INT DEFAULT NULL, weight_class VARCHAR(50) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, first_name VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, last_name VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, country_code VARCHAR(2) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, discipline_id INT NOT NULL, INDEX IDX_7A08C3FCA5522701 (discipline_id), INDEX IDX_7A08C3FCA76ED395 (user_id), INDEX IDX_7A08C3FCA0206A65 (weight_class_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fight_result (id INT AUTO_INCREMENT NOT NULL, event_id INT NOT NULL, match_id INT DEFAULT NULL, fighter_red_id INT NOT NULL, fighter_blue_id INT NOT NULL, winner_id INT DEFAULT NULL, method VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, round_ended INT DEFAULT NULL, time_ended TIME DEFAULT NULL, notes LONGTEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, fight_date DATE NOT NULL, created_at DATETIME NOT NULL, fight_number INT DEFAULT 1 NOT NULL, status VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'COMPLETED\' NOT NULL COLLATE `utf8mb4_unicode_ci`, knockdowns_fighter_red INT DEFAULT 0 NOT NULL, knockdowns_fighter_blue INT DEFAULT 0 NOT NULL, INDEX IDX_1C8CFD0B24F12652 (fighter_blue_id), INDEX IDX_1C8CFD0B71F7E88B (event_id), INDEX IDX_1C8CFD0B5DFCD4B8 (winner_id), INDEX IDX_1C8CFD0B2ABEACD6 (match_id), INDEX IDX_1C8CFD0B72DD592F (fighter_red_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fight_stat (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, fighter_id INT NOT NULL, round_number INT DEFAULT 0 NOT NULL, strikes_thrown INT DEFAULT 0 NOT NULL, strikes_landed INT DEFAULT 0 NOT NULL, power_shots_thrown INT DEFAULT 0 NOT NULL, power_shots_landed INT DEFAULT 0 NOT NULL, jabs_thrown INT DEFAULT 0 NOT NULL, jabs_landed INT DEFAULT 0 NOT NULL, knockdowns INT DEFAULT 0 NOT NULL, punches_thrown INT DEFAULT 0 NOT NULL, punches_landed INT DEFAULT 0 NOT NULL, head_shots_landed INT DEFAULT 0 NOT NULL, body_shots_landed INT DEFAULT 0 NOT NULL, clinch_strikes_thrown INT DEFAULT 0 NOT NULL, clinch_strikes_landed INT DEFAULT 0 NOT NULL, created_at DATETIME NOT NULL, INDEX IDX_fight_stat_fighter (fighter_id), INDEX IDX_fight_stat_round (fight_result_id, fighter_id, round_number), INDEX IDX_fight_stat_result (fight_result_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE system_meta (id INT AUTO_INCREMENT NOT NULL, meta_key VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, value VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, updatedAt DATETIME NOT NULL, UNIQUE INDEX UNIQ_C63EA7C3251C7524 (meta_key), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE user (id INT AUTO_INCREMENT NOT NULL, first_name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, last_name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, email VARCHAR(150) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, password VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, phone VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, role_id INT NOT NULL, is_active TINYINT DEFAULT 1 NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, username VARCHAR(50) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, INDEX IDX_8D93D649D60322AC (role_id), UNIQUE INDEX UNIQ_8D93D649E7927C74 (email), UNIQUE INDEX UNIQ_8D93D649F85E0677 (username), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE user_role (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE weight_class (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, min_weight NUMERIC(5, 2) DEFAULT NULL, max_weight NUMERIC(5, 2) DEFAULT NULL, discipline_id INT NOT NULL, champion_id INT DEFAULT NULL, displayOrder INT DEFAULT 0 NOT NULL, slug VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, INDEX IDX_D2EC4C2FA5522701 (discipline_id), INDEX IDX_D2EC4C2FFA7FD7EB (champion_id), UNIQUE INDEX UNIQ_D2EC4C2F989D9B62 (slug), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_EECCB3E5F675F31B');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT `FK_EECCB3E5F675F31B` FOREIGN KEY (author_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE events DROP latitude, DROP longitude');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY FK_655B447171F7E88B');
        $this->addSql('ALTER TABLE event_booking DROP FOREIGN KEY FK_655B4471A76ED395');
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL, CHANGE ticket_type ticket_type VARCHAR(20) NOT NULL');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT `FK_655B447171F7E88B` FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT `FK_655B4471A76ED395` FOREIGN KEY (user_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F05289C48F0B');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F0522A58A1BC');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT `FK_E207F05289C48F0B` FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT `FK_E207F0522A58A1BC` FOREIGN KEY (favorite_fighter_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fan_profile DROP FOREIGN KEY FK_E95F4CF4A76ED395');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT `FK_E95F4CF4A76ED395` FOREIGN KEY (user_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED024852F087A36');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED0248589C48F0B');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT `FK_8ED024852F087A36` FOREIGN KEY (fight_result_id) REFERENCES fight_result (id)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT `FK_8ED0248589C48F0B` FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fighters CHANGE eloRating eloRating DOUBLE PRECISION DEFAULT \'1500\' NOT NULL, CHANGE performanceScore performanceScore DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE strengthOfSchedule strengthOfSchedule DOUBLE PRECISION DEFAULT \'1500\' NOT NULL');
        $this->addSql('ALTER TABLE fight_statistic DROP FOREIGN KEY FK_4C348AE92F087A36');
        $this->addSql('ALTER TABLE fight_statistic DROP FOREIGN KEY FK_4C348AE934934341');
        $this->addSql('DROP INDEX IDX_4C348AE92F087A36 ON fight_statistic');
        $this->addSql('DROP INDEX IDX_4C348AE934934341 ON fight_statistic');
        $this->addSql('ALTER TABLE fight_statistic ADD strikes_landed INT DEFAULT 0 NOT NULL, ADD strikes_attempted INT DEFAULT 0 NOT NULL, ADD takedowns_landed INT DEFAULT 0 NOT NULL, ADD takedowns_attempted INT DEFAULT 0 NOT NULL, ADD submission_attempts INT DEFAULT 0 NOT NULL, ADD control_time_seconds INT DEFAULT 0 NOT NULL, ADD created_at DATETIME NOT NULL, ADD updated_at DATETIME NOT NULL, DROP body_shots_landed, DROP body_jabs_landed, DROP body_power_landed, DROP jabs_landed, DROP jabs_thrown, DROP power_punches_landed, DROP power_punches_thrown, DROP uppercuts_landed, DROP uppercuts_thrown, DROP right_hand_landed, DROP right_hand_thrown, DROP left_hand_landed, DROP left_hand_thrown, DROP commentary, CHANGE round round_number INT DEFAULT NULL');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE471F7E88B');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4D783CFD6');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4C5366038');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE454374256');
        $this->addSql('DROP INDEX IDX_629F4EE454374256 ON match_proposal');
        $this->addSql('ALTER TABLE match_proposal ADD is_title_fight TINYINT DEFAULT 0 NOT NULL, ADD weight_class_id INT DEFAULT NULL, ADD odds_fighter1 NUMERIC(5, 2) DEFAULT NULL, ADD odds_fighter2 NUMERIC(5, 2) DEFAULT NULL, ADD card_position INT DEFAULT NULL, ADD card_type VARCHAR(20) DEFAULT NULL, DROP vote_count, CHANGE weight_division_id scheduled_rounds INT DEFAULT NULL');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT `FK_629F4EE471F7E88B` FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT `FK_629F4EE4D783CFD6` FOREIGN KEY (fighter1_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT `FK_629F4EE4C5366038` FOREIGN KEY (fighter2_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT `FK_MP_WEIGHT_CLASS` FOREIGN KEY (weight_class_id) REFERENCES weight_class (id)');
        $this->addSql('CREATE INDEX IDX_629F4EE4A0206A65 ON match_proposal (weight_class_id)');
        $this->addSql('ALTER TABLE performance_score DROP FOREIGN KEY FK_6A3980CE34934341');
        $this->addSql('DROP INDEX IDX_6A3980CE34934341 ON performance_score');
        $this->addSql('ALTER TABLE performance_score ADD season VARCHAR(20) NOT NULL, ADD computed_at DATETIME NOT NULL, ADD created_at DATETIME NOT NULL, ADD updated_at DATETIME NOT NULL, DROP calculated_at, CHANGE score score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_80B839D034934341');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_80B839D054374256');
        $this->addSql('DROP INDEX IDX_80B839D034934341 ON ranking');
        $this->addSql('DROP INDEX IDX_80B839D054374256 ON ranking');
        $this->addSql('ALTER TABLE ranking ADD weight_class VARCHAR(50) NOT NULL, ADD season VARCHAR(20) NOT NULL, ADD created_at DATETIME NOT NULL, DROP organization, DROP is_champion, DROP last_fight_date, CHANGE rank_position rank_position INT NOT NULL, CHANGE points points DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL, CHANGE weight_division_id weight_class_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT `FK_80B839D0A0206A65` FOREIGN KEY (weight_class_id) REFERENCES weight_class (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX IDX_80B839D0A0206A65 ON ranking (weight_class_id)');
    }
}
