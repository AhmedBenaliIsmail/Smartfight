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

public class UpdateFightResultController {

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
    private FightResult currentResult;

    @FXML
    public void initialize() {
        serviceFightResult = new ServiceFightResult();
        serviceEvent = new ServiceEvent();
        serviceFighter = new ServiceFighter();
        cbMethod.setItems(
                FXCollections.observableArrayList("KO", "TKO", "SUBMISSION", "DECISION", "DRAW", "NO_CONTEST"));
        try {
            eventList = serviceEvent.recuperer();
            for (Event e : eventList)
                cbEventId.getItems().add(e.getId() + " - " + e.getName());
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

    public void setFightResult(FightResult fr) {
        this.currentResult = fr;
        cbMethod.setValue(fr.getMethod());
        txtRoundEnded.setText(String.valueOf(fr.getRoundEnded()));
        dpFightDate.setValue(fr.getFightDate());
        txtNotes.setText(fr.getNotes());
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getId() == fr.getEventId()) {
                cbEventId.setValue(cbEventId.getItems().get(i));
                break;
            }
        }
        for (int i = 0; i < fighterList.size(); i++) {
            if (fighterList.get(i).getId() == fr.getFighterRedId()) {
                cbFighterRed.setValue(cbFighterRed.getItems().get(i));
            }
            if (fighterList.get(i).getId() == fr.getFighterBlueId()) {
                cbFighterBlue.setValue(cbFighterBlue.getItems().get(i));
            }
            if (fighterList.get(i).getId() == fr.getWinnerId()) {
                cbWinner.setValue(cbWinner.getItems().get(i));
            }
        }
    }

    @FXML
    void updateFightResult(ActionEvent event) {
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
        int eventId = currentResult.getEventId();
        if (cbEventId.getValue() != null && !eventList.isEmpty()) {
            int idx = cbEventId.getItems().indexOf(cbEventId.getValue());
            if (idx >= 0 && idx < eventList.size())
                eventId = eventList.get(idx).getId();
        }
        int redId = fighterList.get(cbFighterRed.getItems().indexOf(cbFighterRed.getValue())).getId();
        int blueId = fighterList.get(cbFighterBlue.getItems().indexOf(cbFighterBlue.getValue())).getId();
        int winnerId = 0;
        if (cbWinner.getValue() != null) {
            int idx = cbWinner.getItems().indexOf(cbWinner.getValue());
            if (idx >= 0 && idx < fighterList.size())
                winnerId = fighterList.get(idx).getId();
        }
        currentResult.setEventId(eventId);
        currentResult.setFighterRedId(redId);
        currentResult.setFighterBlueId(blueId);
        currentResult.setWinnerId(winnerId);
        currentResult.setMethod(cbMethod.getValue());
        currentResult.setRoundEnded(round);
        currentResult.setFightDate(dpFightDate.getValue());
        currentResult.setNotes(txtNotes.getText().trim());
        try {
            serviceFightResult.modifier(currentResult);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Fight result updated.");
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
