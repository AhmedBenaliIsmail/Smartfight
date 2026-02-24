package controllers;

import entities.MatchProposal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.AIMatchmakingService;
import services.ServiceMatchProposal;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MatchProposalListController implements Initializable {

    @FXML
    private TableView<MatchProposal> tableProposals;
    @FXML
    private TableColumn<MatchProposal, Integer> colId;
    @FXML
    private TableColumn<MatchProposal, Integer> colEventId;
    @FXML
    private TableColumn<MatchProposal, Integer> colFighter1Id;
    @FXML
    private TableColumn<MatchProposal, Integer> colFighter2Id;
    @FXML
    private TableColumn<MatchProposal, Double> colCompatibility;
    @FXML
    private TableColumn<MatchProposal, String> colStatus;
    @FXML
    private TextField txtEventId;

    private ServiceMatchProposal serviceProposal;
    private AIMatchmakingService aiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceProposal = new ServiceMatchProposal();
        aiService = new AIMatchmakingService();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEventId.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        colFighter1Id.setCellValueFactory(new PropertyValueFactory<>("fighter1Id"));
        colFighter2Id.setCellValueFactory(new PropertyValueFactory<>("fighter2Id"));
        colCompatibility.setCellValueFactory(new PropertyValueFactory<>("compatibility"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadAllProposals();
    }

    private void loadAllProposals() {
        try {
            List<MatchProposal> list = serviceProposal.recuperer();
            tableProposals.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void generateProposals(ActionEvent event) {
        String idText = txtEventId.getText().trim();
        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Required", "Enter an Event ID.");
            return;
        }
        int eventId;
        try {
            eventId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Event ID must be a number.");
            return;
        }
        try {
            List<MatchProposal> generated = aiService.generateProposals(eventId);
            showAlert(Alert.AlertType.INFORMATION, "AI Matchmaking",
                    "Generated " + generated.size() + " proposals for event " + eventId + ".");
            loadAllProposals();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void acceptProposal(ActionEvent event) {
        updateStatus("ACCEPTED");
    }

    @FXML
    void rejectProposal(ActionEvent event) {
        updateStatus("REJECTED");
    }

    private void updateStatus(String newStatus) {
        MatchProposal selected = tableProposals.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a proposal.");
            return;
        }
        selected.setStatus(newStatus);
        try {
            serviceProposal.modifier(selected);
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Proposal marked as " + newStatus + ".");
            loadAllProposals();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    @FXML
    void navigateToMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            tableProposals.getScene().setRoot(root);
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
