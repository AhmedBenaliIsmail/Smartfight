<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260411172928 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE combat (id INT AUTO_INCREMENT NOT NULL, score_ia DOUBLE PRECISION NOT NULL, resultat VARCHAR(50) DEFAULT NULL, date_combat DATETIME DEFAULT NULL, combattant1_id INT NOT NULL, combattant2_id INT NOT NULL, INDEX IDX_8D51E398CF4B102E (combattant1_id), INDEX IDX_8D51E398DDFEBFC0 (combattant2_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE combat ADD CONSTRAINT FK_8D51E398CF4B102E FOREIGN KEY (combattant1_id) REFERENCES combattant (id)');
        $this->addSql('ALTER TABLE combat ADD CONSTRAINT FK_8D51E398DDFEBFC0 FOREIGN KEY (combattant2_id) REFERENCES combattant (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE combat DROP FOREIGN KEY FK_8D51E398CF4B102E');
        $this->addSql('ALTER TABLE combat DROP FOREIGN KEY FK_8D51E398DDFEBFC0');
        $this->addSql('DROP TABLE combat');
    }
}
