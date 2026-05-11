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
import tn.smartfight.auth.FaceIdRecognizer;
import tn.smartfight.auth.GoogleOAuthService;
import tn.smartfight.auth.PasswordVerifier;
import tn.smartfight.config.Session;
import tn.smartfight.dao.UserDao;
import tn.smartfight.model.User;
import tn.smartfight.util.AccessGuard;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;
    @FXML private Button        googleBtn;

    private final UserDao userDao = new UserDao();

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            statusLabel.setText("Username and password required.");
            return;
        }
        statusLabel.setText("Logging in...");

        Task<User> task = new Task<>() {
            @Override protected User call() {
                User u = userDao.findByUsername(username);
                if (u == null) u = userDao.findByEmail(username);
                if (u == null) return null;
                if (!PasswordVerifier.verify(password, u.getPassword())) return null;
                return u;
            }
        };
        task.setOnSucceeded(ev -> {
            User u = task.getValue();
            if (u == null) { statusLabel.setText("Invalid username or password."); return; }
            Session.setUser(u);
            navigateToShell(u);
        });
        task.setOnFailed(ev -> {
            LOG.log(Level.SEVERE, "Login task failed", task.getException());
            statusLabel.setText("Login error: " + task.getException().getMessage());
        });
        new Thread(task, "login-task").start();
    }

    @FXML
    private void onFaceIdLogin() {
        statusLabel.setText("Scanning face — look at the camera...");
        Task<User> task = new Task<>() {
            @Override protected User call() throws Exception {
                return FaceIdRecognizer.recognize(userDao);
            }
        };
        task.setOnSucceeded(ev -> {
            User u = task.getValue();
            Session.setUser(u);
            navigateToShell(u);
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            statusLabel.setText(ex != null ? ex.getMessage() : "Face ID scan failed.");
        });
        new Thread(task, "faceid-login").start();
    }

    @FXML
    private void onGoogleLogin() {
        if (googleBtn != null) googleBtn.setDisable(true);
        statusLabel.setText("Opening Google sign-in…");

        Task<User> task = new Task<>() {
            @Override protected User call() throws Exception {
                return new GoogleOAuthService().authenticate();
            }
        };
        task.setOnSucceeded(ev -> {
            User u = task.getValue();
            if (u == null) {
                statusLabel.setText("Google sign-in failed — no user returned.");
                if (googleBtn != null) googleBtn.setDisable(false);
                return;
            }
            Session.setUser(u);
            navigateToShell(u);
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            LOG.log(Level.SEVERE, "Google login task failed", ex);
            String msg = ex != null ? ex.getMessage() : "Google sign-in failed.";
            statusLabel.setText(msg);
            if (googleBtn != null) googleBtn.setDisable(false);
        });
        new Thread(task, "google-login").start();
    }

    @FXML
    private void onGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/smartfight/views/Register.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Navigate to register failed", e);
        }
    }

    private void navigateToShell(User u) {
        try {
            String fxml = AccessGuard.isAdmin()
                    ? "/tn/smartfight/views/AdminShell.fxml"
                    : "/tn/smartfight/views/FanShell.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), 1100, 700);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Navigate to shell failed", e);
            statusLabel.setText("Navigation error.");
        }
    }
}
