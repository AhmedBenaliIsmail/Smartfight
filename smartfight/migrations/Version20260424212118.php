<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260424212118 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('SET FOREIGN_KEY_CHECKS = 0');
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE fighter CHANGE elo_rating elo_rating DOUBLE PRECISION DEFAULT 1500 NOT NULL, CHANGE performance_score performance_score DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE strength_of_schedule strength_of_schedule DOUBLE PRECISION DEFAULT 1500 NOT NULL');
        $this->addSql('ALTER TABLE performance_score CHANGE score score DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT 0 NOT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_80B839D0A0206A65');
        $this->addSql('ALTER TABLE ranking CHANGE points points DOUBLE PRECISION DEFAULT 0 NOT NULL');
        $this->addSql('DROP INDEX fk_80b839d0a0206a65 ON ranking');
        $this->addSql('CREATE INDEX IDX_80B839D0A0206A65 ON ranking (weight_class_id)');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_80B839D0A0206A65 FOREIGN KEY (weight_class_id) REFERENCES weight_class (id) ON DELETE SET NULL');
        $this->addSql('DROP INDEX uniq_system_meta_key ON system_meta');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_C63EA7C3251C7524 ON system_meta (meta_key)');
        $this->addSql('ALTER TABLE user DROP FOREIGN KEY fk_user_role');
        $this->addSql('DROP INDEX fk_user_role ON user');
        $this->addSql('DROP INDEX uq_user_email ON user');
        $this->addSql('DROP INDEX uq_weight_class_name ON weight_class');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_WC_DISCIPLINE');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_WC_CHAMPION');
        $this->addSql('ALTER TABLE weight_class CHANGE slug slug VARCHAR(100) NOT NULL');
        $this->addSql('DROP INDEX uniq_wc_slug ON weight_class');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_D2EC4C2F989D9B62 ON weight_class (slug)');
        $this->addSql('DROP INDEX fk_wc_discipline ON weight_class');
        $this->addSql('CREATE INDEX IDX_D2EC4C2FA5522701 ON weight_class (discipline_id)');
        $this->addSql('DROP INDEX fk_wc_champion ON weight_class');
        $this->addSql('CREATE INDEX IDX_D2EC4C2FFA7FD7EB ON weight_class (champion_id)');
        $this->addSql('ALTER TABLE weight_class ADD CONSTRAINT FK_WC_DISCIPLINE FOREIGN KEY (discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE weight_class ADD CONSTRAINT FK_WC_CHAMPION FOREIGN KEY (champion_id) REFERENCES fighter (id) ON DELETE SET NULL');
        $this->addSql('SET FOREIGN_KEY_CHECKS = 1');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE event_booking CHANGE total_price total_price NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
        $this->addSql('ALTER TABLE fighter CHANGE elo_rating elo_rating DOUBLE PRECISION DEFAULT \'1500\' NOT NULL, CHANGE performance_score performance_score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE strength_of_schedule strength_of_schedule DOUBLE PRECISION DEFAULT \'1500\' NOT NULL');
        $this->addSql('ALTER TABLE performance_score CHANGE score score DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE aggression aggression DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE defense defense DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE technique technique DOUBLE PRECISION DEFAULT \'0\' NOT NULL, CHANGE experience experience DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
        $this->addSql('ALTER TABLE ranking DROP FOREIGN KEY FK_80B839D0A0206A65');
        $this->addSql('ALTER TABLE ranking CHANGE points points DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
        $this->addSql('DROP INDEX idx_80b839d0a0206a65 ON ranking');
        $this->addSql('CREATE INDEX FK_80B839D0A0206A65 ON ranking (weight_class_id)');
        $this->addSql('ALTER TABLE ranking ADD CONSTRAINT FK_80B839D0A0206A65 FOREIGN KEY (weight_class_id) REFERENCES weight_class (id) ON DELETE SET NULL');
        $this->addSql('DROP INDEX uniq_c63ea7c3251c7524 ON system_meta');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_SYSTEM_META_KEY ON system_meta (meta_key)');
        $this->addSql('ALTER TABLE user ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES user_role (id) ON UPDATE CASCADE');
        $this->addSql('CREATE INDEX fk_user_role ON user (role_id)');
        $this->addSql('CREATE UNIQUE INDEX uq_user_email ON user (email)');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_D2EC4C2FA5522701');
        $this->addSql('ALTER TABLE weight_class DROP FOREIGN KEY FK_D2EC4C2FFA7FD7EB');
        $this->addSql('ALTER TABLE weight_class CHANGE slug slug VARCHAR(100) DEFAULT \'\' NOT NULL');
        $this->addSql('CREATE UNIQUE INDEX uq_weight_class_name ON weight_class (name)');
        $this->addSql('DROP INDEX idx_d2ec4c2fa5522701 ON weight_class');
        $this->addSql('CREATE INDEX FK_WC_DISCIPLINE ON weight_class (discipline_id)');
        $this->addSql('DROP INDEX idx_d2ec4c2ffa7fd7eb ON weight_class');
        $this->addSql('CREATE INDEX FK_WC_CHAMPION ON weight_class (champion_id)');
        $this->addSql('DROP INDEX uniq_d2ec4c2f989d9b62 ON weight_class');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_WC_SLUG ON weight_class (slug)');
        $this->addSql('ALTER TABLE weight_class ADD CONSTRAINT FK_D2EC4C2FA5522701 FOREIGN KEY (discipline_id) REFERENCES discipline (id)');
        $this->addSql('ALTER TABLE weight_class ADD CONSTRAINT FK_D2EC4C2FFA7FD7EB FOREIGN KEY (champion_id) REFERENCES fighter (id) ON DELETE SET NULL');
    }
}
