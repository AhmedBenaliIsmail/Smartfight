package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.EventDao;
import tn.smartfight.model.Event;
import tn.smartfight.model.EventDetails;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventsListController {
    private static final Logger LOG = Logger.getLogger(EventsListController.class.getName());

    @FXML private TextField searchField;
    @FXML private FlowPane eventsFlow;
    @FXML private Label statusLabel;
    @FXML private Label lblTotal;
    @FXML private Label lblUpcoming;
    @FXML private Label lblPast;
    @FXML private Label lblChamps;

    private final EventDao dao = new EventDao();
    private final DataSource ds = DBConnection.getDataSource();
    private List<Event> allEvents;

    @FXML
    public void initialize() { load(); }

    @FXML private void onFilter() { renderCards(); }

    @FXML private void onCreateEvent() { openForm(null); }

    private void load() {
        statusLabel.setText("Loading...");
        Task<List<Event>> task = new Task<>() {
            @Override protected List<Event> call() { return dao.findAll(); }
        };
        task.setOnSucceeded(e -> {
            allEvents = task.getValue();
            updateStats();
            renderCards();
        });
        task.setOnFailed(e -> statusLabel.setText("Load failed."));
        new Thread(task, "events-load").start();
    }

    private void updateStats() {
        if (allEvents == null) return;
        long total    = allEvents.size();
        long upcoming = allEvents.stream().filter(ev -> ev.getEventDate() != null
                && ev.getEventDate().isAfter(LocalDate.now().minusDays(1))).count();
        long past     = total - upcoming;
        long champs   = allEvents.stream().filter(Event::isChampionsEvent).count();
        lblTotal.setText(String.valueOf(total));
        lblUpcoming.setText(String.valueOf(upcoming));
        lblPast.setText(String.valueOf(past));
        lblChamps.setText(String.valueOf(champs));
    }

    private void renderCards() {
        if (allEvents == null) return;
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        eventsFlow.getChildren().clear();
        int count = 0;
        for (Event ev : allEvents) {
            if (!search.isEmpty()) {
                String hay = ((ev.getEventName() != null ? ev.getEventName() : "")
                        + " " + (ev.getVenue() != null ? ev.getVenue() : "")
                        + " " + (ev.getCity() != null ? ev.getCity() : "")).toLowerCase();
                if (!hay.contains(search)) continue;
            }
            eventsFlow.getChildren().add(buildEventCard(ev));
            count++;
        }
        statusLabel.setText(count + " events");
    }

    private VBox buildEventCard(Event ev) {
        VBox card = new VBox(12);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(320);

        /* Organization badge */
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label orgBadge = new Label(ev.getOrganization() != null ? ev.getOrganization() : "INDEPENDENT");
        orgBadge.getStyleClass().add(orgBadgeCss(ev.getOrganization()));

        if ("LIVE".equalsIgnoreCase(ev.getStatus())) {
            Label liveBadge = new Label("● LIVE");
            liveBadge.getStyleClass().add("badge-orange");
            topRow.getChildren().addAll(orgBadge, liveBadge);
        } else {
            topRow.getChildren().add(orgBadge);
        }

        /* Event name */
        Label name = new Label(ev.getEventName() != null ? ev.getEventName().toUpperCase() : "UNNAMED EVENT");
        name.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
        name.setWrapText(true);

        /* Date */
        String dateStr = ev.getEventDate() != null ? "📅  " + ev.getEventDate().toString() : "📅  TBD";
        Label dateLbl = new Label(dateStr);
        dateLbl.setStyle("-fx-font-size:12;-fx-text-fill:#9ca3af;");

        /* Venue */
        String venueStr = "📍  " + orEmpty(ev.getVenue()) + (ev.getCity() != null ? ", " + ev.getCity() : "");
        Label venueLbl = new Label(venueStr);
        venueLbl.setStyle("-fx-font-size:12;-fx-text-fill:#9ca3af;");

        /* Sanctioning progress */
        Label sanctionLbl = new Label("SANCTIONING PROGRESS: ?/3 BOUTS");
        sanctionLbl.setStyle("-fx-font-size:10;-fx-text-fill:#6b7280;-fx-font-weight:bold;");
        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar");
        loadBoutCount(ev.getEventId(), pb, sanctionLbl);

        /* Buttons row */
        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_LEFT);
        Button manageBtn = new Button("MANAGE CARD");
        manageBtn.getStyleClass().add("btn-primary");
        manageBtn.setOnAction(e -> openManageCard(ev));
        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("btn-icon");
        editBtn.setOnAction(e -> openForm(ev));
        Button delBtn = new Button("🗑");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-padding:4 8;");
        delBtn.setOnAction(e -> deleteEvent(ev));
        btns.getChildren().addAll(manageBtn, editBtn, delBtn);

        card.getChildren().addAll(topRow, name, dateLbl, venueLbl, sanctionLbl, pb, btns);
        return card;
    }

    private void loadBoutCount(int eventId, ProgressBar pb, Label label) {
        Task<Integer> t = new Task<>() {
            @Override protected Integer call() throws Exception {
                String sql = "SELECT COUNT(*) FROM fight_results WHERE eventId=?";
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? rs.getInt(1) : 0;
                    }
                }
            }
        };
        t.setOnSucceeded(e -> {
            int cnt = t.getValue();
            label.setText("SANCTIONING PROGRESS: " + cnt + "/3 BOUTS");
            pb.setProgress(Math.min(cnt / 3.0, 1.0));
        });
        new Thread(t, "bout-count-" + eventId).start();
    }

    private void openManageCard(Event ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/ManageCard.fxml"));
            javafx.scene.Node pane = loader.load();
            ManageCardController ctrl = loader.getController();
            ctrl.setEvent(ev);
            var host = eventsFlow.getScene().lookup("#contentHost");
            if (host instanceof StackPane sp) {
                sp.getChildren().setAll(pane);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open ManageCard", e);
        }
    }

    private void openForm(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/EventForm.fxml"));
            Scene scene = new Scene(loader.load(), 520, 600);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            EventFormController ctrl = loader.getController();
            if (event != null) {
                EventDetails ed = new EventDetails();
                ed.setId(event.getEventId());
                ed.setEventName(event.getEventName());
                ed.setEventDate(event.getEventDate());
                ed.setOrganization(event.getOrganization());
                ed.setVenue(event.getVenue());
                ed.setCity(event.getCity());
                ed.setCountry(event.getCountry());
                ed.setSeatCapacity(event.getSeatCapacity());
                ed.setStatus(event.getStatus());
                ed.setVisibility(event.getVisibility());
                ed.setChampionsEvent(event.isChampionsEvent());
                ctrl.setEvent(ed);
            }
            Stage stage = new Stage();
            stage.setTitle(event == null ? "Create Event" : "Edit Event");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            load();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open EventForm", e);
        }
    }

    private void deleteEvent(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete event: " + event.getEventName() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                dao.deleteById(event.getEventId());
                load();
            }
        });
    }

    private String orgBadgeCss(String org) {
        if (org == null) return "badge-gray";
        return switch (org.toUpperCase()) {
            case "WBC"         -> "badge-green";
            case "WBA"         -> "badge-blue";
            case "IBF"         -> "badge-red";
            case "WBO"         -> "badge-purple";
            case "UNDISPUTED"  -> "badge-gold";
            default            -> "badge-gray";
        };
    }

    private static String orEmpty(String s) { return s != null ? s : ""; }
}
