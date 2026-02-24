package controllers;

import entities.Fighter;
import entities.User;
import entities.WeightClass;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceFighter;
import services.ServiceUser;
import services.ServiceWeightClass;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AddFighterController {

    @FXML
    private ComboBox<String> cbUserId;
    @FXML
    private TextField txtNickname;
    @FXML
    private DatePicker dpDateOfBirth;
    @FXML
    private TextField txtNationality;
    @FXML
    private ComboBox<String> cbWeightClass;
    @FXML
    private ChoiceBox<String> cbStatus;

    private ServiceFighter serviceFighter;
    private ServiceUser serviceUser;
    private ServiceWeightClass serviceWeightClass;
    private List<User> userList;
    private List<WeightClass> weightClassList;

    @FXML
    public void initialize() {
        serviceFighter = new ServiceFighter();
        serviceUser = new ServiceUser();
        serviceWeightClass = new ServiceWeightClass();

        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "SUSPENDED"));
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

        try {
            weightClassList = serviceWeightClass.recuperer();
            for (WeightClass wc : weightClassList)
                cbWeightClass.getItems().add(wc.getId() + " - " + wc.getName());
            if (!cbWeightClass.getItems().isEmpty())
                cbWeightClass.setValue(cbWeightClass.getItems().get(0));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not load weight classes: " + e.getMessage());
        }
    }

    @FXML
    void saveFighter(ActionEvent event) {
        if (cbUserId.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "User is required.");
            return;
        }
        String nickname = txtNickname.getText().trim();
        if (nickname.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Nickname is required.");
            return;
        }

        int userId = userList.get(cbUserId.getItems().indexOf(cbUserId.getValue())).getId();
        int weightClassId = 0;
        if (cbWeightClass.getValue() != null && !weightClassList.isEmpty()) {
            int idx = cbWeightClass.getItems().indexOf(cbWeightClass.getValue());
            if (idx >= 0 && idx < weightClassList.size())
                weightClassId = weightClassList.get(idx).getId();
        }
        LocalDate dob = dpDateOfBirth.getValue();

        Fighter fighter = new Fighter(userId, nickname, dob, txtNationality.getText().trim(), weightClassId, 0, 0, 0,
                cbStatus.getValue());
        try {
            serviceFighter.ajouter(fighter);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Fighter added successfully.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FighterList.fxml"));
            txtNickname.getScene().setRoot(root);
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
