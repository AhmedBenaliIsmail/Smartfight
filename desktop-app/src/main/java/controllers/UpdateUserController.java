package controllers;

import entities.User;
import entities.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceUser;
import services.ServiceUserRole;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UpdateUserController {

    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtPhone;
    @FXML
    private ComboBox<String> cbRole;

    private ServiceUser serviceUser;
    private ServiceUserRole serviceUserRole;
    private List<UserRole> roleList;
    private User currentUser;

    @FXML
    public void initialize() {
        serviceUser = new ServiceUser();
        serviceUserRole = new ServiceUserRole();
        try {
            roleList = serviceUserRole.recuperer();
            for (UserRole r : roleList) {
                cbRole.getItems().add(r.getId() + " - " + r.getName());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not load roles: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        txtFirstName.setText(user.getFirstName());
        txtLastName.setText(user.getLastName());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword());
        txtPhone.setText(user.getPhone());
        for (int i = 0; i < roleList.size(); i++) {
            if (roleList.get(i).getId() == user.getRoleId()) {
                cbRole.setValue(cbRole.getItems().get(i));
                break;
            }
        }
    }

    @FXML
    void updateUser(ActionEvent event) {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String phone = txtPhone.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "First name and last name are required.");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Validation", "A valid email is required.");
            return;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Password is required.");
            return;
        }
        int roleId = currentUser.getRoleId();
        if (cbRole.getValue() != null && !roleList.isEmpty()) {
            int idx = cbRole.getItems().indexOf(cbRole.getValue());
            if (idx >= 0 && idx < roleList.size())
                roleId = roleList.get(idx).getId();
        }
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setPassword(password);
        currentUser.setPhone(phone);
        currentUser.setRoleId(roleId);
        try {
            serviceUser.modifier(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UserList.fxml"));
            txtFirstName.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
