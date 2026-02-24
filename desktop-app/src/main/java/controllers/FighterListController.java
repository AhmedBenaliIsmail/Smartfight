package controllers;

import entities.Fighter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceFighter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FighterListController implements Initializable {

    @FXML
    private TableView<Fighter> tableFighters;
    @FXML
    private TableColumn<Fighter, Integer> colId;
    @FXML
    private TableColumn<Fighter, String> colNickname;
    @FXML
    private TableColumn<Fighter, String> colNationality;
    @FXML
    private TableColumn<Fighter, Integer> colWeightClassId;
    @FXML
    private TableColumn<Fighter, Integer> colWins;
    @FXML
    private TableColumn<Fighter, Integer> colLosses;
    @FXML
    private TableColumn<Fighter, Integer> colDraws;
    @FXML
    private TableColumn<Fighter, String> colStatus;

    private ServiceFighter serviceFighter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceFighter = new ServiceFighter();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNickname.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        colNationality.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        colWeightClassId.setCellValueFactory(new PropertyValueFactory<>("weightClassId"));
        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colLosses.setCellValueFactory(new PropertyValueFactory<>("losses"));
        colDraws.setCellValueFactory(new PropertyValueFactory<>("draws"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadFighters();
    }

    private void loadFighters() {
        try {
            List<Fighter> fighters = serviceFighter.recuperer();
            ObservableList<Fighter> list = FXCollections.observableArrayList(fighters);
            tableFighters.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToAdd(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFighter.fxml"));
            tableFighters.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void navigateToEdit(ActionEvent event) {
        Fighter selected = tableFighters.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a fighter to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFighter.fxml"));
            Parent root = loader.load();
            UpdateFighterController controller = loader.getController();
            controller.setFighter(selected);
            tableFighters.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    @FXML
    void deleteFighter(ActionEvent event) {
        Fighter selected = tableFighters.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a fighter to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete fighter: " + selected.getNickname() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceFighter.supprimer(selected);
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Fighter deleted successfully.");
                loadFighters();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            }
        }
    }

    @FXML
    void navigateToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            tableFighters.getScene().setRoot(root);
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
