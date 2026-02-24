package controllers;

import entities.Discipline;
import entities.Event;
import entities.Venue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ServiceDiscipline;
import services.ServiceEvent;
import services.ServiceVenue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class UpdateEventController {

    @FXML
    private TextField txtName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private ChoiceBox<String> cbStatus;
    @FXML
    private ChoiceBox<String> cbVisibility;
    @FXML
    private TextField txtCapacity;
    @FXML
    private ComboBox<String> cbVenue;
    @FXML
    private ComboBox<String> cbDiscipline;

    private ServiceEvent serviceEvent;
    private ServiceVenue serviceVenue;
    private ServiceDiscipline serviceDiscipline;
    private List<Venue> venueList;
    private List<Discipline> disciplineList;
    private Event currentEvent;

    @FXML
    public void initialize() {
        serviceEvent = new ServiceEvent();
        serviceVenue = new ServiceVenue();
        serviceDiscipline = new ServiceDiscipline();

        cbStatus.setItems(FXCollections.observableArrayList("SCHEDULED", "ONGOING", "COMPLETED", "CANCELLED"));
        cbVisibility.setItems(FXCollections.observableArrayList("PUBLIC", "PRIVATE"));

        loadVenues();
        loadDisciplines();
    }

    private void loadVenues() {
        try {
            venueList = serviceVenue.recuperer();
            for (Venue v : venueList) {
                cbVenue.getItems().add(v.getId() + " - " + v.getName() + " (" + v.getCity() + ")");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not load venues: " + e.getMessage());
        }
    }

    private void loadDisciplines() {
        try {
            disciplineList = serviceDiscipline.recuperer();
            for (Discipline d : disciplineList) {
                cbDiscipline.getItems().add(d.getId() + " - " + d.getName());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not load disciplines: " + e.getMessage());
        }
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        txtName.setText(event.getName());
        txtDescription.setText(event.getDescription());
        dpStartDate.setValue(event.getStartDate());
        dpEndDate.setValue(event.getEndDate());
        cbStatus.setValue(event.getStatus());
        cbVisibility.setValue(event.getVisibility());
        txtCapacity.setText(String.valueOf(event.getCapacity()));

        for (int i = 0; i < venueList.size(); i++) {
            if (venueList.get(i).getId() == event.getVenueId()) {
                cbVenue.setValue(cbVenue.getItems().get(i));
                break;
            }
        }
        for (int i = 0; i < disciplineList.size(); i++) {
            if (disciplineList.get(i).getId() == event.getDisciplineId()) {
                cbDiscipline.setValue(cbDiscipline.getItems().get(i));
                break;
            }
        }
    }

    @FXML
    void updateEvent(ActionEvent event) {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String status = cbStatus.getValue();
        String visibility = cbVisibility.getValue();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Event name is required.");
            return;
        }
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Start date and end date are required.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "End date cannot be before start date.");
            return;
        }
        if (txtCapacity.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity is required.");
            return;
        }
        int capacity;
        try {
            capacity = Integer.parseInt(txtCapacity.getText().trim());
            if (capacity <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be a positive number.");
            return;
        }

        int venueId = 0;
        int disciplineId = 0;

        if (cbVenue.getValue() != null && !cbVenue.getItems().isEmpty()) {
            int idx = cbVenue.getItems().indexOf(cbVenue.getValue());
            if (idx >= 0 && idx < venueList.size())
                venueId = venueList.get(idx).getId();
        }
        if (cbDiscipline.getValue() != null && !cbDiscipline.getItems().isEmpty()) {
            int idx = cbDiscipline.getItems().indexOf(cbDiscipline.getValue());
            if (idx >= 0 && idx < disciplineList.size())
                disciplineId = disciplineList.get(idx).getId();
        }

        currentEvent.setName(name);
        currentEvent.setDescription(description);
        currentEvent.setStartDate(startDate);
        currentEvent.setEndDate(endDate);
        currentEvent.setStatus(status);
        currentEvent.setVisibility(visibility);
        currentEvent.setCapacity(capacity);
        currentEvent.setVenueId(venueId);
        currentEvent.setDisciplineId(disciplineId);

        try {
            serviceEvent.modifier(currentEvent);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully.");
            navigateToList(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    void navigateToList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventList.fxml"));
            Parent root = loader.load();
            txtName.getScene().setRoot(root);
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
