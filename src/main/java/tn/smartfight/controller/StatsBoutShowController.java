package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.dao.FightStatisticDao;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.FightStatistic;
import tn.smartfight.service.BoutAnalysisService;
import tn.smartfight.service.RoundCommentaryService;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fight Dossier — detailed view of a single fight's stats.
 * Mirrors the web app's /stats/show/{fightId} page.
 */
public class StatsBoutShowController {
    private static final Logger LOG = Logger.getLogger(StatsBoutShowController.class.getName());

    @FXML private Label  f1HeroName, f1HeroRecord;
    @FXML private Label  f2HeroName, f2HeroRecord;
    @FXML private Label  breadcrumbLabel;
    @FXML private VBox   f1StatCard, f2StatCard;
    @FXML private VBox   analysisPanel;
    @FXML private VBox   insightsPanel;
    @FXML private VBox   commentaryPanel;
    @FXML private VBox   commentaryContainer;

    @FXML private TableView<FightStatistic>           roundTable;
    @FXML private TableColumn<FightStatistic, String> colRound;
    @FXML private TableColumn<FightStatistic, String> colFighter;
    @FXML private TableColumn<FightStatistic, String> colLanded;
    @FXML private TableColumn<FightStatistic, String> colAcc;
    @FXML private TableColumn<FightStatistic, String> colJabs;
    @FXML private TableColumn<FightStatistic, String> colPower;
    @FXML private TableColumn<FightStatistic, String> colBody;
    @FXML private TableColumn<FightStatistic, String> colKD;

    @FXML private Label lblDomScore, lblTotalLanded, lblKnockdowns, lblOverallAcc;
    @FXML private Label lblSummary;

    private final FightStatisticDao statDao       = new FightStatisticDao();
    private final FightResultDao    resultDao     = new FightResultDao();
    private final BoutAnalysisService analysisService = new BoutAnalysisService();
    private final RoundCommentaryService commentaryService = new RoundCommentaryService();

    private int currentFightId;

    @FXML
    public void initialize() {
        setupTable();
    }

    /** Called by the index controller to pass the fight context. */
    public void loadFight(int fightId, FightResult frSummary) {
        this.currentFightId = fightId;
        String f1 = frSummary != null ? frSummary.getFighter1Name() : "Fighter 1";
        String f2 = frSummary != null ? frSummary.getFighter2Name() : "Fighter 2";
        breadcrumbLabel.setText("STATS / " + (frSummary != null && frSummary.getEventName() != null
                ? frSummary.getEventName().toUpperCase() : "") +
                " / " + lastName(f1) + " VS " + lastName(f2));

        Task<LoadResult> task = new Task<>() {
            @Override protected LoadResult call() {
                FightResult full = resultDao.getById(fightId);
                if (full == null) full = frSummary;
                List<FightStatistic> allStats = statDao.findByFightId(fightId);
                return new LoadResult(full, allStats);
            }
        };
        task.setOnSucceeded(e -> populate(task.getValue()));
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "Dossier load failed fightId=" + fightId, task.getException()));
        new Thread(task, "dossier-load").start();
    }

    private void populate(LoadResult r) {
        FightResult fight       = r.fight;
        List<FightStatistic> allStats = r.allStats;

        String f1Name = fight.getFighter1Name() != null ? fight.getFighter1Name() : "Fighter 1";
        String f2Name = fight.getFighter2Name() != null ? fight.getFighter2Name() : "Fighter 2";

        // Hero
        f1HeroName.setText(f1Name.toUpperCase());
        f2HeroName.setText(f2Name.toUpperCase());
        f1HeroRecord.setText(fight.getFighter1Id() + " (fighter stats)");
        f2HeroRecord.setText(fight.getFighter2Id() + " (fighter stats)");

        // Build aggregates per fighter
        FightStatistic agg1 = buildAggregate(allStats, fight.getFighter1Id(), f1Name);
        FightStatistic agg2 = buildAggregate(allStats, fight.getFighter2Id(), f2Name);

        // Stat cards
        populateStatCard(f1StatCard, agg1, "#dc2626", f1Name);
        populateStatCard(f2StatCard, agg2, "#3b82f6", f2Name);

        // Round table (only per-round rows, sorted by round)
        List<FightStatistic> perRound = new ArrayList<>();
        for (FightStatistic s : allStats) {
            if (s.getRoundNumber() != null) perRound.add(s);
        }
        perRound.sort(Comparator.comparingInt(s -> {
            int r2 = s.getRoundNumber();
            int fi = s.getFighterId() == fight.getFighter1Id() ? 0 : 1;
            return r2 * 10 + fi;
        }));
        roundTable.setItems(FXCollections.observableArrayList(perRound));

        // Generate / attach commentary
        Map<Integer, List<FightStatistic>> roundMap = new LinkedHashMap<>();
        for (FightStatistic s : perRound) {
            roundMap.computeIfAbsent(s.getRoundNumber(), k -> new ArrayList<>()).add(s);
        }

        boolean hasCommentary = false;
        commentaryContainer.getChildren().clear();

        for (Map.Entry<Integer, List<FightStatistic>> entry : roundMap.entrySet()) {
            int rnd = entry.getKey();
            List<FightStatistic> rStats = entry.getValue();

            String commentary = null;
            for (FightStatistic s : rStats) {
                if (s.getCommentary() != null && !s.getCommentary().isBlank()) {
                    commentary = s.getCommentary();
                    break;
                }
            }

            if (commentary == null && rStats.size() == 2) {
                FightStatistic rs1 = rStats.get(0);
                FightStatistic rs2 = rStats.get(1);
                commentary = commentaryService.generateForRound(
                        lastName(f1Name), rs1,
                        lastName(f2Name), rs2,
                        rnd);
            }

            if (commentary != null && !commentary.isBlank()) {
                hasCommentary = true;
                commentaryContainer.getChildren().add(buildCommentaryBlock(rnd, commentary));
            }
        }

        if (hasCommentary) {
            commentaryPanel.setVisible(true);
            commentaryPanel.setManaged(true);
        }

        // AI analysis
        BoutAnalysisService.BoutAnalysis analysis = analysisService.analyzeBout(
                fight.getResultId(),
                fight.getWinnerName(),
                fight.getMethodOfVictory(),
                fight.getRoundNumber(),
                fight.getDecisionType(),
                agg1, agg2, allStats
        );

        lblDomScore.setText(String.format("%.1f", analysis.dominanceScore));
        lblTotalLanded.setText(String.valueOf(analysis.totalPunchesLanded));
        lblKnockdowns.setText(String.valueOf(analysis.knockdowns));
        lblOverallAcc.setText(String.format("%.1f%%", analysis.overallAccuracy));
        lblSummary.setText(analysis.summary);

        // Insights
        insightsPanel.getChildren().removeIf(n -> n instanceof Label && !((Label) n).getText().startsWith("📈"));
        for (String insight : analysis.insights) {
            Label il = new Label("• " + insight);
            il.setStyle("-fx-font-size:12;-fx-text-fill:rgba(255,255,255,0.78);-fx-line-spacing:3;");
            il.setWrapText(true);
            insightsPanel.getChildren().add(il);
        }
    }

    private void populateStatCard(VBox card, FightStatistic agg, String accentColor, String fullName) {
        card.getChildren().clear();
        card.setStyle("-fx-background-color:#121214;-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:1;-fx-border-left-color:" + accentColor +
                ";-fx-border-width:0 0 0 4;-fx-border-radius:24;-fx-background-radius:24;-fx-padding:0;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 14, 20));
        Label name = new Label(lastName(fullName).toUpperCase());
        name.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:white;");
        Label sub = new Label("TOTAL PERFORMANCE ARCHIVE");
        sub.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#52525b;-fx-letter-spacing:1;");
        VBox nameBox = new VBox(2, name, sub);
        header.getChildren().add(nameBox);
        card.getChildren().add(header);

        // Main metrics row
        HBox metrics = new HBox(0);
        metrics.setAlignment(Pos.CENTER);
        metrics.setPadding(new Insets(10, 20, 14, 20));
        metrics.setStyle("-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:1 0 1 0;");

        String[] vals   = {String.format("%.1f%%", agg.getPunchAccuracy()), String.valueOf(agg.getPunchesLanded()), String.valueOf(agg.getPunchesThrown())};
        String[] labels = {"ACCURACY", "LANDED", "THROWN"};
        String[] colors = {"white", "white", "white"};
        for (int i = 0; i < 3; i++) {
            VBox col = new VBox(2);
            col.setAlignment(Pos.CENTER);
            HBox.setHgrow(col, Priority.ALWAYS);
            if (i < 2) col.setStyle("-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:0 1 0 0;");
            Label v = new Label(vals[i]);
            v.setStyle("-fx-font-size:24;-fx-font-weight:bold;-fx-text-fill:" + colors[i] + ";");
            Label l = new Label(labels[i]);
            l.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#52525b;-fx-letter-spacing:1;");
            col.getChildren().addAll(v, l);
            metrics.getChildren().add(col);
        }
        card.getChildren().add(metrics);

        // Progress bars for jab / power
        VBox bars = new VBox(12);
        bars.setPadding(new Insets(14, 20, 18, 20));

        bars.getChildren().add(buildProgressRow("JAB EFFICIENCY", agg.getJabAccuracy(), "#22d3ee"));
        bars.getChildren().add(buildProgressRow("POWER PUNCHES", agg.getPowerAccuracy(), "#fbbf24"));

        // Additional detail rows
        bars.getChildren().add(buildDetailRow("Body Shots", agg.getBodyShotsLanded(), accentColor));
        bars.getChildren().add(buildDetailRow("Knockdowns",  agg.getKnockdowns(),     accentColor));

        card.getChildren().add(bars);
    }

    private HBox buildProgressRow(String label, double pct, String barColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(0);
        VBox v = new VBox(4);
        v.setFillWidth(true);
        HBox.setHgrow(v, Priority.ALWAYS);
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#71717a;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label pctLbl = new Label(String.format("%.1f%%", pct));
        pctLbl.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:white;");
        topRow.getChildren().addAll(lbl, spacer, pctLbl);

        StackPane track = new StackPane();
        track.setMinHeight(4); track.setMaxHeight(4);
        track.setStyle("-fx-background-color:rgba(255,255,255,0.06);-fx-background-radius:2;");
        Region bar = new Region();
        bar.setMinHeight(4); bar.setMaxHeight(4);
        double w = Math.min(100, Math.max(0, pct)) / 100.0;
        bar.prefWidthProperty().bind(track.widthProperty().multiply(w));
        bar.setStyle("-fx-background-color:" + barColor + ";-fx-background-radius:2;");
        StackPane.setAlignment(bar, javafx.geometry.Pos.CENTER_LEFT);
        track.getChildren().add(bar);

        v.getChildren().addAll(topRow, track);
        row.getChildren().add(v);
        return row;
    }

    private HBox buildDetailRow(String label, int value, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-size:11;-fx-text-fill:#71717a;-fx-font-weight:600;");
        Label val = new Label(String.valueOf(value));
        val.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private VBox buildCommentaryBlock(int round, String text) {
        VBox block = new VBox(4);
        block.setStyle("-fx-border-color:rgba(220,38,38,0.4);-fx-border-width:0 0 0 3;-fx-padding:0 0 0 14;");
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label rLabel = new Label("RD " + round);
        rLabel.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
        Label aLabel = new Label("ANALYSIS");
        aLabel.setStyle("-fx-font-size:9;-fx-font-weight:bold;-fx-text-fill:#52525b;-fx-letter-spacing:1;");
        header.getChildren().addAll(rLabel, aLabel);
        Label body = new Label(text);
        body.setStyle("-fx-font-size:12;-fx-text-fill:rgba(255,255,255,0.78);-fx-line-spacing:3;");
        body.setWrapText(true);
        block.getChildren().addAll(header, body);
        return block;
    }

    private FightStatistic buildAggregate(List<FightStatistic> all, int fighterId, String name) {
        FightStatistic agg = new FightStatistic();
        agg.setFighterId(fighterId);
        agg.setFighterName(name);
        int pl = 0, pt = 0, ppl = 0, ppt = 0, jl = 0, jt = 0, kd = 0, bs = 0;
        for (FightStatistic s : all) {
            if (s.getFighterId() != fighterId || s.getRoundNumber() == null) continue;
            pl  += s.getPunchesLanded();
            pt  += s.getPunchesThrown();
            ppl += s.getPowerPunchesLanded();
            ppt += s.getPowerPunchesThrown();
            jl  += s.getJabsLanded();
            jt  += s.getJabsThrown();
            kd  += s.getKnockdowns();
            bs  += s.getBodyShotsLanded();
        }
        agg.setPunchesLanded(pl);
        agg.setPunchesThrown(pt);
        agg.setPowerPunchesLanded(ppl);
        agg.setPowerPunchesThrown(ppt);
        agg.setJabsLanded(jl);
        agg.setJabsThrown(jt);
        agg.setKnockdowns(kd);
        agg.setBodyShotsLanded(bs);
        return agg;
    }

    private void setupTable() {
        colRound.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getRoundNumber() != null ? "R" + cd.getValue().getRoundNumber() : "-"));
        colFighter.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getFighterName() != null ? cd.getValue().getFighterName() : ""));
        colLanded.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPunchesLanded() + " / " + cd.getValue().getPunchesThrown()));
        colAcc.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f%%", cd.getValue().getPunchAccuracy())));
        colJabs.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getJabsLanded() + " / " + cd.getValue().getJabsThrown()));
        colPower.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPowerPunchesLanded() + " / " + cd.getValue().getPowerPunchesThrown()));
        colBody.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cd.getValue().getBodyShotsLanded())));
        colKD.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cd.getValue().getKnockdowns())));

        // Colour the round column alternating red/blue by fighter position
        colRound.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v);
                if (!empty && getTableRow() != null) {
                    int idx = getTableRow().getIndex();
                    setStyle("-fx-font-weight:bold;-fx-text-fill:" + (idx % 2 == 0 ? "#dc2626" : "#3b82f6") + ";");
                }
            }
        });
    }

    @FXML
    private void onBack() {
        navigate("/tn/smartfight/views/admin/StatsBoutIndex.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pane = loader.load();
            StackPane host = (StackPane) roundTable.getScene().lookup("#contentHost");
            if (host != null) host.getChildren().setAll(pane);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Navigate failed: " + fxmlPath, e);
        }
    }

    private String lastName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }

    private static class LoadResult {
        final FightResult fight;
        final List<FightStatistic> allStats;
        LoadResult(FightResult f, List<FightStatistic> s) { fight = f; allStats = s; }
    }
}
