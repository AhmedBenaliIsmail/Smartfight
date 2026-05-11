package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;
import tn.smartfight.config.DBConnection;
import tn.smartfight.config.Session;
import tn.smartfight.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FanProfileController {
    private static final Logger LOG = Logger.getLogger(FanProfileController.class.getName());

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private Label         infoStatusLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         pwStatusLabel;
    @FXML private Label         pointsBadge;

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user == null) return;
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        pointsBadge.setText(user.getPredictionPoints() + " PTS");
    }

    @FXML
    private void onUpdateInfo() {
        String newUsername = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String newEmail    = emailField.getText()    != null ? emailField.getText().trim()    : "";

        if (newUsername.isEmpty()) { infoStatusLabel.setText("Username cannot be empty."); return; }

        User user = Session.getUser();
        if (user == null) { infoStatusLabel.setText("Not logged in."); return; }

        infoStatusLabel.setText("Saving…");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                DataSource ds = DBConnection.getDataSource();
                try (Connection conn = ds.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE users SET username=?, email=? WHERE userId=?")) {
                    ps.setString(1, newUsername);
                    ps.setString(2, newEmail);
                    ps.setInt(3, user.getUserId());
                    ps.executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            infoStatusLabel.setText("Profile updated!");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "onUpdateInfo failed", task.getException());
            infoStatusLabel.setText("Update failed — username may be taken.");
        });
        new Thread(task, "profile-update-info").start();
    }

    @FXML
    private void onUpdatePassword() {
        String newPw  = newPasswordField.getText();
        String confPw = confirmPasswordField.getText();

        if (newPw == null || newPw.length() < 6) {
            pwStatusLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!newPw.equals(confPw)) {
            pwStatusLabel.setText("Passwords do not match.");
            return;
        }

        User user = Session.getUser();
        if (user == null) { pwStatusLabel.setText("Not logged in."); return; }

        String hashed = BCrypt.hashpw(newPw, BCrypt.gensalt(13));

        pwStatusLabel.setText("Saving…");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                DataSource ds = DBConnection.getDataSource();
                try (Connection conn = ds.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE users SET password=? WHERE userId=?")) {
                    ps.setString(1, hashed);
                    ps.setInt(2, user.getUserId());
                    ps.executeUpdate();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            pwStatusLabel.setText("Password updated!");
            newPasswordField.clear();
            confirmPasswordField.clear();
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "onUpdatePassword failed", task.getException());
            pwStatusLabel.setText("Update failed.");
        });
        new Thread(task, "profile-update-pw").start();
    }
}
