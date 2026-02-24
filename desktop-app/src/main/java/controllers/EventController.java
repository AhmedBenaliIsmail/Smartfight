package controllers;

import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EventController implements Initializable {

    @FXML
    private TableView<Event> tableView;
    @FXML
    private TableColumn<Event, Integer> colId;
    @FXML
    private TableColumn<Event, String> colName;
    @FXML
    private TableColumn<Event, String> colDescription;
    @FXML
    private TableColumn<Event, LocalDate> colStartDate;
    @FXML
    private TableColumn<Event, LocalDate> colEndDate;
    @FXML
    private TableColumn<Event, String> colStatus;
    @FXML
    private TableColumn<Event, String> colVisibility;
    @FXML
    private TableColumn<Event, Integer> colCapacity;

    private ServiceEvent serviceEvent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceEvent = new ServiceEvent();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colVisibility.setCellValueFactory(new PropertyValueFactory<>("visibility"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        loadEvents();
    }

    private void loadEvents() {
        try {
            List<Event> events = serviceEvent.recuperer();
            ObservableList<Event> observableList = FXCollections.observableArrayList(events);
            tableView.setItems(observableList);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void navigateToAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEvent.fxml"));
            Parent root = loader.load();
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void navigateToUpdate(ActionEvent event) {
        Event selectedEvent = tableView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No selection");
            alert.setHeaderText("No event selected");
            alert.setContentText("Please select an event to update.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateEvent.fxml"));
            Parent root = loader.load();
            UpdateEventController controller = loader.getController();
            controller.setEvent(selectedEvent);
            tableView.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void deleteEvent(ActionEvent event) {
        Event selectedEvent = tableView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No selection");
            alert.setHeaderText("No event selected");
            alert.setContentText("Please select an event to delete.");
            alert.showAndWait();
            return;
        }
        try {
            serviceEvent.supprimer(selectedEvent);
            loadEvents();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
