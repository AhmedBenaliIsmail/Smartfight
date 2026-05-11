package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.smartfight.config.AppConfig;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.integration.DeepSeekClient;
import tn.smartfight.model.FighterDetails;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FighterFormController {
    private static final Logger LOG = Logger.getLogger(FighterFormController.class.getName());

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField nicknameField;
    @FXML private TextField nationalityField;
    @FXML private TextField winsField;
    @FXML private TextField lossesField;
    @FXML private TextField drawsField;
    @FXML private TextField koWinsField;
    @FXML private TextField technicalWinsField;
    @FXML private TextField decisionWinsField;
    @FXML private TextField koLossesField;
    @FXML private TextField eloField;
    @FXML private TextField heightField;
    @FXML private TextField reachField;
    @FXML private TextField weightField;
    @FXML private TextField ageField;
    @FXML private TextField winStreakField;
    @FXML private TextField performanceScoreField;
    @FXML private TextField strengthOfScheduleField;
    @FXML private TextField titleDefensesField;
    @FXML private TextField strikesthrownField;
    @FXML private TextField strikeslandedField;
    @FXML private DatePicker lastFightDatePicker;
    @FXML private TextField strengthField;
    @FXML private TextField weaknessField;
    @FXML private TextField aiStyleTagField;
    @FXML private TextArea aiDescriptionArea;
    @FXML private Label photoLabel;
    @FXML private Label aiStatusLabel;
    @FXML private Label statusLabel;

    private final FighterDao dao = new FighterDao();
    private Integer fighterId;
    private String photoFilename;

    @FXML
    public void initialize() {
        if (strengthOfScheduleField != null) strengthOfScheduleField.setText("1500.0");
        if (eloField != null) eloField.setText("1500.0");
    }

    public void setFighter(FighterDetails f) {
        if (f == null) return;
        fighterId = f.getFighterId();
        firstNameField.setText(nullToEmpty(f.getFirstName()));
        lastNameField.setText(nullToEmpty(f.getLastName()));
        nicknameField.setText(nullToEmpty(f.getNickname()));
        nationalityField.setText(nullToEmpty(f.getNationality()));
        winsField.setText(String.valueOf(f.getWins()));
        lossesField.setText(String.valueOf(f.getLosses()));
        drawsField.setText(String.valueOf(f.getDraws()));
        koWinsField.setText(String.valueOf(f.getKoWins()));
        technicalWinsField.setText(String.valueOf(f.getTechnicalWins()));
        decisionWinsField.setText(String.valueOf(f.getDecisionWins()));
        koLossesField.setText(String.valueOf(f.getKoLosses()));
        eloField.setText(String.valueOf(f.getEloRating()));
        heightField.setText(f.getHeight() != null ? String.valueOf(f.getHeight()) : "");
        reachField.setText(f.getReach() != null ? String.valueOf(f.getReach()) : "");
        weightField.setText(f.getWeight() != null ? String.valueOf(f.getWeight()) : "");
        ageField.setText(f.getAge() != null ? String.valueOf(f.getAge()) : "");
        winStreakField.setText(String.valueOf(f.getWinStreak()));
        performanceScoreField.setText(String.valueOf(f.getPerformanceScore()));
        strengthOfScheduleField.setText(String.valueOf(f.getStrengthOfSchedule()));
        titleDefensesField.setText(String.valueOf(f.getTitleDefenses()));
        strikesthrownField.setText(String.valueOf(f.getStrikesThrown()));
        strikeslandedField.setText(String.valueOf(f.getStrikesLanded()));
        if (f.getLastFightDate() != null) lastFightDatePicker.setValue(f.getLastFightDate());
        strengthField.setText(nullToEmpty(f.getStrength()));
        weaknessField.setText(nullToEmpty(f.getWeakness()));
        aiStyleTagField.setText(nullToEmpty(f.getAiStyleTag()));
        aiDescriptionArea.setText(nullToEmpty(f.getAiDescription()));
        photoFilename = f.getPhotoFilename();
        if (photoLabel != null && photoFilename != null) photoLabel.setText(photoFilename);
    }

    @FXML
    private void onChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Fighter Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File file = chooser.showOpenDialog(photoLabel.getScene().getWindow());
        if (file == null) return;
        try {
            String ext = file.getName().substring(file.getName().lastIndexOf('.'));
            String filename = "boxers/" + UUID.randomUUID() + ext;
            Path destDir = Paths.get(AppConfig.get().uploadDir, "boxers");
            Files.createDirectories(destDir);
            Files.copy(file.toPath(), destDir.resolve(Paths.get(filename).getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            photoFilename = filename;
            if (photoLabel != null) photoLabel.setText(file.getName());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Photo copy failed", e);
            if (photoLabel != null) photoLabel.setText("Photo error: " + e.getMessage());
        }
    }

    @FXML
    private void onGenerateAiProfile() {
        FighterDetails snapshot = buildDetails();
        if (aiStatusLabel != null) aiStatusLabel.setText("Generating...");
        Task<String[]> task = new Task<>() {
            @Override
            protected String[] call() {
                DeepSeekClient client = new DeepSeekClient(AppConfig.get());
                String desc = client.generateFighterProfile(snapshot);
                String tag = client.generateStyleTag(snapshot);
                return new String[]{desc, tag};
            }
        };
        task.setOnSucceeded(ev -> {
            String[] results = task.getValue();
            if (!results[0].isBlank()) aiDescriptionArea.setText(results[0]);
            if (!results[1].isBlank()) aiStyleTagField.setText(results[1]);
            if (aiStatusLabel != null) aiStatusLabel.setText("AI profile generated.");
        });
        task.setOnFailed(ev -> {
            if (aiStatusLabel != null) aiStatusLabel.setText("AI generation failed.");
        });
        new Thread(task, "ai-profile-task").start();
    }

    @FXML
    private void onSave() {
        FighterDetails f = buildDetails();
        if (statusLabel != null) statusLabel.setText("Saving...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (fighterId == null) dao.create(f);
                else { f.setFighterId(fighterId); dao.update(f); }
                return null;
            }
        };
        task.setOnSucceeded(ev -> close());
        task.setOnFailed(ev -> {
            LOG.log(Level.SEVERE, "Save fighter failed", task.getException());
            if (statusLabel != null) statusLabel.setText("Save failed: " + task.getException().getMessage());
        });
        new Thread(task, "fighter-save-task").start();
    }

    @FXML
    private void onCancel() { close(); }

    private FighterDetails buildDetails() {
        FighterDetails f = new FighterDetails();
        f.setFirstName(val(firstNameField));
        f.setLastName(val(lastNameField));
        f.setNickname(val(nicknameField));
        f.setNationality(val(nationalityField));
        f.setWins(parseInt(winsField, 0));
        f.setLosses(parseInt(lossesField, 0));
        f.setDraws(parseInt(drawsField, 0));
        f.setKoWins(parseInt(koWinsField, 0));
        f.setTechnicalWins(parseInt(technicalWinsField, 0));
        f.setDecisionWins(parseInt(decisionWinsField, 0));
        f.setKoLosses(parseInt(koLossesField, 0));
        f.setEloRating(parseDouble(eloField, 1500.0));
        f.setPerformanceScore(parseDouble(performanceScoreField, 0.0));
        f.setWinStreak(parseInt(winStreakField, 0));
        f.setStrengthOfSchedule(parseDouble(strengthOfScheduleField, 1500.0));
        f.setTitleDefenses(parseInt(titleDefensesField, 0));
        f.setStrikesThrown(parseInt(strikesthrownField, 0));
        f.setStrikesLanded(parseInt(strikeslandedField, 0));
        String h = val(heightField); if (!h.isBlank()) f.setHeight(Integer.parseInt(h));
        String r = val(reachField); if (!r.isBlank()) f.setReach(Integer.parseInt(r));
        String w = val(weightField); if (!w.isBlank()) f.setWeight(Integer.parseInt(w));
        String a = val(ageField); if (!a.isBlank()) f.setAge(Integer.parseInt(a));
        LocalDate ld = lastFightDatePicker != null ? lastFightDatePicker.getValue() : null;
        if (ld != null) f.setLastFightDate(ld);
        f.setStrength(val(strengthField));
        f.setWeakness(val(weaknessField));
        f.setAiStyleTag(val(aiStyleTagField));
        f.setAiDescription(aiDescriptionArea != null ? aiDescriptionArea.getText() : "");
        f.setPhotoFilename(photoFilename);
        return f;
    }

    private void close() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private String val(TextField f) { return f == null || f.getText() == null ? "" : f.getText().trim(); }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private int parseInt(TextField f, int def) {
        try { return Integer.parseInt(val(f)); } catch (Exception e) { return def; }
    }
    private double parseDouble(TextField f, double def) {
        try { return Double.parseDouble(val(f)); } catch (Exception e) { return def; }
    }
}
