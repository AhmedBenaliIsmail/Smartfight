<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260411222346 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // Legacy tables (venue, weight_class) are intentionally kept.
        // blog_article, blog_category, discipline, event: FK_* and IDX_* already applied by a
        // previous partial run — only column-type changes remain for those tables.
        $this->addSql('SET FOREIGN_KEY_CHECKS=0');

        // ── Column type changes (idempotent) ──────────────────────────────────────────
        $this->addSql('ALTER TABLE blog_article CHANGE summary summary LONGTEXT DEFAULT NULL, CHANGE status status VARCHAR(20) NOT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE blog_category CHANGE description description LONGTEXT DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE discipline CHANGE description description LONGTEXT DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE event CHANGE description description LONGTEXT DEFAULT NULL, CHANGE status status VARCHAR(20) NOT NULL, CHANGE visibility visibility VARCHAR(10) NOT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE fan_notification CHANGE type type VARCHAR(30) NOT NULL, CHANGE message message LONGTEXT NOT NULL, CHANGE created_at created_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE fan_prediction CHANGE predicted_method predicted_method VARCHAR(20) NOT NULL, CHANGE submitted_at submitted_at DATETIME NOT NULL, CHANGE season season VARCHAR(10) NOT NULL');
        $this->addSql('ALTER TABLE fan_preference CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE fan_profile CHANGE bio bio LONGTEXT DEFAULT NULL');
        $this->addSql('ALTER TABLE fan_reaction CHANGE reaction_type reaction_type VARCHAR(20) NOT NULL, CHANGE reacted_at reacted_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE fight_result CHANGE method method VARCHAR(20) NOT NULL, CHANGE notes notes LONGTEXT DEFAULT NULL, CHANGE created_at created_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE fighter CHANGE status status VARCHAR(20) NOT NULL, CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');
        $this->addSql('ALTER TABLE match_proposal CHANGE status status VARCHAR(20) NOT NULL, CHANGE proposed_at proposed_at DATETIME NOT NULL, CHANGE notes notes LONGTEXT DEFAULT NULL');
        $this->addSql('ALTER TABLE user CHANGE created_at created_at DATETIME NOT NULL, CHANGE updated_at updated_at DATETIME NOT NULL');

        // ── New Doctrine indexes (only ones not yet in the DB) ────────────────────────
        $this->addSql('CREATE INDEX IDX_D093B975D774A626 ON fan_notification (related_event_id)');
        $this->addSql('CREATE INDEX IDX_89572FC989C48F0B ON fan_prediction (fan_id)');
        $this->addSql('CREATE INDEX IDX_89572FC919EAE00D ON fan_prediction (predicted_winner_id)');
        $this->addSql('CREATE INDEX IDX_E207F05289C48F0B ON fan_preference (fan_id)');
        $this->addSql('CREATE INDEX IDX_E207F05278B04DCA ON fan_preference (favorite_discipline_id)');
        $this->addSql('CREATE INDEX IDX_E207F0522A58A1BC ON fan_preference (favorite_fighter_id)');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_E95F4CF4A76ED395 ON fan_profile (user_id)');
        $this->addSql('CREATE INDEX IDX_8ED024852F087A36 ON fan_reaction (fight_result_id)');
        $this->addSql('CREATE INDEX IDX_8ED0248589C48F0B ON fan_reaction (fan_id)');
        $this->addSql('CREATE INDEX IDX_1C8CFD0B71F7E88B ON fight_result (event_id)');
        $this->addSql('CREATE INDEX IDX_1C8CFD0B2ABEACD6 ON fight_result (match_id)');
        $this->addSql('CREATE INDEX IDX_1C8CFD0B72DD592F ON fight_result (fighter_red_id)');
        $this->addSql('CREATE INDEX IDX_1C8CFD0B24F12652 ON fight_result (fighter_blue_id)');
        $this->addSql('CREATE INDEX IDX_1C8CFD0B5DFCD4B8 ON fight_result (winner_id)');
        $this->addSql('CREATE INDEX IDX_7A08C3FCA76ED395 ON fighter (user_id)');
        $this->addSql('CREATE INDEX IDX_629F4EE471F7E88B ON match_proposal (event_id)');
        $this->addSql('CREATE INDEX IDX_629F4EE4D783CFD6 ON match_proposal (fighter1_id)');
        $this->addSql('CREATE INDEX IDX_629F4EE4C5366038 ON match_proposal (fighter2_id)');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_8D93D649E7927C74 ON user (email)');
        $this->addSql('CREATE INDEX IDX_8D93D649D60322AC ON user (role_id)');

        // ── New Doctrine FK constraints (fk_* ones already exist from original schema) ─
        $this->addSql('ALTER TABLE fan_notification ADD CONSTRAINT FK_D093B97589C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_notification ADD CONSTRAINT FK_D093B975D774A626 FOREIGN KEY (related_event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT FK_89572FC9C6E05170 FOREIGN KEY (match_proposal_id) REFERENCES match_proposal (id)');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT FK_89572FC989C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT FK_89572FC919EAE00D FOREIGN KEY (predicted_winner_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F05289C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F05278B04DCA FOREIGN KEY (favorite_discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F0522A58A1BC FOREIGN KEY (favorite_fighter_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT FK_E95F4CF4A76ED395 FOREIGN KEY (user_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED024852F087A36 FOREIGN KEY (fight_result_id) REFERENCES fight_result (id)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED0248589C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B71F7E88B FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B2ABEACD6 FOREIGN KEY (match_id) REFERENCES match_proposal (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B72DD592F FOREIGN KEY (fighter_red_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B24F12652 FOREIGN KEY (fighter_blue_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B5DFCD4B8 FOREIGN KEY (winner_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fighter ADD CONSTRAINT FK_7A08C3FCA76ED395 FOREIGN KEY (user_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE471F7E88B FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4D783CFD6 FOREIGN KEY (fighter1_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4C5366038 FOREIGN KEY (fighter2_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE user ADD CONSTRAINT FK_8D93D649D60322AC FOREIGN KEY (role_id) REFERENCES user_role (id)');

        // ── Remove old index Doctrine does not track ──────────────────────────────────
        $this->addSql('DROP INDEX uq_role_name ON user_role');

        $this->addSql('SET FOREIGN_KEY_CHECKS=1');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE admin (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, access_level VARCHAR(0) CHARACTER SET utf8mb4 DEFAULT \'STANDARD\' NOT NULL COLLATE `utf8mb4_unicode_ci`, department VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, UNIQUE INDEX uq_admin_user (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE coach (id INT AUTO_INCREMENT NOT NULL, user_id INT NOT NULL, speciality VARCHAR(150) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, experience_years INT DEFAULT 0 NOT NULL, certification VARCHAR(200) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, status VARCHAR(0) CHARACTER SET utf8mb4 DEFAULT \'ACTIVE\' NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_coach_user (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE event_booking (id INT AUTO_INCREMENT NOT NULL, event_id INT NOT NULL, user_id INT NOT NULL, booking_status VARCHAR(0) CHARACTER SET utf8mb4 DEFAULT \'PENDING\' NOT NULL COLLATE `utf8mb4_unicode_ci`, ticket_quantity INT DEFAULT 1 NOT NULL, total_price NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL, ticket_type VARCHAR(0) CHARACTER SET utf8mb4 DEFAULT \'REGULAR\' NOT NULL COLLATE `utf8mb4_unicode_ci`, booking_date DATE NOT NULL, booking_reference VARCHAR(64) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_event_booking_reference (booking_reference), INDEX fk_event_booking_event (event_id), INDEX fk_event_booking_user (user_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE event_fighter (id INT AUTO_INCREMENT NOT NULL, event_id INT NOT NULL, fighter_id INT NOT NULL, registration_date DATE DEFAULT CURRENT_DATE NOT NULL, weight_at_event NUMERIC(5, 2) DEFAULT NULL, corner VARCHAR(0) CHARACTER SET utf8mb4 DEFAULT \'RED\' NOT NULL COLLATE `utf8mb4_unicode_ci`, UNIQUE INDEX uq_event_fighter (event_id, fighter_id), INDEX fk_ef_fighter (fighter_id), INDEX IDX_FF5F6A5371F7E88B (event_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE event_schedule (id INT AUTO_INCREMENT NOT NULL, event_id INT NOT NULL, title VARCHAR(200) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, scheduled_time DATETIME NOT NULL, duration_min INT DEFAULT 60 NOT NULL, notes TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, INDEX fk_schedule_event (event_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fighter_coach (id INT AUTO_INCREMENT NOT NULL, fighter_id INT NOT NULL, coach_id INT NOT NULL, start_date DATE NOT NULL, end_date DATE DEFAULT NULL, is_active TINYINT(1) DEFAULT 1 NOT NULL, INDEX fk_fc_coach (coach_id), UNIQUE INDEX uq_fighter_coach (fighter_id, coach_id), INDEX IDX_E5560F5034934341 (fighter_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fight_highlight (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, video_local_path VARCHAR(512) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, video_url VARCHAR(512) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, normalized_source VARCHAR(512) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, UNIQUE INDEX uq_highlight_result (fight_result_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE fight_statistic (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, fighter_id INT NOT NULL, strikes_landed INT DEFAULT 0 NOT NULL, strikes_thrown INT DEFAULT 0 NOT NULL, takedowns INT DEFAULT 0 NOT NULL, submissions INT DEFAULT 0 NOT NULL, knockdowns INT DEFAULT 0 NOT NULL, INDEX fk_fs_fighter (fighter_id), UNIQUE INDEX uq_stat (fight_result_id, fighter_id), INDEX IDX_4C348AE92F087A36 (fight_result_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE judge_score (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, judge_name VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, score_red INT DEFAULT 0 NOT NULL, score_blue INT DEFAULT 0 NOT NULL, INDEX fk_js_result (fight_result_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE matchmaking_rule (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(150) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, description TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, weight NUMERIC(4, 2) DEFAULT \'1.00\' NOT NULL, is_active TINYINT(1) DEFAULT 1 NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE performance_score (id INT AUTO_INCREMENT NOT NULL, fighter_id INT NOT NULL, score NUMERIC(6, 2) DEFAULT \'0.00\' NOT NULL, aggression NUMERIC(4, 2) DEFAULT NULL, defense NUMERIC(4, 2) DEFAULT NULL, technique NUMERIC(4, 2) DEFAULT NULL, experience NUMERIC(4, 2) DEFAULT NULL, calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX fk_ps_fighter (fighter_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE ranking (id INT AUTO_INCREMENT NOT NULL, fighter_id INT NOT NULL, discipline_id INT DEFAULT NULL, rank_position INT DEFAULT 0 NOT NULL, points NUMERIC(8, 2) DEFAULT \'0.00\' NOT NULL, season VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'2026\' NOT NULL COLLATE `utf8mb4_unicode_ci`, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX fk_rank_discipline (discipline_id), UNIQUE INDEX uq_ranking (fighter_id, discipline_id, season), INDEX IDX_80B839D034934341 (fighter_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE venue (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(150) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, address VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, city VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, country VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT \'Tunisia\' NOT NULL COLLATE `utf8mb4_unicode_ci`, capacity INT DEFAULT 0 NOT NULL, contact_email VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, contact_phone VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE weight_class (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, min_weight NUMERIC(5, 2) NOT NULL, max_weight NUMERIC(5, 2) NOT NULL, UNIQUE INDEX uq_weight_class_name (name), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE admin ADD CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE coach ADD CONSTRAINT fk_coach_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT fk_event_booking_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE event_booking ADD CONSTRAINT fk_event_booking_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE event_fighter ADD CONSTRAINT fk_ef_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE event_fighter ADD CONSTRAINT fk_ef_fighter FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE event_schedule ADD CONSTRAINT fk_schedule_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fighter_coach ADD CONSTRAINT fk_fc_coach FOREIGN KEY (coach_id) REFERENCES coach (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fighter_coach ADD CONSTRAINT fk_fc_fighter FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fight_highlight ADD CONSTRAINT fk_fh_result FOREIGN KEY (fight_result_id) REFERENCES fight_result (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT fk_fs_result FOREIGN KEY (fight_result_id) REFERENCES fight_result (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT fk_fs_fighter FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE');
        $this->addSql('ALTER TABLE judge_score ADD CONSTRAINT fk_js_result FOREIGN KEY (fight_result_id) REFERENCES fight_result (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE performance_score ADD CONSTRAINT fk_ps_fighter FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT fk_rank_discipline FOREIGN KEY (discipline_id) REFERENCES discipline (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT fk_rank_fighter FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_EECCB3E512469DE2');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_EECCB3E5F675F31B');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_EECCB3E512469DE2');
        $this->addSql('ALTER TABLE blog_article DROP FOREIGN KEY FK_EECCB3E5F675F31B');
        $this->addSql('ALTER TABLE blog_article CHANGE summary summary TEXT DEFAULT NULL, CHANGE status status VARCHAR(0) DEFAULT \'DRAFT\' NOT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT fk_blog_article_category FOREIGN KEY (category_id) REFERENCES blog_category (id) ON UPDATE CASCADE');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT fk_blog_article_author FOREIGN KEY (author_id) REFERENCES user (id) ON UPDATE CASCADE');
        $this->addSql('DROP INDEX idx_eeccb3e5f675f31b ON blog_article');
        $this->addSql('CREATE INDEX fk_blog_article_author ON blog_article (author_id)');
        $this->addSql('DROP INDEX idx_eeccb3e512469de2 ON blog_article');
        $this->addSql('CREATE INDEX fk_blog_article_category ON blog_article (category_id)');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT FK_EECCB3E512469DE2 FOREIGN KEY (category_id) REFERENCES blog_category (id)');
        $this->addSql('ALTER TABLE blog_article ADD CONSTRAINT FK_EECCB3E5F675F31B FOREIGN KEY (author_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE blog_category CHANGE description description TEXT DEFAULT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('DROP INDEX uniq_72113de65e237e06 ON blog_category');
        $this->addSql('CREATE UNIQUE INDEX uq_blog_category_name ON blog_category (name)');
        $this->addSql('DROP INDEX uniq_72113de6989d9b62 ON blog_category');
        $this->addSql('CREATE UNIQUE INDEX uq_blog_category_slug ON blog_category (slug)');
        $this->addSql('ALTER TABLE discipline CHANGE description description TEXT DEFAULT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('DROP INDEX uniq_75beee3f5e237e06 ON discipline');
        $this->addSql('CREATE UNIQUE INDEX uq_discipline_name ON discipline (name)');
        $this->addSql('ALTER TABLE event DROP FOREIGN KEY FK_3BAE0AA7A5522701');
        $this->addSql('ALTER TABLE event DROP FOREIGN KEY FK_3BAE0AA7A5522701');
        $this->addSql('ALTER TABLE event CHANGE description description TEXT DEFAULT NULL, CHANGE status status VARCHAR(0) DEFAULT \'SCHEDULED\' NOT NULL, CHANGE visibility visibility VARCHAR(0) DEFAULT \'PUBLIC\' NOT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE event ADD CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE event ADD CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venue (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE event ADD CONSTRAINT fk_event_discipline FOREIGN KEY (discipline_id) REFERENCES discipline (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('CREATE INDEX fk_event_organizer ON event (organizer_id)');
        $this->addSql('CREATE INDEX fk_event_venue ON event (venue_id)');
        $this->addSql('DROP INDEX idx_3bae0aa7a5522701 ON event');
        $this->addSql('CREATE INDEX fk_event_discipline ON event (discipline_id)');
        $this->addSql('ALTER TABLE event ADD CONSTRAINT FK_3BAE0AA7A5522701 FOREIGN KEY (discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE fan_notification DROP FOREIGN KEY FK_D093B97589C48F0B');
        $this->addSql('ALTER TABLE fan_notification DROP FOREIGN KEY FK_D093B975D774A626');
        $this->addSql('ALTER TABLE fan_notification DROP FOREIGN KEY FK_D093B975D774A626');
        $this->addSql('ALTER TABLE fan_notification CHANGE type type VARCHAR(0) NOT NULL, CHANGE message message TEXT NOT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE fan_notification ADD CONSTRAINT fk_fn_event FOREIGN KEY (related_event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE fan_notification ADD CONSTRAINT fk_fn_fan FOREIGN KEY (fan_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('CREATE INDEX idx_fn_fan_read ON fan_notification (fan_id, is_read)');
        $this->addSql('CREATE INDEX idx_fn_type ON fan_notification (type)');
        $this->addSql('DROP INDEX idx_d093b975d774a626 ON fan_notification');
        $this->addSql('CREATE INDEX idx_fn_event ON fan_notification (related_event_id)');
        $this->addSql('ALTER TABLE fan_notification ADD CONSTRAINT FK_D093B975D774A626 FOREIGN KEY (related_event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE fan_prediction DROP FOREIGN KEY FK_89572FC9C6E05170');
        $this->addSql('ALTER TABLE fan_prediction DROP FOREIGN KEY FK_89572FC989C48F0B');
        $this->addSql('ALTER TABLE fan_prediction DROP FOREIGN KEY FK_89572FC919EAE00D');
        $this->addSql('ALTER TABLE fan_prediction DROP FOREIGN KEY FK_89572FC989C48F0B');
        $this->addSql('ALTER TABLE fan_prediction DROP FOREIGN KEY FK_89572FC919EAE00D');
        $this->addSql('ALTER TABLE fan_prediction CHANGE predicted_method predicted_method VARCHAR(0) NOT NULL, CHANGE submitted_at submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE season season VARCHAR(10) DEFAULT \'2026\' NOT NULL');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT fk_fp_fan FOREIGN KEY (fan_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT fk_fp_match FOREIGN KEY (match_proposal_id) REFERENCES match_proposal (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT fk_fp_winner FOREIGN KEY (predicted_winner_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('CREATE INDEX idx_fp_scored ON fan_prediction (is_scored)');
        $this->addSql('CREATE INDEX idx_fp_season ON fan_prediction (season)');
        $this->addSql('DROP INDEX idx_89572fc919eae00d ON fan_prediction');
        $this->addSql('CREATE INDEX fk_fp_winner ON fan_prediction (predicted_winner_id)');
        $this->addSql('DROP INDEX idx_89572fc989c48f0b ON fan_prediction');
        $this->addSql('CREATE INDEX idx_fp_fan ON fan_prediction (fan_id)');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT FK_89572FC989C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_prediction ADD CONSTRAINT FK_89572FC919EAE00D FOREIGN KEY (predicted_winner_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F05289C48F0B');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F05278B04DCA');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F0522A58A1BC');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F05278B04DCA');
        $this->addSql('ALTER TABLE fan_preference DROP FOREIGN KEY FK_E207F0522A58A1BC');
        $this->addSql('ALTER TABLE fan_preference CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT fk_fpref_discipline FOREIGN KEY (favorite_discipline_id) REFERENCES discipline (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT fk_fpref_fan FOREIGN KEY (fan_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT fk_fpref_fighter FOREIGN KEY (favorite_fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('DROP INDEX idx_e207f05278b04dca ON fan_preference');
        $this->addSql('CREATE INDEX idx_fpref_discipline ON fan_preference (favorite_discipline_id)');
        $this->addSql('DROP INDEX idx_e207f0522a58a1bc ON fan_preference');
        $this->addSql('CREATE INDEX idx_fpref_fighter ON fan_preference (favorite_fighter_id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F05278B04DCA FOREIGN KEY (favorite_discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE fan_preference ADD CONSTRAINT FK_E207F0522A58A1BC FOREIGN KEY (favorite_fighter_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fan_profile DROP FOREIGN KEY FK_E95F4CF4A76ED395');
        $this->addSql('ALTER TABLE fan_profile DROP FOREIGN KEY FK_E95F4CF4A76ED395');
        $this->addSql('ALTER TABLE fan_profile CHANGE bio bio TEXT DEFAULT NULL');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT fk_fan_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('DROP INDEX uniq_e95f4cf4a76ed395 ON fan_profile');
        $this->addSql('CREATE UNIQUE INDEX uq_fan_user ON fan_profile (user_id)');
        $this->addSql('ALTER TABLE fan_profile ADD CONSTRAINT FK_E95F4CF4A76ED395 FOREIGN KEY (user_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED024852F087A36');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED0248589C48F0B');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED024852F087A36');
        $this->addSql('ALTER TABLE fan_reaction DROP FOREIGN KEY FK_8ED0248589C48F0B');
        $this->addSql('ALTER TABLE fan_reaction CHANGE reaction_type reaction_type VARCHAR(0) NOT NULL, CHANGE reacted_at reacted_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT fk_frx_fan FOREIGN KEY (fan_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT fk_frx_result FOREIGN KEY (fight_result_id) REFERENCES fight_result (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('CREATE INDEX idx_fr_pinned ON fan_reaction (is_pinned)');
        $this->addSql('CREATE INDEX idx_fr_deleted ON fan_reaction (is_deleted)');
        $this->addSql('CREATE UNIQUE INDEX uq_fan_reaction ON fan_reaction (fight_result_id, fan_id)');
        $this->addSql('DROP INDEX idx_8ed024852f087a36 ON fan_reaction');
        $this->addSql('CREATE INDEX idx_fr_fight ON fan_reaction (fight_result_id)');
        $this->addSql('DROP INDEX idx_8ed0248589c48f0b ON fan_reaction');
        $this->addSql('CREATE INDEX idx_fr_fan ON fan_reaction (fan_id)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED024852F087A36 FOREIGN KEY (fight_result_id) REFERENCES fight_result (id)');
        $this->addSql('ALTER TABLE fan_reaction ADD CONSTRAINT FK_8ED0248589C48F0B FOREIGN KEY (fan_id) REFERENCES user (id)');
        $this->addSql('ALTER TABLE fighter DROP INDEX IDX_7A08C3FCA76ED395, ADD UNIQUE INDEX uq_fighter_user (user_id)');
        $this->addSql('ALTER TABLE fighter DROP FOREIGN KEY FK_7A08C3FCA76ED395');
        $this->addSql('ALTER TABLE fighter CHANGE status status VARCHAR(0) DEFAULT \'ACTIVE\' NOT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE fighter ADD CONSTRAINT fk_fighter_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fighter ADD CONSTRAINT fk_fighter_weight FOREIGN KEY (weight_class_id) REFERENCES weight_class (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('CREATE INDEX fk_fighter_weight ON fighter (weight_class_id)');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B71F7E88B');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B2ABEACD6');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B72DD592F');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B24F12652');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B5DFCD4B8');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B71F7E88B');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B2ABEACD6');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B72DD592F');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B24F12652');
        $this->addSql('ALTER TABLE fight_result DROP FOREIGN KEY FK_1C8CFD0B5DFCD4B8');
        $this->addSql('ALTER TABLE fight_result CHANGE method method VARCHAR(0) NOT NULL, CHANGE notes notes TEXT DEFAULT NULL, CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT fk_fr_blue FOREIGN KEY (fighter_blue_id) REFERENCES fighter (id) ON UPDATE CASCADE');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT fk_fr_red FOREIGN KEY (fighter_red_id) REFERENCES fighter (id) ON UPDATE CASCADE');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT fk_fr_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT fk_fr_winner FOREIGN KEY (winner_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT fk_fr_match FOREIGN KEY (match_id) REFERENCES match_proposal (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('DROP INDEX idx_1c8cfd0b5dfcd4b8 ON fight_result');
        $this->addSql('CREATE INDEX fk_fr_winner ON fight_result (winner_id)');
        $this->addSql('DROP INDEX idx_1c8cfd0b2abeacd6 ON fight_result');
        $this->addSql('CREATE INDEX fk_fr_match ON fight_result (match_id)');
        $this->addSql('DROP INDEX idx_1c8cfd0b72dd592f ON fight_result');
        $this->addSql('CREATE INDEX fk_fr_red ON fight_result (fighter_red_id)');
        $this->addSql('DROP INDEX idx_1c8cfd0b24f12652 ON fight_result');
        $this->addSql('CREATE INDEX fk_fr_blue ON fight_result (fighter_blue_id)');
        $this->addSql('DROP INDEX idx_1c8cfd0b71f7e88b ON fight_result');
        $this->addSql('CREATE INDEX fk_fr_event ON fight_result (event_id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B71F7E88B FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B2ABEACD6 FOREIGN KEY (match_id) REFERENCES match_proposal (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B72DD592F FOREIGN KEY (fighter_red_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B24F12652 FOREIGN KEY (fighter_blue_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE fight_result ADD CONSTRAINT FK_1C8CFD0B5DFCD4B8 FOREIGN KEY (winner_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE471F7E88B');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4D783CFD6');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4C5366038');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE471F7E88B');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4D783CFD6');
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_629F4EE4C5366038');
        $this->addSql('ALTER TABLE match_proposal CHANGE status status VARCHAR(0) DEFAULT \'PENDING\' NOT NULL, CHANGE proposed_at proposed_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE notes notes TEXT DEFAULT NULL');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT fk_mp_fighter1 FOREIGN KEY (fighter1_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT fk_mp_fighter2 FOREIGN KEY (fighter2_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT fk_mp_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('DROP INDEX idx_629f4ee4d783cfd6 ON match_proposal');
        $this->addSql('CREATE INDEX fk_mp_fighter1 ON match_proposal (fighter1_id)');
        $this->addSql('DROP INDEX idx_629f4ee4c5366038 ON match_proposal');
        $this->addSql('CREATE INDEX fk_mp_fighter2 ON match_proposal (fighter2_id)');
        $this->addSql('DROP INDEX idx_629f4ee471f7e88b ON match_proposal');
        $this->addSql('CREATE INDEX fk_mp_event ON match_proposal (event_id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE471F7E88B FOREIGN KEY (event_id) REFERENCES event (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4D783CFD6 FOREIGN KEY (fighter1_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE match_proposal ADD CONSTRAINT FK_629F4EE4C5366038 FOREIGN KEY (fighter2_id) REFERENCES fighter (id)');
        $this->addSql('ALTER TABLE user DROP FOREIGN KEY FK_8D93D649D60322AC');
        $this->addSql('ALTER TABLE user DROP FOREIGN KEY FK_8D93D649D60322AC');
        $this->addSql('ALTER TABLE user CHANGE created_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE updated_at updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL');
        $this->addSql('ALTER TABLE user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES user_role (id) ON UPDATE CASCADE');
        $this->addSql('DROP INDEX uniq_8d93d649e7927c74 ON user');
        $this->addSql('CREATE UNIQUE INDEX uq_user_email ON user (email)');
        $this->addSql('DROP INDEX idx_8d93d649d60322ac ON user');
        $this->addSql('CREATE INDEX fk_user_role ON user (role_id)');
        $this->addSql('ALTER TABLE user ADD CONSTRAINT FK_8D93D649D60322AC FOREIGN KEY (role_id) REFERENCES user_role (id)');
        $this->addSql('CREATE UNIQUE INDEX uq_role_name ON user_role (name)');
    }
}
