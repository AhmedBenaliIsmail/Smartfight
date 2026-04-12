ALTER TABLE fighters ADD championsEventWinStreak INT DEFAULT 0 NOT NULL, ADD titleDefenses INT DEFAULT 0 NOT NULL;
ALTER TABLE events ADD isChampionsEvent TINYINT(1) DEFAULT 0 NOT NULL;
ALTER TABLE ranking ADD weightClass VARCHAR(50) DEFAULT NULL;
ALTER TABLE fight_statistic ADD controlTimeSeconds INT DEFAULT 0 NOT NULL;
