package tn.smartfight.integration;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tn.smartfight.config.AppConfig;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmailMailer {
    private static final Logger LOG = Logger.getLogger(GmailMailer.class.getName());

    public static void send(String to, String subject, String htmlBody) throws Exception {
        AppConfig cfg = AppConfig.get();
        if (cfg.mailSmtpHost.isBlank()) {
            throw new IllegalStateException("SMTP host not configured");
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", cfg.mailSmtpHost);
        props.put("mail.smtp.port", String.valueOf(cfg.mailSmtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.mailSmtpUser, cfg.mailSmtpPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(cfg.mailFrom));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");
        Transport.send(message);
        LOG.info("Email sent to: " + to);
    }
}
