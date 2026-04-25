<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260412143000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add event_booking table with one-row-per-user-per-event model and booking indexes';
    }

    public function up(Schema $schema): void
    {
        $this->addSql(<<<'SQL'
CREATE TABLE IF NOT EXISTS event_booking (
    id INT AUTO_INCREMENT NOT NULL,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    ticket_quantity INT NOT NULL DEFAULT 1,
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    ticket_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    booking_date DATE NOT NULL,
    booking_reference VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_eb_event_status (event_id, booking_status),
    INDEX idx_eb_user_status (user_id, booking_status),
    UNIQUE INDEX uq_eb_reference (booking_reference),
    UNIQUE INDEX uq_eb_event_user (event_id, user_id),
    PRIMARY KEY(id),
    CONSTRAINT fk_eb_event FOREIGN KEY (event_id) REFERENCES event (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_eb_user FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB
SQL);
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS event_booking');
    }
}
