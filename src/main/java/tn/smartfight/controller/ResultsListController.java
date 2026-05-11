package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.model.FightResult;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultsListController {
    private static final Logger LOG = Logger.getLogger(ResultsListController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> orgFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane resultsFlow;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();
    private final FightResultDao resultDao = new FightResultDao();

    /* Holds one EventCard's data: the event + its fights */
    private record EventCard(int eventId, String eventName, String organization,
                             String status, LocalDate eventDate, String venue, String city,
                             List<FightResult> fights) {}

    private List<EventCard> allCards = new ArrayList<>();

    @FXML
    public void initialize() {
        orgFilter.getSelectionModel().select("ALL");
        statusFilter.getSelectionModel().select("ALL");
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        load();
    }

    @FXML private void onFilter()       { applyFilter(); }
    @FXML private void onExportCsv()    { statusLabel.setText("CSV export not yet implemented."); }
    @FXML private void onPdfReport()    { statusLabel.setText("PDF export not yet implemented."); }

    @FXML private void onBoutStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/StatsWizard.fxml"));
            javafx.scene.Node pane = loader.load();
            var host = resultsFlow.getScene().lookup("#contentHost");
            if (host instanceof StackPane sp) sp.getChildren().setAll(pane);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Navigate to stats failed", e);
        }
    }

    @FXML private void onScheduleFight() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/ResultForm.fxml"));
            Scene scene = new Scene(loader.load(), 560, 720);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            ResultFormController ctrl = loader.getController();
            ctrl.setResult(null);
            Stage stage = new Stage();
            stage.setTitle("Schedule Fight");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            load();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open schedule form", e);
        }
    }

    private void load() {
        Task<List<EventCard>> task = new Task<>() {
            @Override protected List<EventCard> call() throws Exception {
                String sql =
                    "SELECT e.eventId, e.eventName, e.organization, e.status, " +
                    "       e.eventDate, e.venue, e.city, " +
                    "       fr.resultId, fr.fightNumber, fr.status AS fightStatus, " +
                    "       fr.methodOfVictory, fr.roundNumber, " +
                    "       CONCAT(f1.firstName,' ',f1.lastName) AS f1Name, " +
                    "       CONCAT(f2.firstName,' ',f2.lastName) AS f2Name, " +
                    "       CONCAT(fw.firstName,' ',fw.lastName) AS winnerName " +
                    "FROM events e " +
                    "LEFT JOIN fight_results fr ON fr.eventId = e.eventId " +
                    "LEFT JOIN fighters f1 ON f1.fighterId = fr.fighter1Id " +
                    "LEFT JOIN fighters f2 ON f2.fighterId = fr.fighter2Id " +
                    "LEFT JOIN fighters fw ON fw.fighterId = fr.winnerId " +
                    "ORDER BY e.eventDate DESC, e.eventId, fr.fightNumber";

                Map<Integer, EventCard> map = new LinkedHashMap<>();
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int eid = rs.getInt("eventId");
                        if (!map.containsKey(eid)) {
                            String dateStr = rs.getString("eventDate");
                            LocalDate date = null;
                            if (dateStr != null) {
                                try { date = rs.getDate("eventDate").toLocalDate(); } catch (Exception ignored) {}
                            }
                            map.put(eid, new EventCard(
                                    eid,
                                    rs.getString("eventName"),
                                    rs.getString("organization"),
                                    rs.getString("status"),
                                    date,
                                    rs.getString("venue"),
                                    rs.getString("city"),
                                    new ArrayList<>()
                            ));
                        }
                        int rid = rs.getInt("resultId");
                        if (rid != 0) {
                            FightResult fr = new FightResult();
                            fr.setResultId(rid);
                            fr.setFightNumber(rs.getInt("fightNumber"));
                            fr.setStatus(rs.getString("fightStatus"));
                            fr.setMethodOfVictory(rs.getString("methodOfVictory"));
                            fr.setRoundNumber(rs.getInt("roundNumber"));
                            fr.setFighter1Name(rs.getString("f1Name"));
                            fr.setFighter2Name(rs.getString("f2Name"));
                            fr.setWinnerName(rs.getString("winnerName"));
                            map.get(eid).fights().add(fr);
                        }
                    }
                }
                return new ArrayList<>(map.values());
            }
        };
        task.setOnSucceeded(e -> {
            allCards = task.getValue();
            applyFilter();
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load failed", task.getException());
            statusLabel.setText("Failed to load results.");
        });
        new Thread(task, "results-load").start();
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String org    = orgFilter.getValue();
        String status = statusFilter.getValue();

        resultsFlow.getChildren().clear();
        int count = 0;
        for (EventCard ec : allCards) {
            if (!"ALL".equals(org) && org != null && !org.equals(ec.organization())) continue;
            if (!"ALL".equals(status) && status != null) {
                boolean hasMatchingFight = ec.fights().stream()
                        .anyMatch(f -> status.equals(f.getStatus()));
                if (!hasMatchingFight && !status.equals(ec.status())) continue;
            }
            if (!search.isEmpty()) {
                String hay = (nvl(ec.eventName()) + " " + nvl(ec.venue())
                        + " " + nvl(ec.city()) + " " +
                        ec.fights().stream()
                                .map(f -> nvl(f.getFighter1Name()) + " " + nvl(f.getFighter2Name()))
                                .reduce("", (a, b) -> a + " " + b)).toLowerCase();
                if (!hay.contains(search)) continue;
            }
            resultsFlow.getChildren().add(buildEventCard(ec));
            count++;
        }
        statusLabel.setText(count + " event" + (count == 1 ? "" : "s") + " shown");
    }

    private VBox buildEventCard(EventCard ec) {
        VBox card = new VBox(12);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(340);

        /* Top row: org badge + status */
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label orgBadge = new Label(ec.organization() != null ? ec.organization() : "STANDARD EVENT");
        orgBadge.getStyleClass().add(orgBadgeCss(ec.organization()));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label statusBadge = new Label(ec.status() != null ? ec.status() : "UNKNOWN");
        statusBadge.getStyleClass().add(statusCss(ec.status()));
        topRow.getChildren().addAll(orgBadge, sp, statusBadge);

        /* Event name */
        Label name = new Label(ec.eventName() != null ? ec.eventName().toUpperCase() : "UNNAMED EVENT");
        name.setStyle("-fx-font-size:15;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
        name.setWrapText(true);

        /* Date + venue */
        String dateStr = ec.eventDate() != null ? "📅  " + ec.eventDate() : "📅  TBD";
        Label dateLbl = new Label(dateStr);
        dateLbl.setStyle("-fx-font-size:12;-fx-text-fill:#9ca3af;");

        String venueStr = "📍  " + nvl(ec.venue()) + (ec.city() != null ? ", " + ec.city() : "");
        Label venueLbl = new Label(venueStr);
        venueLbl.setStyle("-fx-font-size:12;-fx-text-fill:#9ca3af;");

        /* Separator */
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#2d2d2d;");

        /* Fights list */
        VBox fightsBox = new VBox(8);
        if (ec.fights().isEmpty()) {
            Label noData = new Label("NO DATA");
            noData.setStyle("-fx-text-fill:#374151;-fx-font-size:12;-fx-font-weight:bold;");
            fightsBox.getChildren().add(noData);
        } else {
            for (FightResult fr : ec.fights()) {
                fightsBox.getChildren().add(buildFightRow(fr));
            }
        }

        /* Action buttons */
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        Label boutCount = new Label(ec.fights().size() + " BOUT" + (ec.fights().size() == 1 ? "" : "S"));
        boutCount.setStyle("-fx-text-fill:#6b7280;-fx-font-size:11;-fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addBtn = new Button("✏ ENTER RESULT");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> openResultForm(null));
        actions.getChildren().addAll(boutCount, spacer, addBtn);

        card.getChildren().addAll(topRow, name, dateLbl, venueLbl, sep, fightsBox, actions);
        return card;
    }

    private HBox buildFightRow(FightResult fr) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#111111;-fx-background-radius:6;-fx-padding:8 10;");

        Label boutLbl = new Label("B" + fr.getFightNumber());
        boutLbl.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-font-size:10;" +
                "-fx-font-weight:bold;-fx-background-radius:12;-fx-padding:2 6;");

        String vsText = nvl(fr.getFighter1Name()) + " vs " + nvl(fr.getFighter2Name());
        Label vsLbl = new Label(vsText);
        vsLbl.setStyle("-fx-text-fill:#ffffff;-fx-font-size:12;-fx-font-weight:bold;");
        HBox.setHgrow(vsLbl, Priority.ALWAYS);
        vsLbl.setMaxWidth(Double.MAX_VALUE);

        Label fightStatus = new Label(fr.getStatus() != null ? fr.getStatus() : "PENDING");
        fightStatus.getStyleClass().add(statusCss(fr.getStatus()));

        Button enterBtn = new Button("✏");
        enterBtn.getStyleClass().add("btn-icon");
        enterBtn.setStyle("-fx-padding:3 7;-fx-font-size:11;");
        enterBtn.setOnAction(e -> openResultForm(fr));

        Button delBtn = new Button("🗑");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-padding:3 7;-fx-font-size:11;");
        delBtn.setOnAction(e -> deleteFight(fr));

        row.getChildren().addAll(boutLbl, vsLbl, fightStatus, enterBtn, delBtn);
        return row;
    }

    private void openResultForm(FightResult fr) {
        try {
            FightResult full = fr != null ? resultDao.getById(fr.getResultId()) : null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/ResultForm.fxml"));
            Scene scene = new Scene(loader.load(), 560, 720);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            ResultFormController ctrl = loader.getController();
            ctrl.setResult(full != null ? full : fr);
            Stage stage = new Stage();
            stage.setTitle(fr == null ? "Schedule Fight" : "Enter Fight Result");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            load();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open ResultForm", e);
        }
    }

    private void deleteFight(FightResult fr) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Bout #" + fr.getFightNumber() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                resultDao.deleteById(fr.getResultId());
                load();
            }
        });
    }

    private static String orgBadgeCss(String org) {
        if (org == null) return "badge-gray";
        return switch (org.toUpperCase()) {
            case "WBC"        -> "badge-green";
            case "WBA"        -> "badge-blue";
            case "IBF"        -> "badge-red";
            case "WBO"        -> "badge-purple";
            case "UNDISPUTED" -> "badge-gold";
            default           -> "badge-gray";
        };
    }

    private static String statusCss(String s) {
        if (s == null) return "badge-gray";
        return switch (s.toUpperCase()) {
            case "COMPLETED"  -> "badge-green";
            case "PENDING"    -> "badge-amber";
            case "LIVE"       -> "badge-orange";
            case "SCHEDULED"  -> "badge-gray";
            default           -> "badge-gray";
        };
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}
