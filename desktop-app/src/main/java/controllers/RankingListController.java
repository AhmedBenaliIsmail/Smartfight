package controllers;

import entities.Ranking;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceRanking;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class RankingListController implements Initializable {

    @FXML
    private TableView<Ranking> tableRankings;
    @FXML
    private TableColumn<Ranking, Integer> colId;
    @FXML
    private TableColumn<Ranking, Integer> colFighterId;
    @FXML
    private TableColumn<Ranking, Integer> colDisciplineId;
    @FXML
    private TableColumn<Ranking, Integer> colRankPosition;
    @FXML
    private TableColumn<Ranking, Double> colPoints;
    @FXML
    private TableColumn<Ranking, String> colSeason;

    private ServiceRanking serviceRanking;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceRanking = new ServiceRanking();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFighterId.setCellValueFactory(new PropertyValueFactory<>("fighterId"));
        colDisciplineId.setCellValueFactory(new PropertyValueFactory<>("disciplineId"));
        colRankPosition.setCellValueFactory(new PropertyValueFactory<>("rankPosition"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colSeason.setCellValueFactory(new PropertyValueFactory<>("season"));
        loadRankings();
    }

    private void loadRankings() {
        try {
            List<Ranking> rankings = serviceRanking.recuperer();
            tableRankings.setItems(FXCollections.observableArrayList(rankings));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddRanking.fxml"));
            tableRankings.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void navigateToEdit(ActionEvent event) {
        Ranking selected = tableRankings.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a ranking to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateRanking.fxml"));
            Parent root = loader.load();
            UpdateRankingController controller = loader.getController();
            controller.setRanking(selected);
            tableRankings.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void deleteRanking(ActionEvent event) {
        Ranking selected = tableRankings.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a ranking to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete ranking for fighter id=" + selected.getFighterId() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceRanking.supprimer(selected);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Ranking deleted successfully.");
                loadRankings();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            }
        }
    }

    @FXML
    void navigateToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            tableRankings.getScene().setRoot(root);
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
