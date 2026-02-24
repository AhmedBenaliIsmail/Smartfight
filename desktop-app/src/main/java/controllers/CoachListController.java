package controllers;

import entities.Coach;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceCoach;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CoachListController implements Initializable {

    @FXML
    private TableView<Coach> tableCoaches;
    @FXML
    private TableColumn<Coach, Integer> colId;
    @FXML
    private TableColumn<Coach, Integer> colUserId;
    @FXML
    private TableColumn<Coach, String> colSpeciality;
    @FXML
    private TableColumn<Coach, Integer> colExperienceYears;
    @FXML
    private TableColumn<Coach, String> colCertification;
    @FXML
    private TableColumn<Coach, String> colStatus;

    private ServiceCoach serviceCoach;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceCoach = new ServiceCoach();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colSpeciality.setCellValueFactory(new PropertyValueFactory<>("speciality"));
        colExperienceYears.setCellValueFactory(new PropertyValueFactory<>("experienceYears"));
        colCertification.setCellValueFactory(new PropertyValueFactory<>("certification"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadCoaches();
    }

    private void loadCoaches() {
        try {
            List<Coach> coaches = serviceCoach.recuperer();
            ObservableList<Coach> list = FXCollections.observableArrayList(coaches);
            tableCoaches.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddCoach.fxml"));
            tableCoaches.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void navigateToEdit(ActionEvent event) {
        Coach selected = tableCoaches.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a coach to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateCoach.fxml"));
            Parent root = loader.load();
            UpdateCoachController controller = loader.getController();
            controller.setCoach(selected);
            tableCoaches.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void deleteCoach(ActionEvent event) {
        Coach selected = tableCoaches.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a coach to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete coach id=" + selected.getId() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceCoach.supprimer(selected);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Coach deleted successfully.");
                loadCoaches();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            }
        }
    }

    @FXML
    void navigateToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            tableCoaches.getScene().setRoot(root);
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
