package tn.smartfight.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DBConnection {
    private static HikariDataSource ds;

    public static synchronized DataSource getDataSource() {
        if (ds == null) {
            AppConfig cfg = AppConfig.get();
            HikariConfig hikari = new HikariConfig();
            hikari.setJdbcUrl(cfg.dbUrl);
            hikari.setUsername(cfg.dbUser);
            hikari.setPassword(cfg.dbPassword);
            hikari.setMaximumPoolSize(10);
            hikari.setMinimumIdle(2);
            ds = new HikariDataSource(hikari);
        }
        return ds;
    }

    public static synchronized void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
            ds = null;
        }
    }
}
