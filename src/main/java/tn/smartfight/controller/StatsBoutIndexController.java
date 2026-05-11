package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.dao.FightStatisticDao;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.FightStatistic;
import tn.smartfight.util.AccessGuard;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Bout Intelligence Hub — lists all fights that have CompuBox stats.
 * Mirrors the web app's /stats index page.
 */
public class StatsBoutIndexController {
    private static final Logger LOG = Logger.getLogger(StatsBoutIndexController.class.getName());

    @FXML private TextField searchField;
    @FXML private VBox      listContainer;
    @FXML private Label     statusLabel;
    @FXML private ToggleButton btnSortDate;
    @FXML private ToggleButton btnSortAlpha;

    private final FightStatisticDao statDao   = new FightStatisticDao();
    private final FightResultDao    resultDao = new FightResultDao();

    /** Groups fightId → [statF1, statF2] (aggregated totals) */
    private Map<Integer, List<FightStatistic>> groups = new LinkedHashMap<>();
    /** Corresponding fight results */
    private Map<Integer, FightResult> fightMap = new LinkedHashMap<>();

    private boolean sortByDate = true;

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, o, n) -> renderCards(n.trim().toLowerCase()));
        loadData();
    }

    @FXML private void onSortDate() {
        sortByDate = true;
        styleToggle(btnSortDate, btnSortAlpha);
        renderCards(searchField.getText().trim().toLowerCase());
    }

    @FXML private void onSortAlpha() {
        sortByDate = false;
        styleToggle(btnSortAlpha, btnSortDate);
        renderCards(searchField.getText().trim().toLowerCase());
    }

    @FXML private void onNewStats() {
        navigate("/tn/smartfight/views/admin/StatsWizard.fxml");
    }

    private void loadData() {
        statusLabel.setText("Loading archive...");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                List<FightStatistic> aggregated = statDao.findAggregatedByFight();
                List<FightResult>    allResults = resultDao.findAll();
                Map<Integer, FightResult> byId  = new HashMap<>();
                for (FightResult fr : allResults) byId.put(fr.getResultId(), fr);

                Map<Integer, List<FightStatistic>> g = new LinkedHashMap<>();
                for (FightStatistic s : aggregated) {
                    g.computeIfAbsent(s.getFightId(), k -> new ArrayList<>()).add(s);
                }
                groups  = g;
                fightMap = byId;
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            statusLabel.setText("");
            renderCards("");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Stats index load failed", task.getException());
            statusLabel.setText("Failed to load archive: " + task.getException().getMessage());
        });
        new Thread(task, "stats-index-load").start();
    }

    private void renderCards(String filter) {
        listContainer.getChildren().clear();

        List<Integer> fightIds = new ArrayList<>(groups.keySet());

        // Apply filter
        if (!filter.isBlank()) {
            fightIds = fightIds.stream().filter(id -> {
                FightResult fr = fightMap.get(id);
                String haystack = buildHaystack(fr, groups.get(id));
                return haystack.contains(filter);
            }).collect(Collectors.toList());
        }

        // Sort
        if (sortByDate) {
            fightIds.sort((a, b) -> b - a); // higher resultId = more recent
        } else {
            fightIds.sort(Comparator.comparing(id -> {
                FightResult fr = fightMap.get(id);
                return fr != null && fr.getEventName() != null ? fr.getEventName().toLowerCase() : "";
            }));
        }

        if (fightIds.isEmpty()) {
            Label empty = new Label("NO DATA DETECTED — SYSTEM AWAITING ARCHIVE SYNCHRONIZATION");
            empty.setStyle("-fx-text-fill:#3f3f46;-fx-font-size:14;-fx-font-weight:bold;-fx-letter-spacing:2;-fx-padding:60 0;");
            listContainer.getChildren().add(empty);
            return;
        }

        for (int fightId : fightIds) {
            FightResult fr  = fightMap.get(fightId);
            List<FightStatistic> pair = groups.get(fightId);
            FightStatistic s1 = pair.size() > 0 ? pair.get(0) : null;
            FightStatistic s2 = pair.size() > 1 ? pair.get(1) : null;
            listContainer.getChildren().add(buildCard(fightId, fr, s1, s2));
        }
    }

    private HBox buildCard(int fightId, FightResult fr, FightStatistic s1, FightStatistic s2) {
        HBox card = new HBox(0);
        card.setStyle("-fx-background-color:#121214;-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:1;" +
                "-fx-border-radius:12;-fx-background-radius:12;-fx-min-height:90;");

        // Left strip
        Region strip = new Region();
        strip.setMinWidth(4); strip.setMaxWidth(4);
        strip.setStyle("-fx-background-color:#27272a;-fx-background-radius:12 0 0 12;");
        card.getChildren().add(strip);

        // Main content
        HBox content = new HBox(24);
        content.setPadding(new Insets(18, 28, 18, 24));
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        card.getChildren().add(content);

        // ── Event meta (left column) ──────────────────────
        VBox metaCol = new VBox(4);
        metaCol.setMinWidth(220); metaCol.setMaxWidth(220);
        String archiveId = "ARCHIVE_ID: #" + (fightId + 1000);
        String eventName = fr != null && fr.getEventName() != null ? fr.getEventName().toUpperCase() : "UNKNOWN EVENT";
        String dateStr   = fr != null && fr.getFightDate() != null
                ? fr.getFightDate().toLocalDate().toString() : "N/A";

        Label idLbl   = new Label(archiveId);
        idLbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#52525b;-fx-letter-spacing:2;");
        Label evLbl   = new Label(eventName);
        evLbl.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:white;");
        evLbl.setWrapText(true);
        Label dateLbl = new Label("📅 " + dateStr);
        dateLbl.setStyle("-fx-font-size:11;-fx-text-fill:#71717a;-fx-font-weight:600;");
        metaCol.getChildren().addAll(idLbl, evLbl, dateLbl);

        // ── Matchup center ────────────────────────────────
        HBox matchup = new HBox(20);
        HBox.setHgrow(matchup, Priority.ALWAYS);
        matchup.setAlignment(javafx.geometry.Pos.CENTER);

        VBox f1Box = new VBox(2);
        f1Box.setAlignment(javafx.geometry.Pos.CENTER);
        String f1Last = s1 != null && s1.getFighterName() != null ? lastName(s1.getFighterName()) : "N/A";
        String f1Acc  = s1 != null ? String.format("%.0f%% ACC", s1.getPunchAccuracy()) : "0% ACC";
        Label f1Name  = new Label(f1Last.toUpperCase());
        f1Name.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:white;");
        Label f1AccLbl = new Label(f1Acc);
        f1AccLbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#dc2626;-fx-letter-spacing:1;");
        f1Box.getChildren().addAll(f1Name, f1AccLbl);

        VBox centerStats = new VBox(2);
        centerStats.setAlignment(javafx.geometry.Pos.CENTER);
        String landed1 = s1 != null ? String.valueOf(s1.getPunchesLanded()) : "0";
        String landed2 = s2 != null ? String.valueOf(s2.getPunchesLanded()) : "0";
        HBox landedRow = new HBox(12);
        landedRow.setAlignment(javafx.geometry.Pos.CENTER);
        Label v1 = new Label(landed1); v1.setStyle("-fx-font-size:24;-fx-font-weight:bold;-fx-text-fill:white;");
        Label lbl = new Label("LANDED"); lbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#52525b;-fx-letter-spacing:2;");
        Label v2 = new Label(landed2); v2.setStyle("-fx-font-size:24;-fx-font-weight:bold;-fx-text-fill:white;");
        landedRow.getChildren().addAll(v1, lbl, v2);
        Label vs = new Label("VS"); vs.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:#27272a;");
        centerStats.getChildren().addAll(landedRow, vs);

        VBox f2Box = new VBox(2);
        f2Box.setAlignment(javafx.geometry.Pos.CENTER);
        String f2Last = s2 != null && s2.getFighterName() != null ? lastName(s2.getFighterName()) : "N/A";
        String f2Acc  = s2 != null ? String.format("%.0f%% ACC", s2.getPunchAccuracy()) : "0% ACC";
        Label f2Name  = new Label(f2Last.toUpperCase());
        f2Name.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:white;");
        Label f2AccLbl = new Label(f2Acc);
        f2AccLbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#3b82f6;-fx-letter-spacing:1;");
        f2Box.getChildren().addAll(f2Name, f2AccLbl);

        matchup.getChildren().addAll(f1Box, centerStats, f2Box);

        // ── Action buttons (right column) ─────────────────
        VBox actions = new VBox(6);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.setMinWidth(160); actions.setMaxWidth(160);

        Button viewBtn = new Button("OPEN DOSSIER  ▶");
        viewBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#dc2626;-fx-border-color:#dc2626;" +
                "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                "-fx-font-size:10;-fx-font-weight:bold;-fx-padding:8 14;-fx-cursor:hand;-fx-letter-spacing:1;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> openDossier(fightId, fr, s1, s2));

        actions.getChildren().add(viewBtn);

        if (AccessGuard.isAdmin()) {
            HBox adminRow = new HBox(6);
            adminRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button editBtn = new Button("✎");
            editBtn.setStyle("-fx-background-color:rgba(255,255,255,0.03);-fx-border-color:rgba(255,255,255,0.08);" +
                    "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-text-fill:#52525b;-fx-font-size:14;-fx-padding:4 10;-fx-cursor:hand;");
            editBtn.setOnAction(e -> openEditWizard(fightId));

            Button deleteBtn = new Button("🗑");
            deleteBtn.setStyle("-fx-background-color:rgba(255,255,255,0.03);-fx-border-color:rgba(255,255,255,0.08);" +
                    "-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;" +
                    "-fx-text-fill:#ef4444;-fx-font-size:14;-fx-padding:4 10;-fx-cursor:hand;");
            deleteBtn.setOnAction(e -> confirmDelete(fightId));

            adminRow.getChildren().addAll(editBtn, deleteBtn);
            actions.getChildren().add(adminRow);
        }

        content.getChildren().addAll(metaCol, matchup, actions);

        // Hover effect via mouse events
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace("-fx-border-color:rgba(255,255,255,0.05);",
                    "-fx-border-color:rgba(220,38,38,0.25);") + "-fx-background-color:#18181b;");
            strip.setStyle("-fx-background-color:#dc2626;-fx-background-radius:12 0 0 12;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color:#121214;-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:1;" +
                    "-fx-border-radius:12;-fx-background-radius:12;-fx-min-height:90;");
            strip.setStyle("-fx-background-color:#27272a;-fx-background-radius:12 0 0 12;");
        });

        return card;
    }

    private void openDossier(int fightId, FightResult fr, FightStatistic s1, FightStatistic s2) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/StatsBoutShow.fxml"));
            Node pane = loader.load();
            StatsBoutShowController ctrl = loader.getController();
            ctrl.loadFight(fightId, fr);
            StackPane host = (StackPane) listContainer.getScene().lookup("#contentHost");
            if (host != null) host.getChildren().setAll(pane);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open dossier for fightId=" + fightId, e);
        }
    }

    private void openEditWizard(int fightId) {
        navigate("/tn/smartfight/views/admin/StatsWizard.fxml");
    }

    private void confirmDelete(int fightId) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Purge ALL stats records for this bout?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText("CONFIRM PURGE");
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() {
                        new FightStatisticDao().deleteByFightId(fightId);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> loadData());
                task.setOnFailed(e -> LOG.log(Level.SEVERE, "Delete failed", task.getException()));
                new Thread(task, "stats-delete").start();
            }
        });
    }

    private void navigate(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pane = loader.load();
            StackPane host = (StackPane) listContainer.getScene().lookup("#contentHost");
            if (host != null) host.getChildren().setAll(pane);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Navigate failed: " + fxmlPath, e);
        }
    }

    private void styleToggle(ToggleButton active, ToggleButton inactive) {
        active.setStyle("-fx-background-color:rgba(220,38,38,0.1);-fx-text-fill:white;-fx-border-color:#dc2626;-fx-border-width:1;-fx-border-radius:4;-fx-font-size:10;-fx-font-weight:bold;-fx-padding:6 14;-fx-cursor:hand;");
        inactive.setStyle("-fx-background-color:transparent;-fx-text-fill:#52525b;-fx-border-color:#27272a;-fx-border-width:1;-fx-border-radius:4;-fx-font-size:10;-fx-font-weight:bold;-fx-padding:6 14;-fx-cursor:hand;");
    }

    private String buildHaystack(FightResult fr, List<FightStatistic> stats) {
        StringBuilder sb = new StringBuilder();
        if (fr != null) {
            if (fr.getEventName() != null) sb.append(fr.getEventName().toLowerCase()).append(" ");
            if (fr.getFighter1Name() != null) sb.append(fr.getFighter1Name().toLowerCase()).append(" ");
            if (fr.getFighter2Name() != null) sb.append(fr.getFighter2Name().toLowerCase()).append(" ");
        }
        if (stats != null) {
            for (FightStatistic s : stats) {
                if (s.getFighterName() != null) sb.append(s.getFighterName().toLowerCase()).append(" ");
            }
        }
        return sb.toString();
    }

    private String lastName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }
}
