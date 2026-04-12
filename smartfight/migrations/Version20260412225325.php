<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260412225325 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add ELO analytics columns and tables (fight_statistic, performance_score, ranking) in additive-only mode';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE event ADD is_champions_event TINYINT(1) DEFAULT 0 NOT NULL, ADD location VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE fight_result ADD fight_number INT DEFAULT 1 NOT NULL, ADD status VARCHAR(20) DEFAULT \'COMPLETED\' NOT NULL');
        $this->addSql('ALTER TABLE fighter ADD elo_rating DOUBLE PRECISION DEFAULT 1500 NOT NULL, ADD performance_score DOUBLE PRECISION DEFAULT 0 NOT NULL, ADD win_streak INT DEFAULT 0 NOT NULL, ADD strength_of_schedule DOUBLE PRECISION DEFAULT 1500 NOT NULL, ADD ko_wins INT DEFAULT 0 NOT NULL, ADD submission_wins INT DEFAULT 0 NOT NULL, ADD decision_wins INT DEFAULT 0 NOT NULL, ADD champions_event_win_streak INT DEFAULT 0 NOT NULL, ADD title_defenses INT DEFAULT 0 NOT NULL, ADD height INT DEFAULT NULL, ADD reach INT DEFAULT NULL, ADD weight_class VARCHAR(50) DEFAULT NULL');

        $this->addSql('CREATE TABLE fight_statistic (id INT AUTO_INCREMENT NOT NULL, fight_result_id INT NOT NULL, fighter_id INT NOT NULL, strikes_landed INT DEFAULT 0 NOT NULL, strikes_attempted INT DEFAULT 0 NOT NULL, takedowns_landed INT DEFAULT 0 NOT NULL, takedowns_attempted INT DEFAULT 0 NOT NULL, submission_attempts INT DEFAULT 0 NOT NULL, knockdowns INT DEFAULT 0 NOT NULL, control_time_seconds INT DEFAULT 0 NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, INDEX IDX_FIGHT_STAT_RESULT (fight_result_id), INDEX IDX_FIGHT_STAT_FIGHTER (fighter_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT FK_FIGHT_STAT_RESULT FOREIGN KEY (fight_result_id) REFERENCES fight_result (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE fight_statistic ADD CONSTRAINT FK_FIGHT_STAT_FIGHTER FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');

        $this->addSql('CREATE TABLE performance_score (id INT AUTO_INCREMENT NOT NULL, fighter_id INT NOT NULL, score DOUBLE PRECISION DEFAULT 0 NOT NULL, aggression DOUBLE PRECISION DEFAULT 0 NOT NULL, defense DOUBLE PRECISION DEFAULT 0 NOT NULL, technique DOUBLE PRECISION DEFAULT 0 NOT NULL, experience DOUBLE PRECISION DEFAULT 0 NOT NULL, season VARCHAR(20) NOT NULL, computed_at DATETIME NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, INDEX IDX_PERF_SCORE_FIGHTER (fighter_id), UNIQUE INDEX UQ_PERF_SCORE_FIGHTER_SEASON (fighter_id, season), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE performance_score ADD CONSTRAINT FK_PERF_SCORE_FIGHTER FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');

        $this->addSql('CREATE TABLE ranking (id INT AUTO_INCREMENT NOT NULL, fighter_id INT NOT NULL, weight_class VARCHAR(50) NOT NULL, rank_position INT NOT NULL, points DOUBLE PRECISION DEFAULT 0 NOT NULL, season VARCHAR(20) NOT NULL, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, INDEX IDX_RANKING_FIGHTER (fighter_id), INDEX IDX_RANKING_WEIGHT_SEASON (weight_class, season), UNIQUE INDEX UQ_RANKING_WEIGHT_SEASON_POS (weight_class, season, rank_position), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_RANKING_FIGHTER FOREIGN KEY (fighter_id) REFERENCES fighter (id) ON UPDATE CASCADE ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE event DROP is_champions_event, DROP location');

        $this->addSql('ALTER TABLE fighter DROP elo_rating, DROP performance_score, DROP win_streak, DROP strength_of_schedule, DROP ko_wins, DROP submission_wins, DROP decision_wins, DROP champions_event_win_streak, DROP title_defenses, DROP height, DROP reach, DROP weight_class');

        $this->addSql('ALTER TABLE fight_result DROP fight_number, DROP status');

        $this->addSql('DROP TABLE ranking');
        $this->addSql('DROP TABLE performance_score');
        $this->addSql('DROP TABLE fight_statistic');
    }
}
