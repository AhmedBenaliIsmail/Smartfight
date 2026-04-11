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
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE classement (id INT AUTO_INCREMENT NOT NULL, score DOUBLE PRECISION NOT NULL, rang INT NOT NULL, discipline VARCHAR(255) NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE classement_combattant (classement_id INT NOT NULL, combattant_id INT NOT NULL, INDEX IDX_E08F0133A513A63E (classement_id), INDEX IDX_E08F01334B0BCC96 (combattant_id), PRIMARY KEY (classement_id, combattant_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE classement_combattant ADD CONSTRAINT FK_E08F0133A513A63E FOREIGN KEY (classement_id) REFERENCES classement (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE classement_combattant ADD CONSTRAINT FK_E08F01334B0BCC96 FOREIGN KEY (combattant_id) REFERENCES combattant (id) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE classement_combattant DROP FOREIGN KEY FK_E08F0133A513A63E');
        $this->addSql('ALTER TABLE classement_combattant DROP FOREIGN KEY FK_E08F01334B0BCC96');
        $this->addSql('DROP TABLE classement');
        $this->addSql('DROP TABLE classement_combattant');
    }
}
