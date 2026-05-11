package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.smartfight.dao.EventDao;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.model.Event;
import tn.smartfight.model.Fighter;
import tn.smartfight.model.FightResult;
import tn.smartfight.service.RankingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultFormController {
    private static final Logger LOG = Logger.getLogger(ResultFormController.class.getName());

    @FXML private ComboBox<Event> eventCombo;
    @FXML private ComboBox<Fighter> fighter1Combo;
    @FXML private ComboBox<Fighter> fighter2Combo;
    @FXML private ComboBox<Fighter> winnerCombo;
    @FXML private ComboBox<String> methodCombo;
    @FXML private TextField decisionTypeField;
    @FXML private TextField roundNumberField;
    @FXML private TextField scheduledRoundsField;
    @FXML private TextField knockdownRoundField;
    @FXML private TextField fightNumberField;
    @FXML private DatePicker fightDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private CheckBox beltFightCheck;
    @FXML private TextField beltOrganizationField;
    @FXML private TextField fighter1OddsField;
    @FXML private TextField fighter2OddsField;
    @FXML private TextField highlightVideoUrlField;
    @FXML private TextField videoPathField;
    @FXML private TextArea insideTheNumbersArea;
    @FXML private Label statusLabel;

    private final FightResultDao dao = new FightResultDao();
    private final RankingService rankingService = new RankingService();
    private Integer resultId;
    private String previousStatus;
    private List<Fighter> fighters;

    @FXML
    public void initialize() {
        methodCombo.setItems(FXCollections.observableArrayList(
                "KO", "TKO", "SUBMISSION", "DECISION", "DRAW", "NO CONTEST", "DQ"
        ));
        statusCombo.setItems(FXCollections.observableArrayList(
                "SCHEDULED", "COMPLETED", "CANCELLED"
        ));
        statusCombo.setValue("SCHEDULED");
        scheduledRoundsField.setText("12");

        List<Event> events = new EventDao().findAll();
        eventCombo.setItems(FXCollections.observableArrayList(events));
        eventCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Event e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getName());
            }
        });
        eventCombo.setButtonCell(eventCombo.getCellFactory().call(null));

        fighters = new FighterDao().findAll();
        fighter1Combo.setItems(FXCollections.observableArrayList(fighters));
        fighter2Combo.setItems(FXCollections.observableArrayList(fighters));
        winnerCombo.setItems(FXCollections.observableArrayList(fighters));

        for (ComboBox<Fighter> cb : List.of(fighter1Combo, fighter2Combo, winnerCombo)) {
            cb.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Fighter f, boolean empty) {
                    super.updateItem(f, empty);
                    setText(empty || f == null ? null : f.getFirstName() + " " + f.getLastName());
                }
            });
            cb.setButtonCell(cb.getCellFactory().call(null));
        }
    }

    public void setResult(FightResult r) {
        if (r == null) return;
        resultId = r.getResultId();
        previousStatus = r.getStatus();
        fightNumberField.setText(String.valueOf(r.getFightNumber()));
        methodCombo.setValue(r.getMethodOfVictory());
        decisionTypeField.setText(nullToEmpty(r.getDecisionType()));
        roundNumberField.setText(String.valueOf(r.getRoundNumber()));
        scheduledRoundsField.setText(String.valueOf(r.getScheduledRounds()));
        knockdownRoundField.setText(String.valueOf(r.getKnockdownRound()));
        if (r.getFightDate() != null) fightDatePicker.setValue(r.getFightDate().toLocalDate());
        statusCombo.setValue(r.getStatus());
        beltFightCheck.setSelected(r.isBeltFight());
        beltOrganizationField.setText(nullToEmpty(r.getBeltOrganization()));
        fighter1OddsField.setText(String.valueOf(r.getFighter1Odds()));
        fighter2OddsField.setText(String.valueOf(r.getFighter2Odds()));
        highlightVideoUrlField.setText(nullToEmpty(r.getHighlightVideoUrl()));
        videoPathField.setText(nullToEmpty(r.getVideoPath()));
        insideTheNumbersArea.setText(nullToEmpty(r.getInsideTheNumbers()));

        fighters.stream().filter(f -> f.getId() == r.getEventId()).findFirst()
                .ifPresent(f -> { /* event selection handled below */ });
        eventCombo.getItems().stream()
                .filter(e -> e.getId() == r.getEventId()).findFirst()
                .ifPresent(eventCombo::setValue);
        fighters.stream().filter(f -> f.getId() == r.getFighter1Id()).findFirst()
                .ifPresent(fighter1Combo::setValue);
        fighters.stream().filter(f -> f.getId() == r.getFighter2Id()).findFirst()
                .ifPresent(fighter2Combo::setValue);
        if (r.getWinnerId() != null) {
            fighters.stream().filter(f -> f.getId() == r.getWinnerId()).findFirst()
                    .ifPresent(winnerCombo::setValue);
        }
    }

    @FXML
    private void onSave() {
        Event event = eventCombo.getValue();
        Fighter f1 = fighter1Combo.getValue();
        Fighter f2 = fighter2Combo.getValue();
        if (event == null || f1 == null || f2 == null) {
            statusLabel.setText("Event, Fighter 1, and Fighter 2 are required.");
            return;
        }
        FightResult r = buildResult(event, f1, f2);
        statusLabel.setText("Saving...");

        boolean becomingCompleted = "COMPLETED".equals(r.getStatus())
                && (resultId == null || !"COMPLETED".equals(previousStatus));

        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                int savedId;
                if (resultId == null) {
                    savedId = dao.create(r);
                } else {
                    r.setResultId(resultId);
                    dao.update(r);
                    savedId = resultId;
                }
                if (becomingCompleted && savedId > 0) {
                    rankingService.processCompletedFight(savedId);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> close());
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Save fight result failed", task.getException());
            statusLabel.setText("Save failed: " + task.getException().getMessage());
        });
        new Thread(task, "result-save-task").start();
    }

    @FXML
    private void onCancel() { close(); }

    private FightResult buildResult(Event event, Fighter f1, Fighter f2) {
        FightResult r = new FightResult();
        r.setEventId(event.getId());
        r.setFighter1Id(f1.getId());
        r.setFighter2Id(f2.getId());
        Fighter winner = winnerCombo.getValue();
        r.setWinnerId(winner != null ? winner.getId() : null);
        r.setMethodOfVictory(methodCombo.getValue());
        r.setDecisionType(val(decisionTypeField));
        r.setRoundNumber(parseInt(roundNumberField, 0));
        r.setScheduledRounds(parseInt(scheduledRoundsField, 12));
        r.setKnockdownRound(parseInt(knockdownRoundField, 0));
        r.setFightNumber(parseInt(fightNumberField, 1));
        LocalDate d = fightDatePicker.getValue();
        r.setFightDate(d != null ? LocalDateTime.of(d, LocalTime.MIDNIGHT) : null);
        r.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : "SCHEDULED");
        r.setBeltFight(beltFightCheck.isSelected());
        r.setBeltOrganization(val(beltOrganizationField));
        r.setFighter1Odds(parseDouble(fighter1OddsField, 0.0));
        r.setFighter2Odds(parseDouble(fighter2OddsField, 0.0));
        r.setHighlightVideoUrl(val(highlightVideoUrlField));
        r.setVideoPath(val(videoPathField));
        r.setInsideTheNumbers(insideTheNumbersArea.getText());
        return r;
    }

    private void close() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private String val(TextField f) { return f.getText() == null ? "" : f.getText().trim(); }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private int parseInt(TextField f, int def) {
        try { return Integer.parseInt(val(f)); } catch (Exception e) { return def; }
    }
    private double parseDouble(TextField f, double def) {
        try { return Double.parseDouble(val(f)); } catch (Exception e) { return def; }
    }
}
