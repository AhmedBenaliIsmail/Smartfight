<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260411195209 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE classement (id INT AUTO_INCREMENT NOT NULL, combattant_id INT NOT NULL, score DOUBLE PRECISION NOT NULL, rang INT NOT NULL, discipline VARCHAR(50) NOT NULL, INDEX IDX_55EE9D6D4B0BCC96 (combattant_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE classement ADD CONSTRAINT FK_55EE9D6D4B0BCC96 FOREIGN KEY (combattant_id) REFERENCES combattant (id)');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE classement DROP FOREIGN KEY FK_55EE9D6D4B0BCC96');
        $this->addSql('DROP TABLE classement');
    }
}
