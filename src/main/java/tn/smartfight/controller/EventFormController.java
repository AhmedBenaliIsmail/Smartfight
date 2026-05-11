package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.smartfight.dao.EventDao;
import tn.smartfight.model.EventDetails;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EventFormController {
    private static final Logger LOG = Logger.getLogger(EventFormController.class.getName());

    @FXML private TextField eventNameField;
    @FXML private DatePicker eventDatePicker;
    @FXML private ComboBox<String> organizationCombo;
    @FXML private TextField venueField;
    @FXML private TextField cityField;
    @FXML private TextField countryField;
    @FXML private TextField seatCapacityField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> visibilityCombo;
    @FXML private CheckBox championsCheck;
    @FXML private Label statusLabel;

    private final EventDao dao = new EventDao();
    private Integer eventId;

    @FXML
    public void initialize() {
        organizationCombo.setItems(FXCollections.observableArrayList(
                "INDEPENDENT", "WBC", "WBA", "WBO", "IBF", "WBO", "UNDISPUTED"));
        organizationCombo.setValue("INDEPENDENT");

        statusCombo.setItems(FXCollections.observableArrayList(
                "SCHEDULED", "LIVE", "COMPLETED", "CANCELLED", "POSTPONED"));
        statusCombo.setValue("SCHEDULED");

        visibilityCombo.setItems(FXCollections.observableArrayList("PUBLIC", "PRIVATE"));
        visibilityCombo.setValue("PUBLIC");
    }

    public void setEvent(EventDetails e) {
        if (e == null) return;
        eventId = e.getId();
        eventNameField.setText(nullToEmpty(e.getEventName()));
        if (e.getEventDate() != null) eventDatePicker.setValue(e.getEventDate());
        organizationCombo.setValue(e.getOrganization() != null ? e.getOrganization() : "INDEPENDENT");
        venueField.setText(nullToEmpty(e.getVenue()));
        cityField.setText(nullToEmpty(e.getCity()));
        countryField.setText(nullToEmpty(e.getCountry()));
        seatCapacityField.setText(e.getSeatCapacity() != null ? String.valueOf(e.getSeatCapacity()) : "");
        statusCombo.setValue(e.getStatus() != null ? e.getStatus() : "SCHEDULED");
        visibilityCombo.setValue(e.getVisibility() != null ? e.getVisibility() : "PUBLIC");
        championsCheck.setSelected(e.isChampionsEvent());
    }

    @FXML
    private void onSave() {
        if (eventNameField.getText().isBlank()) {
            statusLabel.setText("Event name is required.");
            return;
        }
        EventDetails e = buildDetails();
        statusLabel.setText("Saving...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (eventId == null) dao.create(e);
                else { e.setId(eventId); dao.update(e); }
                return null;
            }
        };
        task.setOnSucceeded(ev -> close());
        task.setOnFailed(ev -> {
            LOG.log(Level.SEVERE, "Save event failed", task.getException());
            statusLabel.setText("Save failed: " + task.getException().getMessage());
        });
        new Thread(task, "event-save-task").start();
    }

    @FXML
    private void onCancel() { close(); }

    private EventDetails buildDetails() {
        EventDetails e = new EventDetails();
        e.setEventName(eventNameField.getText().trim());
        e.setEventDate(eventDatePicker.getValue());
        e.setOrganization(organizationCombo.getValue() != null ? organizationCombo.getValue() : "INDEPENDENT");
        e.setVenue(venueField.getText().trim());
        e.setCity(cityField.getText().trim());
        e.setCountry(countryField.getText().trim());
        String cap = seatCapacityField.getText().trim();
        if (!cap.isBlank()) { try { e.setSeatCapacity(Integer.parseInt(cap)); } catch (Exception ex) {} }
        e.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : "SCHEDULED");
        e.setVisibility(visibilityCombo.getValue() != null ? visibilityCombo.getValue() : "PUBLIC");
        e.setChampionsEvent(championsCheck.isSelected());
        return e;
    }

    private void close() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
