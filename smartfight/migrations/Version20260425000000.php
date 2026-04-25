<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260425000000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Phase 1 cleanup: add user.username, drop legacy event columns (start_date, end_date, venue_id, organizer_id, is_champions_event, location)';
    }

    public function up(Schema $schema): void
    {
        // Add username to user table (nullable — existing rows get no username)
        $this->addSql('ALTER TABLE user ADD COLUMN username VARCHAR(50) DEFAULT NULL');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_8D93D649F85E0677 ON user (username)');

        // Drop legacy event columns replaced by new boxing-oriented fields
        $this->addSql('ALTER TABLE event
            DROP COLUMN start_date,
            DROP COLUMN end_date,
            DROP COLUMN venue_id,
            DROP COLUMN organizer_id,
            DROP COLUMN is_champions_event,
            DROP COLUMN location');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP INDEX UNIQ_8D93D649F85E0677 ON user');
        $this->addSql('ALTER TABLE user DROP COLUMN username');

        $this->addSql('ALTER TABLE event
            ADD COLUMN start_date  DATE         NOT NULL DEFAULT \'2000-01-01\',
            ADD COLUMN end_date    DATE         NOT NULL DEFAULT \'2000-01-01\',
            ADD COLUMN venue_id    INT          DEFAULT NULL,
            ADD COLUMN organizer_id INT         DEFAULT NULL,
            ADD COLUMN is_champions_event TINYINT(1) NOT NULL DEFAULT 0,
            ADD COLUMN location    VARCHAR(255) DEFAULT NULL');
    }
}
