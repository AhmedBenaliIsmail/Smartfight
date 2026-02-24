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

public class UpdateCoachController {

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
    private Coach currentCoach;

    @FXML
    public void initialize() {
        serviceCoach = new ServiceCoach();
        serviceUser = new ServiceUser();
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        try {
            userList = serviceUser.recuperer();
            for (User u : userList)
                cbUserId.getItems().add(u.getId() + " - " + u.getFirstName() + " " + u.getLastName());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    public void setCoach(Coach coach) {
        this.currentCoach = coach;
        txtSpeciality.setText(coach.getSpeciality());
        txtExperienceYears.setText(String.valueOf(coach.getExperienceYears()));
        txtCertification.setText(coach.getCertification());
        cbStatus.setValue(coach.getStatus());
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == coach.getUserId()) {
                cbUserId.setValue(cbUserId.getItems().get(i));
                break;
            }
        }
    }

    @FXML
    void updateCoach(ActionEvent event) {
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
        int userId = currentCoach.getUserId();
        if (cbUserId.getValue() != null && !userList.isEmpty()) {
            int idx = cbUserId.getItems().indexOf(cbUserId.getValue());
            if (idx >= 0 && idx < userList.size())
                userId = userList.get(idx).getId();
        }
        currentCoach.setUserId(userId);
        currentCoach.setSpeciality(speciality);
        currentCoach.setExperienceYears(expYears);
        currentCoach.setCertification(txtCertification.getText().trim());
        currentCoach.setStatus(cbStatus.getValue());
        try {
            serviceCoach.modifier(currentCoach);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Coach updated successfully.");
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
