package controllers;

import entities.FightResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceFightResult;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FightResultListController implements Initializable {

    @FXML
    private TableView<FightResult> tableFightResults;
    @FXML
    private TableColumn<FightResult, Integer> colId;
    @FXML
    private TableColumn<FightResult, Integer> colEventId;
    @FXML
    private TableColumn<FightResult, Integer> colFighterRedId;
    @FXML
    private TableColumn<FightResult, Integer> colFighterBlueId;
    @FXML
    private TableColumn<FightResult, Integer> colWinnerId;
    @FXML
    private TableColumn<FightResult, String> colMethod;
    @FXML
    private TableColumn<FightResult, LocalDate> colFightDate;

    private ServiceFightResult serviceFightResult;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceFightResult = new ServiceFightResult();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEventId.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colFighterRedId.setCellValueFactory(new PropertyValueFactory<>("fighterRedId"));
        colFighterBlueId.setCellValueFactory(new PropertyValueFactory<>("fighterBlueId"));
        colWinnerId.setCellValueFactory(new PropertyValueFactory<>("winnerId"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colFightDate.setCellValueFactory(new PropertyValueFactory<>("fightDate"));
        loadFightResults();
    }

    private void loadFightResults() {
        try {
            List<FightResult> results = serviceFightResult.recuperer();
            tableFightResults.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFightResult.fxml"));
            tableFightResults.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void navigateToEdit(ActionEvent event) {
        FightResult selected = tableFightResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a fight result to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFightResult.fxml"));
            Parent root = loader.load();
            UpdateFightResultController controller = loader.getController();
            controller.setFightResult(selected);
            tableFightResults.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void deleteFightResult(ActionEvent event) {
        FightResult selected = tableFightResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a fight result to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete fight result id=" + selected.getId() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceFightResult.supprimer(selected);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Deleted.");
                loadFightResults();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            }
        }
    }

    @FXML
    void navigateToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            tableFightResults.getScene().setRoot(root);
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
