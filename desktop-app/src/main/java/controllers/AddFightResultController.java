package controllers;

import entities.Event;
import entities.Fighter;
import entities.FightResult;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceEvent;
import services.ServiceFighter;
import services.ServiceFightResult;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AddFightResultController {

    @FXML
    private ComboBox<String> cbEventId;
    @FXML
    private ComboBox<String> cbFighterRed;
    @FXML
    private ComboBox<String> cbFighterBlue;
    @FXML
    private ComboBox<String> cbWinner;
    @FXML
    private ChoiceBox<String> cbMethod;
    @FXML
    private TextField txtRoundEnded;
    @FXML
    private DatePicker dpFightDate;
    @FXML
    private TextArea txtNotes;

    private ServiceFightResult serviceFightResult;
    private ServiceEvent serviceEvent;
    private ServiceFighter serviceFighter;
    private List<Event> eventList;
    private List<Fighter> fighterList;

    @FXML
    public void initialize() {
        serviceFightResult = new ServiceFightResult();
        serviceEvent = new ServiceEvent();
        serviceFighter = new ServiceFighter();
        cbMethod.setItems(
                FXCollections.observableArrayList("KO", "TKO", "SUBMISSION", "DECISION", "DRAW", "NO_CONTEST"));
        cbMethod.setValue("KO");
        try {
            eventList = serviceEvent.recuperer();
            for (Event e : eventList)
                cbEventId.getItems().add(e.getId() + " - " + e.getName());
            if (!cbEventId.getItems().isEmpty())
                cbEventId.setValue(cbEventId.getItems().get(0));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
        try {
            fighterList = serviceFighter.recuperer();
            for (Fighter f : fighterList) {
                String label = f.getId() + " - " + f.getNickname();
                cbFighterRed.getItems().add(label);
                cbFighterBlue.getItems().add(label);
                cbWinner.getItems().add(label);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void saveFightResult(ActionEvent event) {
        if (cbEventId.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Event is required.");
            return;
        }
        if (cbFighterRed.getValue() == null || cbFighterBlue.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Both fighters are required.");
            return;
        }
        if (cbMethod.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Method is required.");
            return;
        }
        int round = 0;
        try {
            round = Integer.parseInt(txtRoundEnded.getText().trim());
        } catch (NumberFormatException e) {
            /* allow 0 */ }
        int eventId = eventList.get(cbEventId.getItems().indexOf(cbEventId.getValue())).getId();
        int redId = fighterList.get(cbFighterRed.getItems().indexOf(cbFighterRed.getValue())).getId();
        int blueId = fighterList.get(cbFighterBlue.getItems().indexOf(cbFighterBlue.getValue())).getId();
        int winnerId = 0;
        if (cbWinner.getValue() != null) {
            int idx = cbWinner.getItems().indexOf(cbWinner.getValue());
            if (idx >= 0 && idx < fighterList.size())
                winnerId = fighterList.get(idx).getId();
        }
        FightResult fr = new FightResult(eventId, 0, redId, blueId, winnerId, cbMethod.getValue(), round,
                dpFightDate.getValue(), txtNotes.getText().trim());
        try {
            serviceFightResult.ajouter(fr);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Fight result added.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FightResultList.fxml"));
            cbMethod.getScene().setRoot(root);
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
