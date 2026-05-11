package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.smartfight.auth.PasswordVerifier;
import tn.smartfight.config.AppConfig;
import tn.smartfight.dao.UserDao;
import tn.smartfight.integration.GmailMailer;
import tn.smartfight.integration.PebbleRenderer;
import tn.smartfight.model.User;
import tn.smartfight.util.TokenGenerator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterController {
    private static final Logger LOG = Logger.getLogger(RegisterController.class.getName());

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;
    @FXML private Button resendButton;

    private final UserDao userDao = new UserDao();
    private User registeredUser;

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            statusLabel.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }
        statusLabel.setText("Registering...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                User u = new User();
                u.setUsername(username);
                u.setEmail(email);
                u.setPassword(PasswordVerifier.hashBcrypt(password));
                u.setVerificationToken(TokenGenerator.generate());

                int userId = userDao.create(u);
                if (userId < 0) throw new RuntimeException("Failed to create user");

                int roleId = userDao.findRoleId("ROLE_USER");
                if (roleId > 0) userDao.assignRole(userId, roleId);

                registeredUser = u;

                AppConfig cfg = AppConfig.get();
                if (cfg.mailSmtpHost.isBlank() || cfg.mailSmtpUser.isBlank()) return false;

                String verifyUrl = cfg.symfonyBaseUrl + "/verify/email?token=" + u.getVerificationToken();
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("user", u);
                ctx.put("verifyUrl", verifyUrl);
                ctx.put("now", LocalDateTime.now());
                String html = PebbleRenderer.render("templates/emails/verification.html.twig", ctx);
                GmailMailer.send(email, "Verify your SmartFight account", html);
                return true;
            }
        };
        task.setOnSucceeded(ev -> {
            boolean emailSent = task.getValue();
            if (emailSent) {
                statusLabel.setText("Registered! Check your email to verify your account.");
                if (resendButton != null) resendButton.setDisable(false);
            } else {
                statusLabel.setText("Registered! Email not configured — ask admin to verify your account.");
            }
        });
        task.setOnFailed(ev -> {
            LOG.log(Level.SEVERE, "Registration failed", task.getException());
            statusLabel.setText("Registration failed: " + task.getException().getMessage());
        });
        new Thread(task, "register-task").start();
    }

    @FXML
    private void onResendEmail() {
        if (registeredUser == null) return;
        AppConfig cfg = AppConfig.get();
        if (cfg.mailSmtpHost.isBlank()) {
            statusLabel.setText("SMTP not configured.");
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String verifyUrl = cfg.symfonyBaseUrl + "/verify/email?token=" + registeredUser.getVerificationToken();
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("user", registeredUser);
                ctx.put("verifyUrl", verifyUrl);
                ctx.put("now", LocalDateTime.now());
                String html = PebbleRenderer.render("templates/emails/verification.html.twig", ctx);
                GmailMailer.send(registeredUser.getEmail(), "Verify your SmartFight account", html);
                return null;
            }
        };
        task.setOnSucceeded(ev -> statusLabel.setText("Verification email resent."));
        task.setOnFailed(ev -> statusLabel.setText("Failed to resend: " + task.getException().getMessage()));
        new Thread(task, "resend-task").start();
    }

    @FXML
    private void onBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/smartfight/views/Login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Navigate to login failed", e);
        }
    }
}
