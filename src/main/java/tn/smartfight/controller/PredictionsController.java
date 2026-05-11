package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.config.DBConnection;
import tn.smartfight.config.Session;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.dao.PredictionDao;
import tn.smartfight.model.Fighter;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.Prediction;
import tn.smartfight.service.PredictionService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PredictionsController {
    private static final Logger LOG = Logger.getLogger(PredictionsController.class.getName());

    @FXML private ComboBox<FightResult> fightCombo;
    @FXML private VBox                  predictionFormContainer;
    @FXML private VBox                  historyContainer;
    @FXML private Label                 statusLabel;

    private final FighterDao        fighterDao    = new FighterDao();
    private final PredictionDao     predictionDao = new PredictionDao();
    private final PredictionService predService   = new PredictionService();

    private FightResult      selectedFight;
    private Fighter          fighter1;
    private Fighter          fighter2;

    private ToggleGroup      winnerGroup;
    private ToggleButton     tbF1;
    private ToggleButton     tbDraw;
    private ToggleButton     tbF2;
    private ComboBox<String> methodCombo;
    private Spinner<Integer> roundSpinner;
    private VBox             roundRow;

    @FXML
    public void initialize() {
        loadScheduledFights();
        fightCombo.setOnAction(e -> {
            FightResult sel = fightCombo.getValue();
            if (sel != null
                    && (selectedFight == null || sel.getResultId() != selectedFight.getResultId())) {
                selectedFight = sel;
                buildFormForFight(sel);
            }
        });
        loadHistory();
    }

    private void loadScheduledFights() {
        Task<List<FightResult>> task = new Task<>() {
            @Override protected List<FightResult> call() { return queryScheduledFights(); }
        };
        task.setOnSucceeded(e -> {
            List<FightResult> fights = task.getValue();
            fightCombo.setItems(FXCollections.observableArrayList(fights));
            fightCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(FightResult r, boolean empty) {
                    super.updateItem(r, empty);
                    if (empty || r == null) { setText(null); return; }
                    setText(comboLabel(r));
                }
            });
            fightCombo.setButtonCell(fightCombo.getCellFactory().call(null));
        });
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "loadScheduledFights failed", task.getException()));
        new Thread(task, "pred-fights-load").start();
    }

    private List<FightResult> queryScheduledFights() {
        String sql =
            "SELECT fr.resultId, fr.fighter1Id, fr.fighter2Id, fr.eventId, fr.status, " +
            "f1.firstName AS f1First, f1.lastName AS f1Last, " +
            "f2.firstName AS f2First, f2.lastName AS f2Last, " +
            "e.eventName " +
            "FROM fight_results fr " +
            "JOIN fighters f1 ON f1.fighterId = fr.fighter1Id " +
            "JOIN fighters f2 ON f2.fighterId = fr.fighter2Id " +
            "JOIN events   e  ON e.eventId    = fr.eventId " +
            "WHERE e.status = 'SCHEDULED' OR fr.status = 'SCHEDULED' " +
            "ORDER BY e.eventDate ASC";
        List<FightResult> list = new ArrayList<>();
        DataSource ds = DBConnection.getDataSource();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FightResult r = new FightResult();
                r.setResultId(rs.getInt("resultId"));
                r.setFighter1Id(rs.getInt("fighter1Id"));
                r.setFighter2Id(rs.getInt("fighter2Id"));
                r.setEventId(rs.getInt("eventId"));
                r.setStatus(rs.getString("status"));
                r.setFighter1Name(rs.getString("f1First") + " " + rs.getString("f1Last").toUpperCase());
                r.setFighter2Name(rs.getString("f2First") + " " + rs.getString("f2Last").toUpperCase());
                r.setEventName(rs.getString("eventName"));
                list.add(r);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "queryScheduledFights failed", e);
        }
        return list;
    }

    private String comboLabel(FightResult r) {
        String f1 = r.getFighter1Name() != null ? r.getFighter1Name() : "Fighter 1";
        String f2 = r.getFighter2Name() != null ? r.getFighter2Name() : "Fighter 2";
        String ev = r.getEventName()    != null ? r.getEventName()    : "Event";
        return f1 + "  vs  " + f2 + "  —  " + ev;
    }

    private void buildFormForFight(FightResult fight) {
        predictionFormContainer.getChildren().clear();
        statusLabel.setText("Loading…");
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() {
                Fighter f1 = fighterDao.findById(fight.getFighter1Id());
                Fighter f2 = fighterDao.findById(fight.getFighter2Id());
                Prediction existing = Session.getUser() != null
                        ? predictionDao.findByFightAndUser(
                              fight.getResultId(), Session.getUser().getUserId())
                        : null;
                PredictionService.WinProbability wp =
                        predService.calculateWinProbability(
                              fight.getFighter1Id(), fight.getFighter2Id());
                return new Object[]{ f1, f2, existing, wp };
            }
        };
        task.setOnSucceeded(e -> {
            statusLabel.setText("");
            Object[] res = task.getValue();
            fighter1 = (Fighter) res[0];
            fighter2 = (Fighter) res[1];
            Prediction existing = (Prediction) res[2];
            PredictionService.WinProbability wp = (PredictionService.WinProbability) res[3];
            renderForm(fight, fighter1, fighter2, existing, wp);
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "buildFormForFight failed", task.getException());
            statusLabel.setText("Failed to load fight details.");
        });
        new Thread(task, "pred-form-build").start();
    }

    private void renderForm(FightResult fight, Fighter f1, Fighter f2,
                            Prediction existing, PredictionService.WinProbability wp) {
        predictionFormContainer.getChildren().clear();

        // Header card
        VBox headerCard = card();
        Label fightTitle = new Label(comboLabel(fight));
        fightTitle.getStyleClass().add("h3");
        boolean alreadyPredicted = existing != null;
        Label statBadge = alreadyPredicted ? badge("PREDICTED", "#16a34a") : badge("PENDING", "#52525b");
        HBox titleRow = new HBox(10, fightTitle, statBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        headerCard.getChildren().add(titleRow);
        predictionFormContainer.getChildren().add(headerCard);

        // Win probability bar
        if (wp != null) predictionFormContainer.getChildren().add(buildProbBar(wp, f1, f2));

        // Fighter selection
        winnerGroup = new ToggleGroup();
        boolean f1Sel   = alreadyPredicted && existing.getPredictedWinnerId() != null
                && f1 != null && existing.getPredictedWinnerId() == f1.getFighterId();
        boolean drawSel = alreadyPredicted && existing.getPredictedWinnerId() == null;
        boolean f2Sel   = alreadyPredicted && existing.getPredictedWinnerId() != null
                && f2 != null && existing.getPredictedWinnerId() == f2.getFighterId();

        tbF1   = new ToggleButton(); tbF1.setToggleGroup(winnerGroup);   tbF1.setSelected(f1Sel);
        tbDraw = new ToggleButton(); tbDraw.setToggleGroup(winnerGroup); tbDraw.setSelected(drawSel);
        tbF2   = new ToggleButton(); tbF2.setToggleGroup(winnerGroup);   tbF2.setSelected(f2Sel);

        VBox f1Wrap   = buildFighterCardWrapper(f1,   tbF1,   f1Sel,   "#dc2626");
        VBox drawWrap = buildDrawCardWrapper(tbDraw, drawSel);
        VBox f2Wrap   = buildFighterCardWrapper(f2,   tbF2,   f2Sel,   "#3b82f6");
        HBox.setHgrow(f1Wrap, Priority.ALWAYS);
        HBox.setHgrow(f2Wrap, Priority.ALWAYS);

        HBox fightersRow = new HBox(12, f1Wrap, drawWrap, f2Wrap);
        fightersRow.setAlignment(Pos.TOP_CENTER);
        predictionFormContainer.getChildren().add(fightersRow);

        // Method
        VBox methodCard = card();
        methodCard.getChildren().add(lbl("Win Method", "#a1a1aa", 11));
        methodCombo = new ComboBox<>(FXCollections.observableArrayList(
                "DECISION", "KO/TKO", "SUBMISSION", "NO CONTEST"));
        methodCombo.setMaxWidth(Double.MAX_VALUE);
        methodCombo.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;-fx-text-fill:#fafafa;");
        if (alreadyPredicted && existing.getPredictedMethod() != null) {
            String m = existing.getPredictedMethod();
            methodCombo.setValue("KO".equals(m) || "TKO".equals(m) ? "KO/TKO" : m);
        } else {
            methodCombo.setValue("DECISION");
        }
        methodCard.getChildren().add(methodCombo);
        predictionFormContainer.getChildren().add(methodCard);

        // Round spinner
        roundRow = card();
        roundRow.getChildren().add(lbl("Predicted Round  (KO / TKO only)", "#a1a1aa", 11));
        roundSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1));
        roundSpinner.setEditable(false);
        roundSpinner.setMaxWidth(110);
        roundSpinner.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;");
        if (alreadyPredicted && existing.getPredictedRound() != null)
            roundSpinner.getValueFactory().setValue(existing.getPredictedRound());
        roundRow.getChildren().add(roundSpinner);
        boolean isKo = "KO/TKO".equals(methodCombo.getValue());
        roundRow.setVisible(isKo); roundRow.setManaged(isKo);
        methodCombo.setOnAction(ev -> {
            boolean ko = "KO/TKO".equals(methodCombo.getValue());
            roundRow.setVisible(ko); roundRow.setManaged(ko);
        });
        predictionFormContainer.getChildren().add(roundRow);

        // Submit button
        Button submitBtn = new Button(alreadyPredicted ? "Update Prediction" : "Lock In Prediction");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(ev -> onSubmit(fight));
        predictionFormContainer.getChildren().add(submitBtn);
    }

    private VBox buildFighterCardWrapper(Fighter f, ToggleButton tb, boolean selected, String accentColor) {
        tb.setMaxWidth(Double.MAX_VALUE);
        tb.setMaxHeight(Double.MAX_VALUE);
        tb.setStyle("-fx-background-color:transparent;-fx-border-width:0;-fx-padding:0;-fx-cursor:hand;");

        StackPane photo = new StackPane();
        photo.setMinSize(72, 72); photo.setMaxSize(72, 72);
        photo.setStyle("-fx-background-color:#27272a;-fx-background-radius:36;");
        String init = (f != null && f.getLastName() != null && !f.getLastName().isEmpty())
                ? String.valueOf(f.getLastName().charAt(0)).toUpperCase() : "?";
        Label initLbl = new Label(init);
        initLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-size:28px;-fx-font-weight:bold;");
        photo.getChildren().add(initLbl);

        String name   = f != null ? f.getFirstName() + " " + f.getLastName() : "TBD";
        String record = f != null ? f.getRecord() : "-";

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-weight:bold;-fx-font-size:13px;");
        nameLbl.setWrapText(true); nameLbl.setAlignment(Pos.CENTER);
        Label recLbl = new Label(record);
        recLbl.setStyle("-fx-text-fill:#a1a1aa;-fx-font-size:11px;");
        Label selectLbl = new Label(selected ? "SELECTED ✓" : "SELECT");
        selectLbl.setStyle((selected ? "-fx-text-fill:" + accentColor : "-fx-text-fill:#71717a") +
                ";-fx-font-size:10px;-fx-font-weight:bold;");

        VBox inner = new VBox(8, photo, nameLbl, recLbl, selectLbl);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(16));
        inner.setMaxWidth(Double.MAX_VALUE);
        String desel = "-fx-background-color:#18181b;-fx-border-color:#27272a;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;";
        String sel   = "-fx-background-color:rgba(220,38,38,0.15);-fx-border-color:" + accentColor + ";-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;";
        inner.setStyle(selected ? sel : desel);

        tb.setGraphic(inner);
        tb.selectedProperty().addListener((obs, was, now) -> {
            if (now) {
                inner.setStyle(sel);
                selectLbl.setText("SELECTED ✓");
                selectLbl.setStyle("-fx-text-fill:" + accentColor + ";-fx-font-size:10px;-fx-font-weight:bold;");
            } else {
                inner.setStyle(desel);
                selectLbl.setText("SELECT");
                selectLbl.setStyle("-fx-text-fill:#71717a;-fx-font-size:10px;-fx-font-weight:bold;");
            }
        });

        VBox wrapper = new VBox(tb);
        VBox.setVgrow(tb, Priority.ALWAYS);
        wrapper.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private VBox buildDrawCardWrapper(ToggleButton tb, boolean selected) {
        tb.setStyle("-fx-background-color:transparent;-fx-border-width:0;-fx-padding:0;-fx-cursor:hand;");
        Label drawLbl = new Label("DRAW");
        drawLbl.setStyle("-fx-text-fill:#a1a1aa;-fx-font-size:18px;-fx-font-weight:bold;");
        Label selectLbl = new Label(selected ? "SELECTED ✓" : "SELECT");
        selectLbl.setStyle((selected ? "-fx-text-fill:#a1a1aa" : "-fx-text-fill:#52525b") +
                ";-fx-font-size:10px;-fx-font-weight:bold;");

        VBox inner = new VBox(8, drawLbl, selectLbl);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(16, 10, 16, 10));
        inner.setMinWidth(90);
        String desel = "-fx-background-color:#18181b;-fx-border-color:#27272a;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;";
        String sel   = "-fx-background-color:rgba(161,161,170,0.15);-fx-border-color:#a1a1aa;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;";
        inner.setStyle(selected ? sel : desel);

        tb.setGraphic(inner);
        tb.selectedProperty().addListener((obs, was, now) -> {
            if (now) {
                inner.setStyle(sel);
                selectLbl.setText("SELECTED ✓");
                selectLbl.setStyle("-fx-text-fill:#a1a1aa;-fx-font-size:10px;-fx-font-weight:bold;");
            } else {
                inner.setStyle(desel);
                selectLbl.setText("SELECT");
                selectLbl.setStyle("-fx-text-fill:#52525b;-fx-font-size:10px;-fx-font-weight:bold;");
            }
        });

        VBox wrapper = new VBox(tb);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private VBox buildProbBar(PredictionService.WinProbability wp, Fighter f1, Fighter f2) {
        VBox card = card();
        card.getChildren().add(lbl("AI Win Probability", "#a1a1aa", 11));

        String f1Last = (f1 != null && f1.getLastName() != null) ? f1.getLastName().toUpperCase() : "F1";
        String f2Last = (f2 != null && f2.getLastName() != null) ? f2.getLastName().toUpperCase() : "F2";

        Label f1Pct   = new Label(String.format("%.1f%%", wp.f1Prob()));
        f1Pct.setStyle("-fx-text-fill:#dc2626;-fx-font-weight:bold;-fx-font-size:14px;");
        Label drawPct = new Label(String.format("%.1f%%", wp.drawProb()));
        drawPct.setStyle("-fx-text-fill:#a1a1aa;-fx-font-size:12px;");
        Label f2Pct   = new Label(String.format("%.1f%%", wp.f2Prob()));
        f2Pct.setStyle("-fx-text-fill:#3b82f6;-fx-font-weight:bold;-fx-font-size:14px;");

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        HBox pctRow = new HBox(f1Pct, sp1, drawPct, sp2, f2Pct);
        pctRow.setAlignment(Pos.CENTER_LEFT);

        double total = Math.max(wp.f1Prob() + wp.drawProb() + wp.f2Prob(), 0.001);
        Region f1Bar   = probRegion(wp.f1Prob()  / total * 200, "#dc2626", "-fx-background-radius:4 0 0 4;");
        Region drawBar = probRegion(wp.drawProb() / total * 200, "#52525b", "");
        Region f2Bar   = probRegion(wp.f2Prob()  / total * 200, "#3b82f6", "-fx-background-radius:0 4 4 0;");
        HBox bar = new HBox(0, f1Bar, drawBar, f2Bar);
        bar.setPrefHeight(12); bar.setMaxWidth(Double.MAX_VALUE);

        Label f1Lbl   = lbl(f1Last, "#a1a1aa", 11);
        Label drLbl   = lbl("DRAW", "#71717a", 11);
        Label f2Lbl   = lbl(f2Last, "#a1a1aa", 11);
        Region ns1 = new Region(); HBox.setHgrow(ns1, Priority.ALWAYS);
        Region ns2 = new Region(); HBox.setHgrow(ns2, Priority.ALWAYS);
        HBox nameRow = new HBox(f1Lbl, ns1, drLbl, ns2, f2Lbl);

        card.getChildren().addAll(pctRow, bar, nameRow);
        return card;
    }

    private Region probRegion(double minWidth, String color, String extra) {
        Region r = new Region();
        r.setMinWidth(minWidth); r.setPrefHeight(12); r.setMaxHeight(12);
        r.setStyle("-fx-background-color:" + color + ";" + extra);
        return r;
    }

    private void onSubmit(FightResult fight) {
        if (Session.getUser() == null) { statusLabel.setText("Not logged in."); return; }
        if (winnerGroup == null) { statusLabel.setText("Select a fight first."); return; }

        Toggle sel = winnerGroup.getSelectedToggle();
        if (sel == null) { statusLabel.setText("Select a winner or DRAW."); return; }

        Integer winnerId = null;
        if (sel == tbF1 && fighter1 != null)      winnerId = fighter1.getFighterId();
        else if (sel == tbF2 && fighter2 != null)  winnerId = fighter2.getFighterId();
        // tbDraw → winnerId stays null

        String rawMethod    = (methodCombo != null && methodCombo.getValue() != null)
                ? methodCombo.getValue() : "DECISION";
        String storedMethod = "KO/TKO".equals(rawMethod) ? "KO" : rawMethod;
        Integer round = ("KO/TKO".equals(rawMethod) && roundSpinner != null)
                ? roundSpinner.getValue() : null;

        final Integer fw = winnerId, fr = round;
        final String  fm = storedMethod;

        statusLabel.setText("Saving…");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                Prediction p = new Prediction();
                p.setFightId(fight.getResultId());
                p.setUserId(Session.getUser().getUserId());
                p.setPredictedWinnerId(fw);
                p.setPredictedMethod(fm);
                p.setPredictedRound(fr);
                predictionDao.save(p);
                return null;
            }
        };
        task.setOnSucceeded(e -> { statusLabel.setText("Prediction saved!"); loadHistory(); });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "onSubmit failed", task.getException());
            statusLabel.setText("Save failed — check logs.");
        });
        new Thread(task, "pred-save").start();
    }

    private void loadHistory() {
        if (Session.getUser() == null) return;
        int uid = Session.getUser().getUserId();
        Task<List<Object[]>> task = new Task<>() {
            @Override protected List<Object[]> call() { return queryHistory(uid); }
        };
        task.setOnSucceeded(e -> renderHistory(task.getValue()));
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "loadHistory failed", task.getException()));
        new Thread(task, "pred-history-load").start();
    }

    private List<Object[]> queryHistory(int userId) {
        String sql =
            "SELECT CONCAT(f1.firstName,' ',f1.lastName,' vs ',f2.firstName,' ',f2.lastName) AS fightLabel, " +
            "p.points_awarded " +
            "FROM predictions p " +
            "JOIN fight_results r ON r.resultId  = p.fightId " +
            "JOIN fighters f1    ON f1.fighterId = r.fighter1Id " +
            "JOIN fighters f2    ON f2.fighterId = r.fighter2Id " +
            "WHERE p.userId = ? AND p.is_processed = 1 " +
            "ORDER BY p.created_at DESC LIMIT 10";
        List<Object[]> rows = new ArrayList<>();
        DataSource ds = DBConnection.getDataSource();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    rows.add(new Object[]{ rs.getString("fightLabel"), rs.getInt("points_awarded") });
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "queryHistory: " + e.getMessage());
        }
        return rows;
    }

    private void renderHistory(List<Object[]> rows) {
        historyContainer.getChildren().clear();
        if (rows.isEmpty()) {
            Label empty = new Label("No processed predictions yet.");
            empty.setStyle("-fx-text-fill:#52525b;-fx-font-size:12px;-fx-padding:16;");
            historyContainer.getChildren().add(empty);
            return;
        }
        for (Object[] row : rows) {
            String fightLabel = (String)  row[0];
            int    pts        = (Integer) row[1];

            Label fightLbl = new Label(fightLabel);
            fightLbl.setStyle("-fx-text-fill:#d4d4d8;-fx-font-size:12px;");
            fightLbl.setWrapText(true);
            HBox.setHgrow(fightLbl, Priority.ALWAYS);

            Label ptsBadge = pts > 0 ? badge("+" + pts + " PTS", "#16a34a") : badge("0 PTS", "#52525b");

            HBox histRow = new HBox(8, fightLbl, ptsBadge);
            histRow.setAlignment(Pos.CENTER_LEFT);
            histRow.setPadding(new Insets(8));
            histRow.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;" +
                    "-fx-border-radius:8;-fx-background-radius:8;");
            historyContainer.getChildren().add(histRow);
        }
    }

    private VBox card() {
        VBox v = new VBox(8);
        v.setPadding(new Insets(16));
        v.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;" +
                "-fx-border-radius:10;-fx-background-radius:10;");
        return v;
    }

    private Label badge(String text, String bgColor) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color:" + bgColor + ";-fx-text-fill:#ffffff;" +
                "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 8;-fx-background-radius:12;");
        return l;
    }

    private Label lbl(String text, String color, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + ";-fx-font-size:" + size + "px;");
        return l;
    }
}
