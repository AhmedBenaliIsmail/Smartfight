package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private Label lblTitle;

    private void navigate(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            lblTitle.getScene().setRoot(root);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot load: " + fxmlFile + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    // Module 1 — Events
    @FXML
    void openEvents(ActionEvent event) {
        navigate("EventList.fxml");
    }

    // Module 2 — Fighters & Coaches
    @FXML
    void openFighters(ActionEvent event) {
        navigate("FighterList.fxml");
    }

    @FXML
    void openCoaches(ActionEvent event) {
        navigate("CoachList.fxml");
    }

    // Module 3 — AI Matchmaking
    @FXML
    void openMatchmaking(ActionEvent event) {
        navigate("MatchProposalList.fxml");
    }

    @FXML
    void openRankings(ActionEvent event) {
        navigate("RankingList.fxml");
    }

    // Module 4 — Fight Results
    @FXML
    void openFightResults(ActionEvent event) {
        navigate("FightResultList.fxml");
    }

    // Module 5 — Users
    @FXML
    void openUsers(ActionEvent event) {
        navigate("UserList.fxml");
    }
}
