<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260424213335 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE fighter CHANGE elo_rating elo_rating DOUBLE PRECISION DEFAULT 1500 NOT NULL, CHANGE performance_score performance_score DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE strength_of_schedule strength_of_schedule DOUBLE PRECISION DEFAULT 1500 NOT NULL');
        $this->addSql('ALTER TABLE performance_score CHANGE score score DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE ranking CHANGE points points DOUBLE PRECISION DEFAULT 0 NOT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
        $this->addSql('ALTER TABLE fighter CHANGE elo_rating elo_rating DOUBLE PRECISION DEFAULT \'1500\' NOT NULL, CHANGE performance_score performance_score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE strength_of_schedule strength_of_schedule DOUBLE PRECISION DEFAULT \'1500\' NOT NULL');
        $this->addSql('ALTER TABLE performance_score CHANGE score score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
        $this->addSql('ALTER TABLE ranking CHANGE points points DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
    }
}
