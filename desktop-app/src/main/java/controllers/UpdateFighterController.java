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
import java.util.List;

public class UpdateFighterController {

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
    private Fighter currentFighter;

    @FXML
    public void initialize() {
        serviceFighter = new ServiceFighter();
        serviceUser = new ServiceUser();
        serviceWeightClass = new ServiceWeightClass();
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "SUSPENDED"));
        try {
            userList = serviceUser.recuperer();
            for (User u : userList)
                cbUserId.getItems().add(u.getId() + " - " + u.getFirstName() + " " + u.getLastName());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
        try {
            weightClassList = serviceWeightClass.recuperer();
            for (WeightClass wc : weightClassList)
                cbWeightClass.getItems().add(wc.getId() + " - " + wc.getName());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    public void setFighter(Fighter fighter) {
        this.currentFighter = fighter;
        txtNickname.setText(fighter.getNickname());
        dpDateOfBirth.setValue(fighter.getDateOfBirth());
        txtNationality.setText(fighter.getNationality());
        cbStatus.setValue(fighter.getStatus());
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == fighter.getUserId()) {
                cbUserId.setValue(cbUserId.getItems().get(i));
                break;
            }
        }
        for (int i = 0; i < weightClassList.size(); i++) {
            if (weightClassList.get(i).getId() == fighter.getWeightClassId()) {
                cbWeightClass.setValue(cbWeightClass.getItems().get(i));
                break;
            }
        }
    }

    @FXML
    void updateFighter(ActionEvent event) {
        String nickname = txtNickname.getText().trim();
        if (nickname.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Nickname is required.");
            return;
        }
        int userId = currentFighter.getUserId();
        if (cbUserId.getValue() != null && !userList.isEmpty()) {
            int idx = cbUserId.getItems().indexOf(cbUserId.getValue());
            if (idx >= 0 && idx < userList.size())
                userId = userList.get(idx).getId();
        }
        int weightClassId = currentFighter.getWeightClassId();
        if (cbWeightClass.getValue() != null && !weightClassList.isEmpty()) {
            int idx = cbWeightClass.getItems().indexOf(cbWeightClass.getValue());
            if (idx >= 0 && idx < weightClassList.size())
                weightClassId = weightClassList.get(idx).getId();
        }
        currentFighter.setUserId(userId);
        currentFighter.setNickname(nickname);
        currentFighter.setDateOfBirth(dpDateOfBirth.getValue());
        currentFighter.setNationality(txtNationality.getText().trim());
        currentFighter.setWeightClassId(weightClassId);
        currentFighter.setStatus(cbStatus.getValue());
        try {
            serviceFighter.modifier(currentFighter);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Fighter updated successfully.");
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
