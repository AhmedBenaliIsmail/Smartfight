package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.smartfight.config.AppConfig;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.integration.PdfRenderer;
import tn.smartfight.integration.PebbleRenderer;
import tn.smartfight.model.Fighter;
import tn.smartfight.service.AIService;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InjuryPredictionsController {
    private static final Logger LOG = Logger.getLogger(InjuryPredictionsController.class.getName());

    public record InjuryRow(String name, AIService.InjuryRiskResult r) {}

    @FXML private ComboBox<Fighter> fighterCombo;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private VBox resultPane;
    @FXML private Label lblFighterName;
    @FXML private Label lblRiskLevel;
    @FXML private Label lblTotalRisk;
    @FXML private Label lblZone;
    @FXML private Label lblDaysToAlert;
    @FXML private Label lblMitigation;
    @FXML private Label lblAccuracy;
    @FXML private Label lblRoi;
    @FXML private TableView<InjuryRow> allResultsTable;
    @FXML private TableColumn<InjuryRow, String> colFighterAll;
    @FXML private TableColumn<InjuryRow, String> colRiskAll;
    @FXML private TableColumn<InjuryRow, Number> colRiskPctAll;
    @FXML private TableColumn<InjuryRow, String> colZoneAll;
    @FXML private TableColumn<InjuryRow, Number> colDaysAll;
    @FXML private Label statusLabel;

    private final FighterDao fighterDao = new FighterDao();
    private final AIService aiService = new AIService(AppConfig.get());
    private List<Fighter> allFighters = new ArrayList<>();
    private Fighter lastFighter;
    private AIService.InjuryRiskResult lastResult;

    @FXML
    public void initialize() {
        fighterCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fighter f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getFirstName() + " " + f.getLastName());
            }
        });
        fighterCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fighter f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? "Select fighter..." : f.getFirstName() + " " + f.getLastName());
            }
        });

        colFighterAll.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name()));
        colRiskAll.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().r().riskLevel()));
        colRiskPctAll.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().r().totalRisk()));
        colZoneAll.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().r().zone()));
        colDaysAll.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().r().daysToAlert()));

        Task<List<Fighter>> load = new Task<>() {
            @Override protected List<Fighter> call() { return fighterDao.findAll(); }
        };
        load.setOnSucceeded(e -> {
            allFighters = load.getValue();
            fighterCombo.setItems(FXCollections.observableArrayList(allFighters));
        });
        new Thread(load, "injury-load").start();
    }

    @FXML
    private void onAnalyze() {
        Fighter f = fighterCombo.getValue();
        if (f == null) { statusLabel.setText("Select a fighter first."); return; }
        progressBar.setVisible(true);
        progressLabel.setText("Analyzing " + f.getFirstName() + " " + f.getLastName() + "...");
        resultPane.setVisible(false);
        resultPane.setManaged(false);

        Task<AIService.InjuryRiskResult> task = new Task<>() {
            @Override protected AIService.InjuryRiskResult call() { return aiService.predictInjuryRisk(f); }
        };
        task.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            progressLabel.setText("");
            displayResult(f, task.getValue());
        });
        task.setOnFailed(e -> {
            progressBar.setVisible(false);
            progressLabel.setText("");
            LOG.log(Level.SEVERE, "Analyze failed", task.getException());
            statusLabel.setText("Analysis failed.");
        });
        new Thread(task, "injury-analyze").start();
    }

    @FXML
    private void onAnalyzeAll() {
        if (allFighters.isEmpty()) { statusLabel.setText("No fighters loaded."); return; }
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        allResultsTable.setVisible(true);
        allResultsTable.setManaged(true);
        resultPane.setVisible(false);
        resultPane.setManaged(false);

        Task<List<InjuryRow>> task = new Task<>() {
            @Override protected List<InjuryRow> call() {
                List<InjuryRow> results = new ArrayList<>();
                int total = allFighters.size();
                for (int i = 0; i < total; i++) {
                    Fighter f = allFighters.get(i);
                    AIService.InjuryRiskResult r = aiService.predictInjuryRisk(f);
                    results.add(new InjuryRow(f.getFirstName() + " " + f.getLastName(), r));
                    updateProgress(i + 1, total);
                    updateMessage("Analyzed " + (i + 1) + "/" + total);
                }
                return results;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            progressBar.setVisible(false);
            progressLabel.setText("");
            allResultsTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText("Analyzed " + task.getValue().size() + " fighters.");
        });
        task.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            progressBar.setVisible(false);
            LOG.log(Level.SEVERE, "Analyze all failed", task.getException());
            statusLabel.setText("Analysis failed.");
        });
        new Thread(task, "injury-all").start();
    }

    @FXML
    private void onExportPdf() {
        if (lastFighter == null || lastResult == null) {
            statusLabel.setText("Analyze a fighter first before exporting.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Injury Report PDF");
        String safeName = (lastFighter.getFirstName() + "_" + lastFighter.getLastName()).replaceAll("\\s+", "_");
        fc.setInitialFileName("injury_" + safeName + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        statusLabel.setText("Generating PDF...");
        Fighter f = lastFighter;
        AIService.InjuryRiskResult r = lastResult;
        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() {
                String riskClass = "High".equals(r.riskLevel()) ? "high"
                        : "Medium".equals(r.riskLevel()) ? "medium" : "low";
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("fighterName", f.getFirstName() + " " + f.getLastName());
                ctx.put("division", "N/A");
                ctx.put("wins", f.getWins());
                ctx.put("losses", f.getLosses());
                ctx.put("draws", f.getDraws());
                ctx.put("age", f.getAge() != null ? String.valueOf(f.getAge()) : "N/A");
                ctx.put("totalFights", f.getWins() + f.getLosses() + f.getDraws());
                ctx.put("koLosses", f.getKoLosses());
                ctx.put("fightingStyle", f.getAiStyleTag() != null ? f.getAiStyleTag() : "Unknown");
                ctx.put("eloRating", String.format("%.1f", f.getEloRating()));
                ctx.put("riskLevel", r.riskLevel());
                ctx.put("riskClass", riskClass);
                ctx.put("riskPct", String.format("%.1f", r.totalRisk()));
                ctx.put("zone", r.zone());
                ctx.put("daysToAlert", r.daysToAlert());
                ctx.put("mitigation", r.mitigationStrategy());
                ctx.put("accuracy", String.format("%.1f", r.accuracy()));
                ctx.put("roi", r.roi());
                ctx.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                String html = PebbleRenderer.render("templates/performance/injury_pdf.html.twig", ctx);
                return PdfRenderer.render(html);
            }
        };
        task.setOnSucceeded(e -> {
            try {
                Files.write(file.toPath(), task.getValue());
                statusLabel.setText("PDF saved: " + file.getName());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Write injury PDF failed", ex);
                statusLabel.setText("Failed to write PDF.");
            }
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Injury PDF export failed", task.getException());
            statusLabel.setText("PDF generation failed.");
        });
        new Thread(task, "injury-pdf").start();
    }

    private void displayResult(Fighter f, AIService.InjuryRiskResult r) {
        lastFighter = f;
        lastResult = r;
        lblFighterName.setText(f.getFirstName() + " " + f.getLastName());
        lblRiskLevel.setText(r.riskLevel());
        lblTotalRisk.setText(String.format("%.1f%%", r.totalRisk()));
        lblZone.setText(r.zone());
        lblDaysToAlert.setText(r.daysToAlert() + " days");
        lblMitigation.setText(r.mitigationStrategy());
        lblAccuracy.setText(String.format("%.1f%%", r.accuracy()));
        lblRoi.setText("$" + r.roi());
        resultPane.setVisible(true);
        resultPane.setManaged(true);
        statusLabel.setText(r.isFallback() ? "Heuristic result (AI offline)" : "AI-powered result");
    }
}
