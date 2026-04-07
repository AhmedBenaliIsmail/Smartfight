package tn.smartfight.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.models.Event;
import tn.smartfight.services.EventService;

import java.time.LocalDate;
import java.util.List;

public class EventsAdminController {

    @FXML private TextField           tfSearch;
    @FXML private TableView<Event>    table;
    @FXML private TableColumn<Event, String> colId;
    @FXML private TableColumn<Event, String> colName;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colVenue;
    @FXML private TableColumn<Event, String> colStatus;
    @FXML private TableColumn<Event, String> colCapacity;
    @FXML private TableColumn<Event, String> colVisibility;
    @FXML private Label lblStatus;

    private final EventService service = new EventService();
    private final ObservableList<Event> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getStartDate() != null ? c.getValue().getStartDate().toString() : "—"));
        colVenue.setCellValueFactory(c -> {
            String v = c.getValue().getVenueName();
            String city = c.getValue().getVenueCity();
            String display = (v != null ? v : "—") + (city != null ? ", " + city : "");
            return new SimpleStringProperty(display);
        });
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colCapacity.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacity())));
        colVisibility.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getVisibility() != null ? c.getValue().getVisibility() : "PUBLIC"));

        table.setItems(data);
        tfSearch.textProperty().addListener((obs, o, n) -> filter(n));
        loadData();
    }

    @FXML private void onAddClicked()  { showDialog(null); }

    @FXML
    private void onEditClicked() {
        Event sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select an event to edit."); return; }
        showDialog(sel);
    }

    @FXML
    private void onDeleteClicked() {
        Event sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select an event to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete event \"" + sel.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            if (service.deleteEvent(sel.getId())) { loadData(); setStatus("Event deleted."); }
            else setStatus("Delete failed — event may have bookings or fight results.");
        });
    }

    @FXML private void onRefreshClicked() { loadData(); }

    private void loadData() {
        List<Event> events = service.getAllEvents();
        data.setAll(events);
        setStatus(events.size() + " events");
    }

    private void filter(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lower = q.toLowerCase();
        ObservableList<Event> filtered = FXCollections.observableArrayList();
        for (Event e : data) {
            if (e.getName().toLowerCase().contains(lower) ||
                (e.getVenueName() != null && e.getVenueName().toLowerCase().contains(lower)) ||
                (e.getStatus() != null && e.getStatus().toLowerCase().contains(lower))) {
                filtered.add(e);
            }
        }
        table.setItems(filtered);
    }

    private void showDialog(Event existing) {
        boolean isEdit = existing != null;
        Dialog<Event> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit Event" : "Add Event");

        DialogPane pane = dlg.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color:#16161b;");

        TextField tfName         = field("Event Name *");
        DatePicker dpStart       = new DatePicker();
        DatePicker dpEnd         = new DatePicker();
        TextField tfCapacity     = field("Capacity");
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        cbStatus.setValue("SCHEDULED");
        ComboBox<String> cbVisibility = new ComboBox<>();
        cbVisibility.getItems().addAll("PUBLIC", "PRIVATE");
        cbVisibility.setValue("PUBLIC");

        dpStart.setStyle("-fx-background-color:#2a2a3e;");
        dpEnd.setStyle("-fx-background-color:#2a2a3e;");
        cbStatus.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;");
        cbVisibility.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;");

        if (isEdit) {
            tfName.setText(existing.getName());
            if (existing.getStartDate() != null) dpStart.setValue(existing.getStartDate());
            if (existing.getEndDate()   != null) dpEnd.setValue(existing.getEndDate());
            tfCapacity.setText(String.valueOf(existing.getCapacity()));
            if (existing.getStatus()     != null) cbStatus.setValue(existing.getStatus());
            if (existing.getVisibility() != null) cbVisibility.setValue(existing.getVisibility());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:#16161b;");
        addRow(grid, 0, "Name *",      tfName);
        addRow(grid, 1, "Start Date",  dpStart);
        addRow(grid, 2, "End Date",    dpEnd);
        addRow(grid, 3, "Capacity",    tfCapacity);
        addRow(grid, 4, "Status",      cbStatus);
        addRow(grid, 5, "Visibility",  cbVisibility);
        pane.setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (tfName.getText().isBlank()) { setStatus("Name is required."); return null; }
            Event e = isEdit ? existing : new Event();
            e.setName(tfName.getText().trim());
            e.setStartDate(dpStart.getValue());
            e.setEndDate(dpEnd.getValue());
            e.setCapacity(parseInt(tfCapacity.getText()));
            e.setStatus(cbStatus.getValue());
            e.setVisibility(cbVisibility.getValue());
            return e;
        });

        dlg.showAndWait().ifPresent(e -> {
            boolean ok = isEdit ? service.updateEvent(e) : service.createEvent(e);
            if (ok) { loadData(); setStatus(isEdit ? "Event updated." : "Event created."); }
            else      setStatus("Save failed.");
        });
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;-fx-prompt-text-fill:#888;-fx-background-radius:4;-fx-padding:6 10;");
        return tf;
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node ctrl) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#aaa;-fx-font-size:11px;");
        g.add(lbl, 0, row);
        g.add(ctrl, 1, row);
        GridPane.setHgrow(ctrl, Priority.ALWAYS);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }
}
