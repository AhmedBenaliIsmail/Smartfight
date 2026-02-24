package controllers;

import entities.Discipline;
import entities.Fighter;
import entities.Ranking;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceDiscipline;
import services.ServiceFighter;
import services.ServiceRanking;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AddRankingController {

    @FXML
    private ComboBox<String> cbFighterId;
    @FXML
    private ComboBox<String> cbDisciplineId;
    @FXML
    private TextField txtRankPosition;
    @FXML
    private TextField txtPoints;
    @FXML
    private TextField txtSeason;

    private ServiceRanking serviceRanking;
    private ServiceFighter serviceFighter;
    private ServiceDiscipline serviceDiscipline;
    private List<Fighter> fighterList;
    private List<Discipline> disciplineList;

    @FXML
    public void initialize() {
        serviceRanking = new ServiceRanking();
        serviceFighter = new ServiceFighter();
        serviceDiscipline = new ServiceDiscipline();
        try {
            fighterList = serviceFighter.recuperer();
            for (Fighter f : fighterList)
                cbFighterId.getItems().add(f.getId() + " - " + f.getNickname());
            if (!cbFighterId.getItems().isEmpty())
                cbFighterId.setValue(cbFighterId.getItems().get(0));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
        try {
            disciplineList = serviceDiscipline.recuperer();
            for (Discipline d : disciplineList)
                cbDisciplineId.getItems().add(d.getId() + " - " + d.getName());
            if (!cbDisciplineId.getItems().isEmpty())
                cbDisciplineId.setValue(cbDisciplineId.getItems().get(0));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void saveRanking(ActionEvent event) {
        if (cbFighterId.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Fighter is required.");
            return;
        }
        int rankPos = 0;
        double pts = 0;
        try {
            rankPos = Integer.parseInt(txtRankPosition.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Rank position must be a number.");
            return;
        }
        try {
            pts = Double.parseDouble(txtPoints.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Points must be a number.");
            return;
        }
        int fighterId = fighterList.get(cbFighterId.getItems().indexOf(cbFighterId.getValue())).getId();
        int disciplineId = 0;
        if (cbDisciplineId.getValue() != null && !disciplineList.isEmpty()) {
            int idx = cbDisciplineId.getItems().indexOf(cbDisciplineId.getValue());
            if (idx >= 0 && idx < disciplineList.size())
                disciplineId = disciplineList.get(idx).getId();
        }
        Ranking r = new Ranking(fighterId, disciplineId, rankPos, pts, txtSeason.getText().trim());
        try {
            serviceRanking.ajouter(r);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Ranking added.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/RankingList.fxml"));
            txtSeason.getScene().setRoot(root);
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
