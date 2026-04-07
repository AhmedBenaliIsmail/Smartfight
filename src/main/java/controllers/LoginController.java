package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.MyDatabase;
import tn.smartfight.models.User;
import tn.smartfight.session.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    @FXML
    private void goToSignup() {
        try {
            java.net.URL resource = getClass().getResource("/Signup.fxml");
            if (resource == null) {
                showError("Signup.fxml not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent rootNode = loader.load();
            txtEmail.getScene().setRoot(rootNode);
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        lblError.setVisible(false);

        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        Connection cnx = MyDatabase.getInstance().getConnection();
        if (cnx == null) {
            showError("Cannot connect to database.");
            return;
        }

        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.role_id, " +
                     "ur.name AS role_name " +
                     "FROM user u " +
                     "JOIN user_role ur ON u.role_id = ur.id " +
                     "WHERE u.email = ? AND u.password = ? AND u.is_active = 1";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showError("Invalid email or password.");
                    return;
                }

                String role      = rs.getString("role_name");
                int    userId    = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName  = rs.getString("last_name");
                String dbEmail   = rs.getString("email");
                int    roleId    = rs.getInt("role_id");

                // Store in both legacy LoggedInUser and unified SessionManager
                utils.LoggedInUser loggedInUser =
                    new utils.LoggedInUser(userId, firstName, lastName, dbEmail, role, roleId);
                utils.LoggedInUser.setInstance(loggedInUser);

                User m5User = new User(userId, firstName, lastName, dbEmail, null, roleId, true);

                Parent root;
                switch (role.toUpperCase()) {
                    case "ADMIN": {
                        SessionManager.loginAsAdmin(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/AdminShellView.fxml"));
                        break;
                    }
                    case "FAN": {
                        SessionManager.loginAsFan(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/FanShellView.fxml"));
                        break;
                    }
                    case "ORGANIZER": {
                        SessionManager.loginAsOrganizer(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/OrganizerShellView.fxml"));
                        break;
                    }
                    case "FIGHTER": {
                        SessionManager.loginAsFighter(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/FighterShellView.fxml"));
                        break;
                    }
                    case "COACH": {
                        SessionManager.loginAsCoach(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/CoachShellView.fxml"));
                        break;
                    }
                    default: {
                        SessionManager.loginAsAdmin(m5User);
                        root = FXMLLoader.load(getClass().getResource(
                            "/tn/smartfight/views/AdminShellView.fxml"));
                        break;
                    }
                }

                txtEmail.getScene().setRoot(root);
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (IOException e) {
            showError("Cannot load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
