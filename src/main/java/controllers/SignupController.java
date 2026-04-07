package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import utils.MyDatabase;

import java.io.IOException;
import java.sql.*;

public class SignupController {

    @FXML private TextField     txtFirstName;
    @FXML private TextField     txtLastName;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPhone;
    @FXML private Label         lblError;
    @FXML private Label         lblSuccess;
    @FXML private Label         lblPasswordStrength;
    @FXML private VBox          cardAdmin;
    @FXML private VBox          cardOrganizer;
    @FXML private VBox          cardFighter;
    @FXML private VBox          cardFan;
    @FXML private VBox          cardCoach;

    private String selectedRole = null;

    @FXML
    public void initialize() {
        if (txtPassword != null) {
            txtPassword.textProperty().addListener(
                (obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
    }

    private void updatePasswordStrength(String password) {
        if (lblPasswordStrength == null) return;
        lblPasswordStrength.setVisible(true);
        if (password.length() < 8) {
            lblPasswordStrength.setText("Too short");
            lblPasswordStrength.setStyle("-fx-text-fill: #E50914; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else if (!password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:,.<>?]).{8,}$")) {
            lblPasswordStrength.setText("Weak — add uppercase, number and symbol");
            lblPasswordStrength.setStyle("-fx-text-fill: #FF6D00; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lblPasswordStrength.setText("Strong ✓");
            lblPasswordStrength.setStyle("-fx-text-fill: #00C853; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    @FXML void selectAdmin(MouseEvent e)     { selectedRole = "ADMIN";     resetAllCards(); highlightCard(cardAdmin,     "#E50914"); }
    @FXML void selectOrganizer(MouseEvent e) { selectedRole = "ORGANIZER"; resetAllCards(); highlightCard(cardOrganizer, "#FF6D00"); }
    @FXML void selectFighter(MouseEvent e)   { selectedRole = "FIGHTER";   resetAllCards(); highlightCard(cardFighter,   "#00D4FF"); }
    @FXML void selectFan(MouseEvent e)       { selectedRole = "FAN";       resetAllCards(); highlightCard(cardFan,       "#7C3AED"); }
    @FXML void selectCoach(MouseEvent e)     { selectedRole = "COACH";     resetAllCards(); highlightCard(cardCoach,     "#00C853"); }

    private void resetAllCards() {
        String base = "-fx-background-color: #1A1A2E; -fx-background-radius: 10; " +
                      "-fx-border-color: transparent; -fx-border-width: 1; " +
                      "-fx-border-radius: 10; -fx-padding: 10 12 10 12;";
        if (cardAdmin     != null) cardAdmin.setStyle(base);
        if (cardOrganizer != null) cardOrganizer.setStyle(base);
        if (cardFighter   != null) cardFighter.setStyle(base);
        if (cardFan       != null) cardFan.setStyle(base);
        if (cardCoach     != null) cardCoach.setStyle(base);
    }

    private void highlightCard(VBox card, String color) {
        if (card == null) return;
        card.setStyle("-fx-background-color: #2A2A3E; -fx-background-radius: 10; " +
                      "-fx-border-color: " + color + "; -fx-border-width: 2; " +
                      "-fx-border-radius: 10; -fx-padding: 10 12 10 12;");
    }

    @FXML
    void handleSignup(ActionEvent event) {
        lblError.setVisible(false);
        lblSuccess.setVisible(false);

        String firstName = txtFirstName.getText().trim();
        String lastName  = txtLastName.getText().trim();
        String email     = txtEmail.getText().trim();
        String password  = txtPassword.getText();
        String phone     = txtPhone.getText().trim();

        if (firstName.isEmpty() || !firstName.matches("^[a-zA-Z ]{2,50}$")) {
            showError("First name must be 2-50 letters only."); return;
        }
        if (lastName.isEmpty() || !lastName.matches("^[a-zA-Z ]{2,50}$")) {
            showError("Last name must be 2-50 letters only."); return;
        }
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Please enter a valid email address."); return;
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:,.<>?]).{8,}$")) {
            showError("Password needs 8+ chars, uppercase, lowercase, number and special character."); return;
        }
        if (!phone.isEmpty() && !phone.matches("^\\d{8,15}$")) {
            showError("Phone must be 8-15 digits only."); return;
        }
        if (selectedRole == null) {
            showError("Please select a role."); return;
        }

        Connection conn = MyDatabase.getInstance().getConnection();
        try {
            PreparedStatement checkEmail = conn.prepareStatement("SELECT id FROM user WHERE email = ?");
            checkEmail.setString(1, email);
            if (checkEmail.executeQuery().next()) {
                showError("This email is already registered."); return;
            }

            PreparedStatement getRoleId = conn.prepareStatement("SELECT id FROM user_role WHERE name = ?");
            getRoleId.setString(1, selectedRole);
            ResultSet rsRole = getRoleId.executeQuery();
            if (!rsRole.next()) { showError("Role not found."); return; }
            int roleId = rsRole.getInt("id");

            PreparedStatement insertUser = conn.prepareStatement(
                "INSERT INTO user (first_name, last_name, email, password, phone, role_id, is_active) " +
                "VALUES (?,?,?,?,?,?,1)", Statement.RETURN_GENERATED_KEYS);
            insertUser.setString(1, firstName);
            insertUser.setString(2, lastName);
            insertUser.setString(3, email);
            insertUser.setString(4, password);
            insertUser.setString(5, phone.isEmpty() ? null : phone);
            insertUser.setInt(6, roleId);
            insertUser.executeUpdate();

            int newUserId = 0;
            ResultSet keys = insertUser.getGeneratedKeys();
            if (keys.next()) newUserId = keys.getInt(1);

            if ("FIGHTER".equals(selectedRole) && newUserId > 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO fighter (user_id, nickname, date_of_birth, nationality, wins, losses, draws, status) " +
                    "VALUES (?,?,?,?,0,0,0,'ACTIVE')");
                ps.setInt(1, newUserId);
                ps.setString(2, firstName + " " + lastName);
                ps.setString(3, "2000-01-01");
                ps.setString(4, "Unknown");
                ps.executeUpdate();
            }

            if ("COACH".equals(selectedRole) && newUserId > 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coach (user_id, speciality, experience_years, certification, status) " +
                    "VALUES (?,'General',0,'None','ACTIVE')");
                ps.setInt(1, newUserId);
                ps.executeUpdate();
            }

            lblSuccess.setText("Account created! Redirecting to login...");
            lblSuccess.setVisible(true);
            lblError.setVisible(false);

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(this::navigateToLogin);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML void goToLogin(ActionEvent event) { navigateToLogin(); }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            txtFirstName.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblSuccess.setVisible(false);
    }
}
