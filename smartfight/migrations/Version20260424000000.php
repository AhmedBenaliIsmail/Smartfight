<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260424000000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Phase 1: Boxing migration — upgrade weight_class to 17 boxing divisions, add SystemMeta, boxing stats, updated fight methods, event venue fields, match boxing fields';
    }

    public function up(Schema $schema): void
    {
        // ── 1. Upgrade weight_class ───────────────────────────────────────────
        // Drop the existing FK so we can safely clear the old MMA rows
        $this->addSql('ALTER TABLE fighter DROP FOREIGN KEY fk_fighter_weight');
        $this->addSql('UPDATE fighter SET weight_class_id = NULL');
        $this->addSql('DELETE FROM weight_class');
        $this->addSql('ALTER TABLE weight_class AUTO_INCREMENT = 1');

        // Widen existing columns to nullable, add new columns
        $this->addSql('ALTER TABLE weight_class
            MODIFY COLUMN min_weight DECIMAL(5,2) DEFAULT NULL,
            MODIFY COLUMN max_weight DECIMAL(5,2) DEFAULT NULL,
            ADD COLUMN discipline_id INT          NOT NULL DEFAULT 0   AFTER max_weight,
            ADD COLUMN champion_id   INT          DEFAULT NULL         AFTER discipline_id,
            ADD COLUMN displayOrder  INT          NOT NULL DEFAULT 0   AFTER champion_id,
            ADD COLUMN slug          VARCHAR(100) NOT NULL DEFAULT \'\'  AFTER displayOrder');

        // ── 2. Seed 17 boxing weight classes ─────────────────────────────────
        $this->addSql("
            INSERT INTO weight_class (name, min_weight, max_weight, discipline_id, champion_id, displayOrder, slug)
            SELECT v.name, v.min_w, v.max_w, d.id, NULL, v.ord, v.slug
            FROM discipline d
            JOIN (
                SELECT 'Heavyweight'          AS name,  91.00 AS min_w, 120.00 AS max_w,  1 AS ord, 'heavyweight'          AS slug UNION ALL
                SELECT 'Cruiserweight',               86.00,  90.72,  2, 'cruiserweight'         UNION ALL
                SELECT 'Light Heavyweight',           76.00,  79.38,  3, 'light-heavyweight'     UNION ALL
                SELECT 'Super Middleweight',          73.00,  76.20,  4, 'super-middleweight'    UNION ALL
                SELECT 'Middleweight',                69.00,  72.57,  5, 'middleweight'          UNION ALL
                SELECT 'Super Welterweight',          66.00,  69.85,  6, 'super-welterweight'    UNION ALL
                SELECT 'Welterweight',                63.00,  66.68,  7, 'welterweight'          UNION ALL
                SELECT 'Super Lightweight',           61.00,  63.50,  8, 'super-lightweight'     UNION ALL
                SELECT 'Lightweight',                 58.00,  61.23,  9, 'lightweight'           UNION ALL
                SELECT 'Super Featherweight',         56.00,  58.97, 10, 'super-featherweight'   UNION ALL
                SELECT 'Featherweight',               54.00,  57.15, 11, 'featherweight'         UNION ALL
                SELECT 'Super Bantamweight',          52.00,  55.34, 12, 'super-bantamweight'    UNION ALL
                SELECT 'Bantamweight',                50.00,  53.52, 13, 'bantamweight'          UNION ALL
                SELECT 'Super Flyweight',             49.00,  52.16, 14, 'super-flyweight'       UNION ALL
                SELECT 'Flyweight',                   48.00,  50.80, 15, 'flyweight'             UNION ALL
                SELECT 'Light Flyweight',             46.00,  48.99, 16, 'light-flyweight'       UNION ALL
                SELECT 'Minimumweight',                0.00,  47.63, 17, 'minimumweight'
            ) v ON d.name = 'Boxing'
        ");

        // Remove placeholder DEFAULT from discipline_id now that rows are seeded
        $this->addSql('ALTER TABLE weight_class MODIFY COLUMN discipline_id INT NOT NULL');

        // Add unique slug index and FK constraints
        $this->addSql('ALTER TABLE weight_class ADD UNIQUE INDEX UNIQ_WC_SLUG (slug)');
        $this->addSql('ALTER TABLE weight_class
            ADD CONSTRAINT FK_WC_DISCIPLINE FOREIGN KEY (discipline_id) REFERENCES discipline (id),
            ADD CONSTRAINT FK_WC_CHAMPION   FOREIGN KEY (champion_id)   REFERENCES fighter (id) ON DELETE SET NULL');

        // Re-add fighter → weight_class FK (dropped at start of step 1)
        $this->addSql('ALTER TABLE fighter
            ADD CONSTRAINT fk_fighter_weight FOREIGN KEY (weight_class_id) REFERENCES weight_class (id) ON DELETE SET NULL');

        // ── 3. system_meta ───────────────────────────────────────────────────
        $this->addSql("CREATE TABLE system_meta (
            id        INT AUTO_INCREMENT NOT NULL,
            meta_key  VARCHAR(100)       NOT NULL,
            value     VARCHAR(255)       NOT NULL,
            updatedAt DATETIME           NOT NULL,
            UNIQUE INDEX UNIQ_SYSTEM_META_KEY (meta_key),
            PRIMARY KEY(id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB");

        $this->addSql("INSERT INTO system_meta (meta_key, value, updatedAt) VALUES ('last_status_refresh', '1970-01-01 00:00:00', NOW())");

        // ── 4. fighter — new columns ──────────────────────────────────────────
        $this->addSql('ALTER TABLE fighter
            ADD COLUMN first_name    VARCHAR(100) DEFAULT NULL,
            ADD COLUMN last_name     VARCHAR(100) DEFAULT NULL,
            ADD COLUMN country_code  CHAR(2)      DEFAULT NULL,
            ADD COLUMN discipline_id INT          DEFAULT NULL');

        $this->addSql('ALTER TABLE fighter
            ADD CONSTRAINT FK_FIGHTER_DISCIPLINE FOREIGN KEY (discipline_id) REFERENCES discipline (id)');

        $this->addSql("UPDATE fighter SET discipline_id = (SELECT id FROM discipline WHERE name = 'Boxing' LIMIT 1)");
        $this->addSql('ALTER TABLE fighter MODIFY COLUMN discipline_id INT NOT NULL');

        // Make user_id nullable to support social-login and standalone fighter profiles
        $this->addSql('ALTER TABLE fighter MODIFY COLUMN user_id INT DEFAULT NULL');

        // ── 5. event — venue and datetime columns ─────────────────────────────
        $this->addSql('ALTER TABLE event
            ADD COLUMN starts_at  DATETIME     DEFAULT NULL,
            ADD COLUMN ends_at    DATETIME     DEFAULT NULL,
            ADD COLUMN venue_name VARCHAR(150) DEFAULT NULL,
            ADD COLUMN city       VARCHAR(100) DEFAULT NULL,
            ADD COLUMN country    CHAR(2)      DEFAULT NULL,
            ADD COLUMN poster_url VARCHAR(255) DEFAULT NULL');

        $this->addSql("UPDATE event SET starts_at = CONCAT(start_date, ' 00:00:00'), ends_at = CONCAT(end_date, ' 23:59:59') WHERE start_date IS NOT NULL");

        // ── 6. match_proposal — boxing match fields ───────────────────────────
        $this->addSql('ALTER TABLE match_proposal
            ADD COLUMN scheduled_rounds INT          DEFAULT NULL,
            ADD COLUMN is_title_fight   TINYINT(1)   NOT NULL DEFAULT 0,
            ADD COLUMN weight_class_id  INT          DEFAULT NULL,
            ADD COLUMN odds_fighter1    DECIMAL(5,2) DEFAULT NULL,
            ADD COLUMN odds_fighter2    DECIMAL(5,2) DEFAULT NULL,
            ADD COLUMN card_position    INT          DEFAULT NULL,
            ADD COLUMN card_type        VARCHAR(20)  DEFAULT NULL');

        $this->addSql('ALTER TABLE match_proposal
            ADD CONSTRAINT FK_MP_WEIGHT_CLASS FOREIGN KEY (weight_class_id) REFERENCES weight_class (id)');

        $this->addSql("UPDATE match_proposal SET status = 'SCHEDULED' WHERE status = 'PENDING'");
        $this->addSql("UPDATE match_proposal SET status = 'COMPLETED' WHERE status = 'ACCEPTED'");
        $this->addSql("UPDATE match_proposal SET status = 'CANCELLED' WHERE status = 'REJECTED'");

        // ── 7. fight_result — boxing method values + knockdown counters ───────
        $this->addSql("UPDATE fight_result SET method = 'UD'  WHERE method = 'DECISION'");
        $this->addSql("UPDATE fight_result SET method = 'TKO' WHERE method = 'SUBMISSION'");

        $this->addSql('ALTER TABLE fight_result
            ADD COLUMN knockdowns_fighter_red  INT NOT NULL DEFAULT 0,
            ADD COLUMN knockdowns_fighter_blue INT NOT NULL DEFAULT 0');

        // ── 8. fight_statistic — boxing punch stats ───────────────────────────
        $this->addSql('ALTER TABLE fight_statistic
            ADD COLUMN punches_thrown INT NOT NULL DEFAULT 0,
            ADD COLUMN punches_landed INT NOT NULL DEFAULT 0,
            ADD COLUMN round_number   INT DEFAULT NULL');

        // ── 9. ranking — add weight_class FK ─────────────────────────────────
        $this->addSql('ALTER TABLE ranking
            ADD COLUMN weight_class_id INT DEFAULT NULL,
            ADD CONSTRAINT FK_RANKING_WC FOREIGN KEY (weight_class_id) REFERENCES weight_class (id)');
    }

    public function down(Schema $schema): void
    {
        // ── 9. ranking ────────────────────────────────────────────────────────
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_RANKING_WC');
        $this->addSql('ALTER TABLE ranking DROP COLUMN weight_class_id');

        // ── 8. fight_statistic ────────────────────────────────────────────────
        $this->addSql('ALTER TABLE fight_statistic DROP COLUMN punches_thrown, DROP COLUMN punches_landed, DROP COLUMN round_number');

        // ── 7. fight_result ───────────────────────────────────────────────────
        $this->addSql('ALTER TABLE fight_result DROP COLUMN knockdowns_fighter_red, DROP COLUMN knockdowns_fighter_blue');
        $this->addSql("UPDATE fight_result SET method = 'DECISION'   WHERE method = 'UD'");
        $this->addSql("UPDATE fight_result SET method = 'SUBMISSION' WHERE method = 'TKO'");

        // ── 6. match_proposal ─────────────────────────────────────────────────
        $this->addSql('ALTER TABLE match_proposal DROP FOREIGN KEY FK_MP_WEIGHT_CLASS');
        $this->addSql('ALTER TABLE match_proposal DROP COLUMN scheduled_rounds, DROP COLUMN is_title_fight, DROP COLUMN weight_class_id, DROP COLUMN odds_fighter1, DROP COLUMN odds_fighter2, DROP COLUMN card_position, DROP COLUMN card_type');
        $this->addSql("UPDATE match_proposal SET status = 'PENDING'  WHERE status = 'SCHEDULED'");
        $this->addSql("UPDATE match_proposal SET status = 'ACCEPTED' WHERE status = 'COMPLETED'");
        $this->addSql("UPDATE match_proposal SET status = 'REJECTED' WHERE status = 'CANCELLED'");

        // ── 5. event ──────────────────────────────────────────────────────────
        $this->addSql('ALTER TABLE event DROP COLUMN starts_at, DROP COLUMN ends_at, DROP COLUMN venue_name, DROP COLUMN city, DROP COLUMN country, DROP COLUMN poster_url');

        // ── 4. fighter ────────────────────────────────────────────────────────
        $this->addSql('ALTER TABLE fighter DROP FOREIGN KEY FK_FIGHTER_DISCIPLINE');
        $this->addSql('ALTER TABLE fighter MODIFY COLUMN user_id INT NOT NULL');
        $this->addSql('ALTER TABLE fighter DROP COLUMN first_name, DROP COLUMN last_name, DROP COLUMN country_code, DROP COLUMN discipline_id');

        // ── 3. system_meta ────────────────────────────────────────────────────
        $this->addSql('DROP TABLE system_meta');

        // ── 2 + 1. revert weight_class ────────────────────────────────────────
        $this->addSql('ALTER TABLE fighter DROP FOREIGN KEY fk_fighter_weight');
        $this->addSql('UPDATE fighter SET weight_class_id = NULL');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_WC_DISCIPLINE');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_WC_CHAMPION');
        $this->addSql('ALTER TABLE weight_class DROP INDEX UNIQ_WC_SLUG');
        $this->addSql('DELETE FROM weight_class');
        $this->addSql('ALTER TABLE weight_class AUTO_INCREMENT = 1');
        $this->addSql('ALTER TABLE weight_class
            DROP COLUMN discipline_id,
            DROP COLUMN champion_id,
            DROP COLUMN displayOrder,
            DROP COLUMN slug,
            MODIFY COLUMN min_weight DECIMAL(5,2) NOT NULL,
            MODIFY COLUMN max_weight DECIMAL(5,2) NOT NULL');
        $this->addSql('ALTER TABLE fighter
            ADD CONSTRAINT fk_fighter_weight FOREIGN KEY (weight_class_id) REFERENCES weight_class (id)');
    }
}
