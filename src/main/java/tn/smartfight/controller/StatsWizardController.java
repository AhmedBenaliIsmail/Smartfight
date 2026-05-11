package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.smartfight.config.AppConfig;
import tn.smartfight.dao.EventDao;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.dao.FightStatisticDao;
import tn.smartfight.model.Event;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.FightStatistic;
import tn.smartfight.service.AnalyticsEngine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsWizardController {
    private static final Logger LOG = Logger.getLogger(StatsWizardController.class.getName());
    private static final String INVALID_STYLE = "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:3;";

    @FXML private VBox step1, step2, step3;
    @FXML private Label step1Status;

    @FXML private ComboBox<Event>        eventCombo;
    @FXML private TableView<FightResult> fightTable;
    @FXML private TableColumn<FightResult, String>  colFighters;
    @FXML private TableColumn<FightResult, String>  colMethod;
    @FXML private TableColumn<FightResult, Integer> colRounds;
    @FXML private Label step2Status;

    @FXML private Label  fightLabel;
    @FXML private Label  f1Header, f2Header;
    @FXML private Label  commentaryRoundLabel;
    @FXML private ComboBox<Integer> roundCombo;
    @FXML private Button btnAiAutoFill, btnSaveRound;

    @FXML private TextField f1PunchesThrown, f1PunchesLanded;
    @FXML private TextField f1RightThrown,   f1RightLanded;
    @FXML private TextField f1LeftThrown,    f1LeftLanded;
    @FXML private TextField f1PowerThrown,   f1PowerLanded;
    @FXML private TextField f1JabsThrown,    f1JabsLanded;
    @FXML private TextField f1UppThrown,     f1UppLanded;
    @FXML private TextField f1BodyLanded,    f1Knockdowns;

    @FXML private TextField f2PunchesThrown, f2PunchesLanded;
    @FXML private TextField f2RightThrown,   f2RightLanded;
    @FXML private TextField f2LeftThrown,    f2LeftLanded;
    @FXML private TextField f2PowerThrown,   f2PowerLanded;
    @FXML private TextField f2JabsThrown,    f2JabsLanded;
    @FXML private TextField f2UppThrown,     f2UppLanded;
    @FXML private TextField f2BodyLanded,    f2Knockdowns;

    @FXML private TextArea commentaryArea;
    @FXML private Label validationLabel, step3Status;

    private final EventDao          eventDao  = new EventDao();
    private final FightResultDao    resultDao = new FightResultDao();
    private final FightStatisticDao statDao   = new FightStatisticDao();
    private final AnalyticsEngine   analytics = new AnalyticsEngine();

    private FightResult selectedFight;
    private boolean filling = false;

    @FXML
    public void initialize() {
        eventCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Event e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getEventName() + " (" + e.getEventDate() + ")");
            }
        });
        eventCombo.setButtonCell(eventCombo.getCellFactory().call(null));

        colFighters.setCellValueFactory(cd -> {
            FightResult r = cd.getValue();
            String f1 = r.getFighter1Name() != null ? r.getFighter1Name() : "Fighter 1";
            String f2 = r.getFighter2Name() != null ? r.getFighter2Name() : "Fighter 2";
            return new javafx.beans.property.SimpleStringProperty(f1 + " vs " + f2);
        });
        colMethod.setCellValueFactory(new PropertyValueFactory<>("methodOfVictory"));
        colRounds.setCellValueFactory(new PropertyValueFactory<>("scheduledRounds"));

        roundCombo.valueProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            btnSaveRound.setText("Save Round " + n);
            commentaryRoundLabel.setText("INSIDE THE NUMBERS — ROUND " + n);
            loadRoundStats(n);
        });

        allFields().forEach(f -> f.textProperty().addListener((obs, o, n) -> {
            if (selectedFight != null && !filling) validateAndMark();
        }));

        Task<List<Event>> evTask = new Task<>() {
            @Override protected List<Event> call() { return eventDao.findAll(); }
        };
        evTask.setOnSucceeded(e -> eventCombo.setItems(FXCollections.observableArrayList(evTask.getValue())));
        evTask.setOnFailed(e -> {
            LOG.log(Level.WARNING, "Load events failed", evTask.getException());
            step1Status.setText("Failed to load events.");
        });
        new Thread(evTask, "events-load").start();
    }

    // ── Step 1 ──────────────────────────────────────────────────────────

    @FXML
    private void onStep1Next() {
        Event ev = eventCombo.getValue();
        if (ev == null) { step1Status.setText("Select an event first."); return; }
        step1Status.setText("");
        loadFights(ev.getEventId());
    }

    private void loadFights(int eventId) {
        Task<List<FightResult>> task = new Task<>() {
            @Override protected List<FightResult> call() { return resultDao.findByEventId(eventId); }
        };
        task.setOnSucceeded(e -> {
            List<FightResult> fights = task.getValue();
            fightTable.setItems(FXCollections.observableArrayList(fights));
            step2Status.setText(fights.isEmpty() ? "No fights found for this event." : "");
            showStep(2);
        });
        task.setOnFailed(e -> step1Status.setText("Load failed: " + task.getException().getMessage()));
        new Thread(task, "fights-load").start();
    }

    // ── Step 2 ──────────────────────────────────────────────────────────

    @FXML private void onStep2Back() { showStep(1); }

    @FXML
    private void onStep2Next() {
        selectedFight = fightTable.getSelectionModel().getSelectedItem();
        if (selectedFight == null) { step2Status.setText("Select a fight first."); return; }
        setupStep3();
        showStep(3);
    }

    private void setupStep3() {
        String f1Name = selectedFight.getFighter1Name() != null ? selectedFight.getFighter1Name() : "Fighter 1";
        String f2Name = selectedFight.getFighter2Name() != null ? selectedFight.getFighter2Name() : "Fighter 2";
        fightLabel.setText(f1Name + " vs " + f2Name);
        f1Header.setText(f1Name);
        f2Header.setText(f2Name);

        int rounds = selectedFight.getScheduledRounds() > 0 ? selectedFight.getScheduledRounds() : 12;
        List<Integer> roundList = new ArrayList<>();
        for (int i = 1; i <= rounds; i++) roundList.add(i);
        roundCombo.setItems(FXCollections.observableArrayList(roundList));

        filling = true;
        clearFields();
        filling = false;

        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
        step3Status.setText("");
        roundCombo.setValue(1); // triggers listener: loadRoundStats + button label
    }

    // ── Step 3 ──────────────────────────────────────────────────────────

    @FXML private void onStep3Back() { showStep(2); }

    private void loadRoundStats(int round) {
        if (selectedFight == null) return;
        int fightId = selectedFight.getResultId();
        int f1Id    = selectedFight.getFighter1Id();
        int f2Id    = selectedFight.getFighter2Id();

        Task<FightStatistic[]> task = new Task<>() {
            @Override protected FightStatistic[] call() {
                return new FightStatistic[]{
                    statDao.findByFightFighterRound(fightId, f1Id, round),
                    statDao.findByFightFighterRound(fightId, f2Id, round)
                };
            }
        };
        task.setOnSucceeded(e -> {
            FightStatistic[] pair = task.getValue();
            filling = true;
            fillFields(pair[0], true);
            fillFields(pair[1], false);
            filling = false;

            if (pair[0] != null && pair[0].getCommentary() != null)
                commentaryArea.setText(pair[0].getCommentary());
            else if (pair[1] != null && pair[1].getCommentary() != null)
                commentaryArea.setText(pair[1].getCommentary());
            else
                commentaryArea.setText("");

            validateAndMark();
        });
        task.setOnFailed(e -> {
            LOG.log(Level.WARNING, "Load round stats failed", task.getException());
            step3Status.setText("Failed to load round " + round + " data.");
        });
        new Thread(task, "load-round-stats").start();
    }

    @FXML
    private void onSave() {
        if (selectedFight == null) return;
        String error = validate();
        if (error != null) {
            validationLabel.setText(error);
            validationLabel.setVisible(true);
            validationLabel.setManaged(true);
            return;
        }
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);

        Integer round = roundCombo.getValue();
        FightStatistic s1 = buildStat(selectedFight.getFighter1Id(), round, true);
        FightStatistic s2 = buildStat(selectedFight.getFighter2Id(), round, false);
        String commentary = commentaryArea.getText();
        s1.setCommentary(commentary);
        s2.setCommentary(commentary);

        btnSaveRound.setDisable(true);
        step3Status.setText("Saving...");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                statDao.save(s1);
                statDao.save(s2);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            btnSaveRound.setDisable(false);
            int maxRounds = selectedFight.getScheduledRounds() > 0 ? selectedFight.getScheduledRounds() : 12;
            triggerRecalcAsync();
            if (round != null && round < maxRounds) {
                step3Status.setText("Round " + round + " saved.");
                roundCombo.setValue(round + 1);
            } else {
                step3Status.setText("All rounds complete! Performance scores recalculating...");
                showStep(2);
            }
        });
        task.setOnFailed(e -> {
            btnSaveRound.setDisable(false);
            LOG.log(Level.SEVERE, "Save stats failed", task.getException());
            step3Status.setText("Save failed: " + task.getException().getMessage());
        });
        new Thread(task, "stats-save").start();
    }

    @FXML
    private void onAiAutoFill() {
        if (selectedFight == null) return;
        btnAiAutoFill.setDisable(true);
        btnAiAutoFill.setText("Loading...");
        step3Status.setText("AI generating stats...");

        Integer round = roundCombo.getValue();
        String f1Name = selectedFight.getFighter1Name() != null ? selectedFight.getFighter1Name() : "Fighter 1";
        String f2Name = selectedFight.getFighter2Name() != null ? selectedFight.getFighter2Name() : "Fighter 2";
        int r = round != null ? round : 1;

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception { return fetchAiContent(f1Name, f2Name, r); }
        };
        task.setOnSucceeded(e -> {
            filling = true;
            applyAiContent(task.getValue());
            filling = false;
            validateAndMark();
            btnAiAutoFill.setDisable(false);
            btnAiAutoFill.setText("✦ AI Auto-Fill");
            step3Status.setText("AI suggestions applied.");
        });
        task.setOnFailed(e -> {
            btnAiAutoFill.setDisable(false);
            btnAiAutoFill.setText("✦ AI Auto-Fill");
            LOG.log(Level.WARNING, "AI auto-fill failed", task.getException());
            step3Status.setText("AI Error: " + task.getException().getMessage());
        });
        new Thread(task, "ai-fill").start();
    }

    // ── AI Auto-Fill ─────────────────────────────────────────────────────

    private String fetchAiContent(String f1Name, String f2Name, int round) throws Exception {
        AppConfig cfg = AppConfig.get();
        if (cfg.deepseekApiKey == null || cfg.deepseekApiKey.isBlank())
            throw new Exception("DeepSeek API key not configured in config.properties");

        String prompt = "Generate realistic boxing CompuBox punch statistics for Round " + round +
            " between " + f1Name + " (Red Corner) and " + f2Name + " (Blue Corner). " +
            "Return ONLY a single JSON object with these exact keys (all integers except commentary string): " +
            "f1_punchesThrown, f1_punchesLanded, f1_rightThrown, f1_rightLanded, " +
            "f1_leftThrown, f1_leftLanded, f1_powerThrown, f1_powerLanded, " +
            "f1_jabsThrown, f1_jabsLanded, f1_uppThrown, f1_uppLanded, f1_body, f1_kd, " +
            "f2_punchesThrown, f2_punchesLanded, f2_rightThrown, f2_rightLanded, " +
            "f2_leftThrown, f2_leftLanded, f2_powerThrown, f2_powerLanded, " +
            "f2_jabsThrown, f2_jabsLanded, f2_uppThrown, f2_uppLanded, f2_body, f2_kd, " +
            "commentary. Rules: rightThrown+leftThrown=punchesThrown, " +
            "rightLanded+leftLanded=punchesLanded, landed<=thrown for all, kd 0-3.";

        String body = "{\"model\":\"deepseek-chat\",\"messages\":[{\"role\":\"user\",\"content\":" +
            jsonStr(prompt) + "}],\"temperature\":0.8,\"max_tokens\":600}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(cfg.deepseekApiUrl + "/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + cfg.deepseekApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("DeepSeek API error " + response.statusCode() + ": " + response.body().substring(0, Math.min(200, response.body().length())));

        return extractContent(response.body());
    }

    private String extractContent(String responseBody) {
        int idx = responseBody.indexOf("\"content\":\"");
        if (idx < 0) throw new RuntimeException("No content field in AI response");
        idx += 11;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = idx; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (esc) {
                switch (c) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(c);    break;
                }
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void applyAiContent(String content) {
        // Strip markdown code block if AI wrapped the JSON
        int jsonStart = content.indexOf('{');
        int jsonEnd   = content.lastIndexOf('}');
        String json   = (jsonStart >= 0 && jsonEnd > jsonStart) ? content.substring(jsonStart, jsonEnd + 1) : content;

        f1PunchesThrown.setText(String.valueOf(extractInt(json, "f1_punchesThrown")));
        f1PunchesLanded.setText(String.valueOf(extractInt(json, "f1_punchesLanded")));
        f1RightThrown.setText(String.valueOf(extractInt(json, "f1_rightThrown")));
        f1RightLanded.setText(String.valueOf(extractInt(json, "f1_rightLanded")));
        f1LeftThrown.setText(String.valueOf(extractInt(json, "f1_leftThrown")));
        f1LeftLanded.setText(String.valueOf(extractInt(json, "f1_leftLanded")));
        f1PowerThrown.setText(String.valueOf(extractInt(json, "f1_powerThrown")));
        f1PowerLanded.setText(String.valueOf(extractInt(json, "f1_powerLanded")));
        f1JabsThrown.setText(String.valueOf(extractInt(json, "f1_jabsThrown")));
        f1JabsLanded.setText(String.valueOf(extractInt(json, "f1_jabsLanded")));
        f1UppThrown.setText(String.valueOf(extractInt(json, "f1_uppThrown")));
        f1UppLanded.setText(String.valueOf(extractInt(json, "f1_uppLanded")));
        f1BodyLanded.setText(String.valueOf(extractInt(json, "f1_body")));
        f1Knockdowns.setText(String.valueOf(extractInt(json, "f1_kd")));

        f2PunchesThrown.setText(String.valueOf(extractInt(json, "f2_punchesThrown")));
        f2PunchesLanded.setText(String.valueOf(extractInt(json, "f2_punchesLanded")));
        f2RightThrown.setText(String.valueOf(extractInt(json, "f2_rightThrown")));
        f2RightLanded.setText(String.valueOf(extractInt(json, "f2_rightLanded")));
        f2LeftThrown.setText(String.valueOf(extractInt(json, "f2_leftThrown")));
        f2LeftLanded.setText(String.valueOf(extractInt(json, "f2_leftLanded")));
        f2PowerThrown.setText(String.valueOf(extractInt(json, "f2_powerThrown")));
        f2PowerLanded.setText(String.valueOf(extractInt(json, "f2_powerLanded")));
        f2JabsThrown.setText(String.valueOf(extractInt(json, "f2_jabsThrown")));
        f2JabsLanded.setText(String.valueOf(extractInt(json, "f2_jabsLanded")));
        f2UppThrown.setText(String.valueOf(extractInt(json, "f2_uppThrown")));
        f2UppLanded.setText(String.valueOf(extractInt(json, "f2_uppLanded")));
        f2BodyLanded.setText(String.valueOf(extractInt(json, "f2_body")));
        f2Knockdowns.setText(String.valueOf(extractInt(json, "f2_kd")));

        String commentary = extractStr(json, "commentary");
        if (!commentary.isEmpty()) commentaryArea.setText(commentary);
    }

    private int extractInt(String json, String key) {
        try {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {}
        return 0;
    }

    private String extractStr(String json, String key) {
        try {
            Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
            if (m.find()) return m.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception ignored) {}
        return "";
    }

    private static String jsonStr(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"";
    }

    // ── Validation ───────────────────────────────────────────────────────

    private void validateAndMark() {
        clearValidationMarks();

        int pt1 = i(f1PunchesThrown), pl1 = i(f1PunchesLanded);
        int rt1 = i(f1RightThrown),   rl1 = i(f1RightLanded);
        int lt1 = i(f1LeftThrown),    ll1 = i(f1LeftLanded);
        int pwt1= i(f1PowerThrown),   pwl1= i(f1PowerLanded);
        int jt1 = i(f1JabsThrown),    jl1 = i(f1JabsLanded);
        int ut1 = i(f1UppThrown),     ul1 = i(f1UppLanded);

        int pt2 = i(f2PunchesThrown), pl2 = i(f2PunchesLanded);
        int rt2 = i(f2RightThrown),   rl2 = i(f2RightLanded);
        int lt2 = i(f2LeftThrown),    ll2 = i(f2LeftLanded);
        int pwt2= i(f2PowerThrown),   pwl2= i(f2PowerLanded);
        int jt2 = i(f2JabsThrown),    jl2 = i(f2JabsLanded);
        int ut2 = i(f2UppThrown),     ul2 = i(f2UppLanded);

        // Fighter 1
        if (pl1 > pt1)  { markInvalid(f1PunchesLanded); markInvalid(f1PunchesThrown); }
        if (pwl1 > pwt1){ markInvalid(f1PowerLanded);   markInvalid(f1PowerThrown); }
        if (jl1 > jt1)  { markInvalid(f1JabsLanded);   markInvalid(f1JabsThrown); }
        if (ul1 > ut1)  { markInvalid(f1UppLanded);     markInvalid(f1UppThrown); }
        if (rt1 + lt1 != pt1) { markInvalid(f1PunchesThrown); markInvalid(f1RightThrown); markInvalid(f1LeftThrown); }
        if (rl1 + ll1 != pl1) { markInvalid(f1PunchesLanded); markInvalid(f1RightLanded); markInvalid(f1LeftLanded); }

        // Fighter 2
        if (pl2 > pt2)  { markInvalid(f2PunchesLanded); markInvalid(f2PunchesThrown); }
        if (pwl2 > pwt2){ markInvalid(f2PowerLanded);   markInvalid(f2PowerThrown); }
        if (jl2 > jt2)  { markInvalid(f2JabsLanded);   markInvalid(f2JabsThrown); }
        if (ul2 > ut2)  { markInvalid(f2UppLanded);     markInvalid(f2UppThrown); }
        if (rt2 + lt2 != pt2) { markInvalid(f2PunchesThrown); markInvalid(f2RightThrown); markInvalid(f2LeftThrown); }
        if (rl2 + ll2 != pl2) { markInvalid(f2PunchesLanded); markInvalid(f2RightLanded); markInvalid(f2LeftLanded); }

        boolean hasErrors = allFields().stream().anyMatch(f -> !f.getStyle().isEmpty());
        if (hasErrors) {
            validationLabel.setText("Fix validation errors before saving.");
            validationLabel.setVisible(true);
            validationLabel.setManaged(true);
        } else {
            validationLabel.setVisible(false);
            validationLabel.setManaged(false);
        }
    }

    private void markInvalid(TextField f) { f.setStyle(INVALID_STYLE); }

    private void clearValidationMarks() { allFields().forEach(f -> f.setStyle("")); }

    private String validate() {
        int pt1 = i(f1PunchesThrown), pl1 = i(f1PunchesLanded);
        int rt1 = i(f1RightThrown),   rl1 = i(f1RightLanded);
        int lt1 = i(f1LeftThrown),    ll1 = i(f1LeftLanded);
        int pwt1= i(f1PowerThrown),   pwl1= i(f1PowerLanded);
        int jt1 = i(f1JabsThrown),    jl1 = i(f1JabsLanded);
        int ut1 = i(f1UppThrown),     ul1 = i(f1UppLanded);

        int pt2 = i(f2PunchesThrown), pl2 = i(f2PunchesLanded);
        int rt2 = i(f2RightThrown),   rl2 = i(f2RightLanded);
        int lt2 = i(f2LeftThrown),    ll2 = i(f2LeftLanded);
        int pwt2= i(f2PowerThrown),   pwl2= i(f2PowerLanded);
        int jt2 = i(f2JabsThrown),    jl2 = i(f2JabsLanded);
        int ut2 = i(f2UppThrown),     ul2 = i(f2UppLanded);

        if (pl1 > pt1)  return "F1: Punches landed > thrown";
        if (pwl1> pwt1) return "F1: Power landed > thrown";
        if (jl1 > jt1)  return "F1: Jabs landed > thrown";
        if (ul1 > ut1)  return "F1: Uppercuts landed > thrown";
        if (rt1 + lt1 != pt1) return "F1: Right(" + rt1 + ")+Left(" + lt1 + ")=" + (rt1+lt1) + " ≠ Total(" + pt1 + ")";
        if (rl1 + ll1 != pl1) return "F1: Right landed + Left landed ≠ Total landed";

        if (pl2 > pt2)  return "F2: Punches landed > thrown";
        if (pwl2> pwt2) return "F2: Power landed > thrown";
        if (jl2 > jt2)  return "F2: Jabs landed > thrown";
        if (ul2 > ut2)  return "F2: Uppercuts landed > thrown";
        if (rt2 + lt2 != pt2) return "F2: Right(" + rt2 + ")+Left(" + lt2 + ")=" + (rt2+lt2) + " ≠ Total(" + pt2 + ")";
        if (rl2 + ll2 != pl2) return "F2: Right landed + Left landed ≠ Total landed";

        return null;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void triggerRecalcAsync() {
        int f1Id = selectedFight.getFighter1Id();
        int f2Id = selectedFight.getFighter2Id();
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                analytics.recalculateAndPersist(f1Id);
                analytics.recalculateAndPersist(f2Id);
                return null;
            }
        };
        task.setOnFailed(e -> LOG.log(Level.WARNING, "Background recalc failed", task.getException()));
        new Thread(task, "recalc-bg").start();
    }

    private FightStatistic buildStat(int fighterId, Integer round, boolean isF1) {
        FightStatistic s = new FightStatistic();
        s.setFightId(selectedFight.getResultId());
        s.setFighterId(fighterId);
        s.setRoundNumber(round);
        if (isF1) {
            s.setPunchesThrown(i(f1PunchesThrown));  s.setPunchesLanded(i(f1PunchesLanded));
            s.setRightHandThrown(i(f1RightThrown));  s.setRightHandLanded(i(f1RightLanded));
            s.setLeftHandThrown(i(f1LeftThrown));    s.setLeftHandLanded(i(f1LeftLanded));
            s.setPowerPunchesThrown(i(f1PowerThrown)); s.setPowerPunchesLanded(i(f1PowerLanded));
            s.setJabsThrown(i(f1JabsThrown));        s.setJabsLanded(i(f1JabsLanded));
            s.setUppercutsThrown(i(f1UppThrown));    s.setUppercutsLanded(i(f1UppLanded));
            s.setBodyShotsLanded(i(f1BodyLanded));   s.setKnockdowns(i(f1Knockdowns));
        } else {
            s.setPunchesThrown(i(f2PunchesThrown));  s.setPunchesLanded(i(f2PunchesLanded));
            s.setRightHandThrown(i(f2RightThrown));  s.setRightHandLanded(i(f2RightLanded));
            s.setLeftHandThrown(i(f2LeftThrown));    s.setLeftHandLanded(i(f2LeftLanded));
            s.setPowerPunchesThrown(i(f2PowerThrown)); s.setPowerPunchesLanded(i(f2PowerLanded));
            s.setJabsThrown(i(f2JabsThrown));        s.setJabsLanded(i(f2JabsLanded));
            s.setUppercutsThrown(i(f2UppThrown));    s.setUppercutsLanded(i(f2UppLanded));
            s.setBodyShotsLanded(i(f2BodyLanded));   s.setKnockdowns(i(f2Knockdowns));
        }
        return s;
    }

    private void fillFields(FightStatistic s, boolean isF1) {
        if (s == null) { if (isF1) clearF1(); else clearF2(); return; }
        if (isF1) {
            f1PunchesThrown.setText(String.valueOf(s.getPunchesThrown()));
            f1PunchesLanded.setText(String.valueOf(s.getPunchesLanded()));
            f1RightThrown.setText(String.valueOf(s.getRightHandThrown()));
            f1RightLanded.setText(String.valueOf(s.getRightHandLanded()));
            f1LeftThrown.setText(String.valueOf(s.getLeftHandThrown()));
            f1LeftLanded.setText(String.valueOf(s.getLeftHandLanded()));
            f1PowerThrown.setText(String.valueOf(s.getPowerPunchesThrown()));
            f1PowerLanded.setText(String.valueOf(s.getPowerPunchesLanded()));
            f1JabsThrown.setText(String.valueOf(s.getJabsThrown()));
            f1JabsLanded.setText(String.valueOf(s.getJabsLanded()));
            f1UppThrown.setText(String.valueOf(s.getUppercutsThrown()));
            f1UppLanded.setText(String.valueOf(s.getUppercutsLanded()));
            f1BodyLanded.setText(String.valueOf(s.getBodyShotsLanded()));
            f1Knockdowns.setText(String.valueOf(s.getKnockdowns()));
        } else {
            f2PunchesThrown.setText(String.valueOf(s.getPunchesThrown()));
            f2PunchesLanded.setText(String.valueOf(s.getPunchesLanded()));
            f2RightThrown.setText(String.valueOf(s.getRightHandThrown()));
            f2RightLanded.setText(String.valueOf(s.getRightHandLanded()));
            f2LeftThrown.setText(String.valueOf(s.getLeftHandThrown()));
            f2LeftLanded.setText(String.valueOf(s.getLeftHandLanded()));
            f2PowerThrown.setText(String.valueOf(s.getPowerPunchesThrown()));
            f2PowerLanded.setText(String.valueOf(s.getPowerPunchesLanded()));
            f2JabsThrown.setText(String.valueOf(s.getJabsThrown()));
            f2JabsLanded.setText(String.valueOf(s.getJabsLanded()));
            f2UppThrown.setText(String.valueOf(s.getUppercutsThrown()));
            f2UppLanded.setText(String.valueOf(s.getUppercutsLanded()));
            f2BodyLanded.setText(String.valueOf(s.getBodyShotsLanded()));
            f2Knockdowns.setText(String.valueOf(s.getKnockdowns()));
        }
    }

    private void showStep(int step) {
        step1.setVisible(step == 1); step1.setManaged(step == 1);
        step2.setVisible(step == 2); step2.setManaged(step == 2);
        step3.setVisible(step == 3); step3.setManaged(step == 3);
    }

    private void clearFields() { clearF1(); clearF2(); commentaryArea.setText(""); }

    private void clearF1() {
        for (TextField f : new TextField[]{f1PunchesThrown, f1PunchesLanded,
                f1RightThrown, f1RightLanded, f1LeftThrown, f1LeftLanded,
                f1PowerThrown, f1PowerLanded, f1JabsThrown, f1JabsLanded,
                f1UppThrown, f1UppLanded, f1BodyLanded, f1Knockdowns}) f.setText("0");
    }

    private void clearF2() {
        for (TextField f : new TextField[]{f2PunchesThrown, f2PunchesLanded,
                f2RightThrown, f2RightLanded, f2LeftThrown, f2LeftLanded,
                f2PowerThrown, f2PowerLanded, f2JabsThrown, f2JabsLanded,
                f2UppThrown, f2UppLanded, f2BodyLanded, f2Knockdowns}) f.setText("0");
    }

    private List<TextField> allFields() {
        return Arrays.asList(
            f1PunchesThrown, f1PunchesLanded, f1RightThrown, f1RightLanded,
            f1LeftThrown, f1LeftLanded, f1PowerThrown, f1PowerLanded,
            f1JabsThrown, f1JabsLanded, f1UppThrown, f1UppLanded,
            f1BodyLanded, f1Knockdowns,
            f2PunchesThrown, f2PunchesLanded, f2RightThrown, f2RightLanded,
            f2LeftThrown, f2LeftLanded, f2PowerThrown, f2PowerLanded,
            f2JabsThrown, f2JabsLanded, f2UppThrown, f2UppLanded,
            f2BodyLanded, f2Knockdowns
        );
    }

    private int i(TextField f) {
        try { return Integer.parseInt(f.getText().trim()); } catch (Exception e) { return 0; }
    }
}
