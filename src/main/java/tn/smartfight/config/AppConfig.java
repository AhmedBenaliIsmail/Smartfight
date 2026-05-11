package tn.smartfight.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static AppConfig INSTANCE;

    public final String dbUrl;
    public final String dbUser;
    public final String dbPassword;
    public final String mailSmtpHost;
    public final int mailSmtpPort;
    public final String mailSmtpUser;
    public final String mailSmtpPassword;
    public final String mailFrom;
    public final String symfonyBaseUrl;
    public final String uploadDir;
    public final String deepseekApiKey;
    public final String deepseekApiUrl;
    public final String googleClientId;
    public final String googleClientSecret;

    private AppConfig(Properties props) {
        this.dbUrl = require(props, "db.url");
        this.dbUser = require(props, "db.user");
        this.dbPassword = props.getProperty("db.password", "");
        this.mailSmtpHost = props.getProperty("mail.smtp.host", "").trim();
        this.mailSmtpPort = Integer.parseInt(props.getProperty("mail.smtp.port", "587"));
        this.mailSmtpUser = props.getProperty("mail.smtp.user", "");
        this.mailSmtpPassword = props.getProperty("mail.smtp.password", "");
        this.mailFrom = props.getProperty("mail.from", "");
        this.symfonyBaseUrl = props.getProperty("symfony.base.url", "http://localhost");
        this.uploadDir = props.getProperty("upload.dir", "uploads");
        this.deepseekApiKey = props.getProperty("deepseek.api.key", "");
        this.deepseekApiUrl = props.getProperty("deepseek.api.url", "https://api.deepseek.com/v1");
        this.googleClientId     = props.getProperty("google.client.id", "");
        this.googleClientSecret = props.getProperty("google.client.secret", "");
    }

    public static void loadFromClasspath(String resourceName) {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new IllegalStateException("Config not found: " + resourceName);
            props.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load config: " + resourceName, e);
        }
        INSTANCE = new AppConfig(props);
    }

    public static AppConfig fromProperties(Properties props) {
        INSTANCE = new AppConfig(props);
        return INSTANCE;
    }

    public static AppConfig get() {
        if (INSTANCE == null) throw new IllegalStateException("AppConfig not initialised — call loadFromClasspath first");
        return INSTANCE;
    }

    private static String require(Properties props, String key) {
        String val = props.getProperty(key);
        if (val == null || val.isBlank()) throw new IllegalStateException("Required config missing: " + key);
        return val;
    }
}
