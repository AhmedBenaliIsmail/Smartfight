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

public class UpdateRankingController {

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
    private Ranking currentRanking;

    @FXML
    public void initialize() {
        serviceRanking = new ServiceRanking();
        serviceFighter = new ServiceFighter();
        serviceDiscipline = new ServiceDiscipline();
        try {
            fighterList = serviceFighter.recuperer();
            for (Fighter f : fighterList)
                cbFighterId.getItems().add(f.getId() + " - " + f.getNickname());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
        try {
            disciplineList = serviceDiscipline.recuperer();
            for (Discipline d : disciplineList)
                cbDisciplineId.getItems().add(d.getId() + " - " + d.getName());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    public void setRanking(Ranking ranking) {
        this.currentRanking = ranking;
        txtRankPosition.setText(String.valueOf(ranking.getRankPosition()));
        txtPoints.setText(String.valueOf(ranking.getPoints()));
        txtSeason.setText(ranking.getSeason());
        for (int i = 0; i < fighterList.size(); i++) {
            if (fighterList.get(i).getId() == ranking.getFighterId()) {
                cbFighterId.setValue(cbFighterId.getItems().get(i));
                break;
            }
        }
        for (int i = 0; i < disciplineList.size(); i++) {
            if (disciplineList.get(i).getId() == ranking.getDisciplineId()) {
                cbDisciplineId.setValue(cbDisciplineId.getItems().get(i));
                break;
            }
        }
    }

    @FXML
    void updateRanking(ActionEvent event) {
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
        int fighterId = currentRanking.getFighterId();
        if (cbFighterId.getValue() != null && !fighterList.isEmpty()) {
            int idx = cbFighterId.getItems().indexOf(cbFighterId.getValue());
            if (idx >= 0 && idx < fighterList.size())
                fighterId = fighterList.get(idx).getId();
        }
        int disciplineId = currentRanking.getDisciplineId();
        if (cbDisciplineId.getValue() != null && !disciplineList.isEmpty()) {
            int idx = cbDisciplineId.getItems().indexOf(cbDisciplineId.getValue());
            if (idx >= 0 && idx < disciplineList.size())
                disciplineId = disciplineList.get(idx).getId();
        }
        currentRanking.setFighterId(fighterId);
        currentRanking.setDisciplineId(disciplineId);
        currentRanking.setRankPosition(rankPos);
        currentRanking.setPoints(pts);
        currentRanking.setSeason(txtSeason.getText().trim());
        try {
            serviceRanking.modifier(currentRanking);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Ranking updated.");
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
