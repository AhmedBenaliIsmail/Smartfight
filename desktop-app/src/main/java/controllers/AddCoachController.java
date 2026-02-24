package controllers;

import entities.Coach;
import entities.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceCoach;
import services.ServiceUser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AddCoachController {

    @FXML
    private ComboBox<String> cbUserId;
    @FXML
    private TextField txtSpeciality;
    @FXML
    private TextField txtExperienceYears;
    @FXML
    private TextField txtCertification;
    @FXML
    private ChoiceBox<String> cbStatus;

    private ServiceCoach serviceCoach;
    private ServiceUser serviceUser;
    private List<User> userList;

    @FXML
    public void initialize() {
        serviceCoach = new ServiceCoach();
        serviceUser = new ServiceUser();
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cbStatus.setValue("ACTIVE");
        try {
            userList = serviceUser.recuperer();
            for (User u : userList)
                cbUserId.getItems().add(u.getId() + " - " + u.getFirstName() + " " + u.getLastName());
            if (!cbUserId.getItems().isEmpty())
                cbUserId.setValue(cbUserId.getItems().get(0));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not load users: " + e.getMessage());
        }
    }

    @FXML
    void saveCoach(ActionEvent event) {
        if (cbUserId.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "User is required.");
            return;
        }
        String speciality = txtSpeciality.getText().trim();
        if (speciality.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Speciality is required.");
            return;
        }
        int expYears = 0;
        try {
            expYears = Integer.parseInt(txtExperienceYears.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Experience years must be a number.");
            return;
        }
        int userId = userList.get(cbUserId.getItems().indexOf(cbUserId.getValue())).getId();
        Coach coach = new Coach(userId, speciality, expYears, txtCertification.getText().trim(), cbStatus.getValue());
        try {
            serviceCoach.ajouter(coach);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Coach added successfully.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CoachList.fxml"));
            txtSpeciality.getScene().setRoot(root);
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
