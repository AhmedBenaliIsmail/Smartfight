<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260424000000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Phase 1: Boxing Migration (WeightDivision, Fighter, Event, FightResult, FightStatistic, Ranking)';
    }

    public function up(Schema $schema): void
    {
        // weight_division
        $this->addSql('CREATE TABLE weight_division (id INT AUTO_INCREMENT NOT NULL, name VARCHAR(100) NOT NULL, max_weight_lbs INT NOT NULL, slug VARCHAR(100) NOT NULL, UNIQUE INDEX UNIQ_D9C2E351989D9B62 (slug), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        
        // fighters
        $this->addSql('ALTER TABLE fighters ADD weight_division_id INT DEFAULT NULL, ADD nationality VARCHAR(2) DEFAULT NULL, ADD photo_filename VARCHAR(255) DEFAULT NULL, ADD technical_wins INT DEFAULT 0 NOT NULL, ADD last_fight_date DATE DEFAULT NULL');
        $this->addSql('ALTER TABLE fighters DROP weightClass, DROP country, DROP submissionWins, DROP championsEventWinStreak');
        $this->addSql('ALTER TABLE fighters ADD CONSTRAINT FK_8368F8C22CA1E359 FOREIGN KEY (weight_division_id) REFERENCES weight_division (id)');
        $this->addSql('CREATE INDEX IDX_8368F8C22CA1E359 ON fighters (weight_division_id)');

        // events
        $this->addSql('ALTER TABLE events ADD organization VARCHAR(20) DEFAULT \'INDEPENDENT\' NOT NULL, ADD venue VARCHAR(200) DEFAULT NULL, ADD city VARCHAR(100) DEFAULT NULL, ADD country VARCHAR(2) DEFAULT NULL, ADD seat_capacity INT DEFAULT NULL, ADD poster_filename VARCHAR(255) DEFAULT NULL, ADD status VARCHAR(20) DEFAULT \'SCHEDULED\' NOT NULL');
        $this->addSql('ALTER TABLE events DROP location, DROP isChampionsEvent');

        // fight_results
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY fight_results_ibfk_1'); // eventId
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY fight_results_ibfk_2'); // fighter1Id
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY fight_results_ibfk_3'); // fighter2Id
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY fight_results_ibfk_4'); // winnerId
        
        $this->addSql('ALTER TABLE fight_results CHANGE eventId eventId INT NOT NULL, CHANGE fighter1Id fighter1Id INT NOT NULL, CHANGE fighter2Id fighter2Id INT NOT NULL, CHANGE winnerId winnerId INT DEFAULT NULL');
        $this->addSql('ALTER TABLE fight_results ADD decision_type VARCHAR(10) DEFAULT NULL, ADD knockdown_round INT DEFAULT NULL, ADD scheduled_rounds INT DEFAULT 12 NOT NULL, ADD is_belt_fight TINYINT(1) DEFAULT 0 NOT NULL, ADD belt_organization VARCHAR(20) DEFAULT NULL, ADD fighter1_odds DOUBLE PRECISION DEFAULT NULL, ADD fighter2_odds DOUBLE PRECISION DEFAULT NULL');
        
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT FK_77583D93922726E9 FOREIGN KEY (eventId) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT FK_77583D93A8BCC9CA FOREIGN KEY (fighter1Id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT FK_77583D93E44B1578 FOREIGN KEY (fighter2Id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT FK_77583D93EBDD3302 FOREIGN KEY (winnerId) REFERENCES fighters (fighterId)');
        $this->addSql('CREATE INDEX IDX_77583D93922726E9 ON fight_results (eventId)');
        $this->addSql('CREATE INDEX IDX_77583D93A8BCC9CA ON fight_results (fighter1Id)');
        $this->addSql('CREATE INDEX IDX_77583D93E44B1578 ON fight_results (fighter2Id)');
        $this->addSql('CREATE INDEX IDX_77583D93EBDD3302 ON fight_results (winnerId)');

        // fight_statistic
        $this->addSql('ALTER TABLE fight_statistic ADD round INT DEFAULT NULL, ADD body_punches_landed INT DEFAULT 0 NOT NULL, ADD jabs_landed INT DEFAULT 0 NOT NULL, ADD jabs_thrown INT DEFAULT 0 NOT NULL, ADD body_jabs_landed INT DEFAULT 0 NOT NULL, ADD power_punches_landed INT DEFAULT 0 NOT NULL, ADD power_punches_thrown INT DEFAULT 0 NOT NULL, ADD body_power_punches_landed INT DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE fight_statistic DROP takedowns, DROP takedownAttempts, DROP submissions, DROP controlTimeSeconds');
        $this->addSql('ALTER TABLE fight_statistic CHANGE strikes_landed punches_landed INT DEFAULT 0 NOT NULL, CHANGE strikes_thrown punches_thrown INT DEFAULT 0 NOT NULL');

        // ranking
        $this->addSql('ALTER TABLE ranking ADD weight_division_id INT DEFAULT NULL, ADD organization VARCHAR(20) DEFAULT \'MEDIA\' NOT NULL, ADD is_champion TINYINT(1) DEFAULT 0 NOT NULL, ADD last_fight_date DATE DEFAULT NULL');
        $this->addSql('ALTER TABLE ranking DROP weightClass, DROP season');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_80B839D02CA1E359 FOREIGN KEY (weight_division_id) REFERENCES weight_division (id)');
        $this->addSql('CREATE INDEX IDX_80B839D02CA1E359 ON ranking (weight_division_id)');
    }

    public function down(Schema $schema): void
    {
        // Revert weight_division
        $this->addSql('ALTER TABLE fighters DROP FOREIGN KEY FK_8368F8C22CA1E359');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_80B839D02CA1E359');
        $this->addSql('DROP TABLE weight_division');

        // Revert fighters
        $this->addSql('DROP INDEX IDX_8368F8C22CA1E359 ON fighters');
        $this->addSql('ALTER TABLE fighters ADD weightClass VARCHAR(50) DEFAULT NULL, ADD country VARCHAR(50) DEFAULT NULL, ADD submissionWins INT DEFAULT 0 NOT NULL, ADD championsEventWinStreak INT DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE fighters DROP weight_division_id, DROP nationality, DROP photo_filename, DROP technical_wins, DROP last_fight_date');

        // Revert events
        $this->addSql('ALTER TABLE events ADD location VARCHAR(200) DEFAULT NULL, ADD isChampionsEvent TINYINT(1) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE events DROP organization, DROP venue, DROP city, DROP country, DROP seat_capacity, DROP poster_filename, DROP status');

        // Revert fight_results
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY FK_77583D93922726E9');
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY FK_77583D93A8BCC9CA');
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY FK_77583D93E44B1578');
        $this->addSql('ALTER TABLE fight_results DROP FOREIGN KEY FK_77583D93EBDD3302');
        $this->addSql('DROP INDEX IDX_77583D93922726E9 ON fight_results');
        $this->addSql('DROP INDEX IDX_77583D93A8BCC9CA ON fight_results');
        $this->addSql('DROP INDEX IDX_77583D93E44B1578 ON fight_results');
        $this->addSql('DROP INDEX IDX_77583D93EBDD3302 ON fight_results');
        $this->addSql('ALTER TABLE fight_results DROP decision_type, DROP knockdown_round, DROP scheduled_rounds, DROP is_belt_fight, DROP belt_organization, DROP fighter1_odds, DROP fighter2_odds');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT fight_results_ibfk_1 FOREIGN KEY (eventId) REFERENCES events (eventId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT fight_results_ibfk_2 FOREIGN KEY (fighter1Id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT fight_results_ibfk_3 FOREIGN KEY (fighter2Id) REFERENCES fighters (fighterId)');
        $this->addSql('ALTER TABLE fight_results ADD CONSTRAINT fight_results_ibfk_4 FOREIGN KEY (winnerId) REFERENCES fighters (fighterId)');

        // Revert fight_statistic
        $this->addSql('ALTER TABLE fight_statistic ADD takedowns INT DEFAULT 0 NOT NULL, ADD takedownAttempts INT DEFAULT 0 NOT NULL, ADD submissions INT DEFAULT 0 NOT NULL, ADD controlTimeSeconds INT DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE fight_statistic DROP round, DROP body_punches_landed, DROP jabs_landed, DROP jabs_thrown, DROP body_jabs_landed, DROP power_punches_landed, DROP power_punches_thrown, DROP body_power_punches_landed');
        $this->addSql('ALTER TABLE fight_statistic CHANGE punches_landed strikes_landed INT DEFAULT 0 NOT NULL, CHANGE punches_thrown strikes_thrown INT DEFAULT 0 NOT NULL');

        // Revert ranking
        $this->addSql('DROP INDEX IDX_80B839D02CA1E359 ON ranking');
        $this->addSql('ALTER TABLE ranking ADD weightClass VARCHAR(50) DEFAULT NULL, ADD season VARCHAR(20) DEFAULT NULL');
        $this->addSql('ALTER TABLE ranking DROP weight_division_id, DROP organization, DROP is_champion, DROP last_fight_date');
    }
}
