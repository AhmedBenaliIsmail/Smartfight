package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.smartfight.integration.PdfRenderer;
import tn.smartfight.integration.PebbleRenderer;
import tn.smartfight.model.Ranking;
import tn.smartfight.service.RankingService;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RankingsListController {
    private static final Logger LOG = Logger.getLogger(RankingsListController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> orgFilter;
    @FXML private ComboBox<String> divFilter;
    @FXML private TableView<Ranking> rankTable;
    @FXML private TableColumn<Ranking, Number> colPos;
    @FXML private TableColumn<Ranking, String> colOrg;
    @FXML private TableColumn<Ranking, String> colDivision;
    @FXML private TableColumn<Ranking, String> colFighter;
    @FXML private TableColumn<Ranking, Number> colPoints;
    @FXML private Label statusLabel;

    private final RankingService service = new RankingService();
    private List<Ranking> allRankings;

    @FXML
    public void initialize() {
        colPos     .setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getRankPosition()));
        colOrg     .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOrganization()));
        colDivision.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDivisionName()));
        colFighter .setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFighterName()));
        colPoints  .setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPoints()));

        orgFilter.setItems(FXCollections.observableArrayList("ALL", "WBC", "WBA", "IBF", "WBO", "MEDIA"));
        orgFilter.setValue("ALL");
        orgFilter.setOnAction(e -> applyFilter());
        if (searchField != null) searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<List<Ranking>> task = new Task<>() {
            @Override protected List<Ranking> call() { return service.findAll(); }
        };
        task.setOnSucceeded(e -> {
            allRankings = task.getValue();
            applyFilter();
            statusLabel.setText(allRankings.size() + " ranking entries");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load rankings failed", task.getException());
            statusLabel.setText("Failed to load rankings");
        });
        new Thread(task, "rankings-load").start();
    }

    private void applyFilter() {
        if (allRankings == null) return;
        String org    = orgFilter.getValue();
        String div    = divFilter != null ? divFilter.getValue() : null;
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase() : "";

        List<Ranking> filtered = allRankings.stream()
                .filter(r -> "ALL".equals(org) || org == null || org.equals(r.getOrganization()))
                .filter(r -> "ALL DIVISIONS".equals(div) || div == null || div.equals(r.getDivisionName()))
                .filter(r -> search.isEmpty() || (r.getFighterName() != null && r.getFighterName().toLowerCase().contains(search)))
                .toList();
        rankTable.setItems(FXCollections.observableArrayList(filtered));
        statusLabel.setText(filtered.size() + " entries");

        if (divFilter != null && (divFilter.getItems() == null || divFilter.getItems().isEmpty())) {
            java.util.Set<String> divs = new java.util.LinkedHashSet<>();
            divs.add("ALL DIVISIONS");
            allRankings.stream().map(Ranking::getDivisionName).filter(d -> d != null).distinct().forEach(divs::add);
            divFilter.setItems(FXCollections.observableArrayList(divs));
            divFilter.setValue("ALL DIVISIONS");
            divFilter.setOnAction(e -> applyFilter());
        }
    }

    @FXML
    private void onRefresh() { loadData(); }

    @FXML
    private void onExportPdf() {
        if (allRankings == null || allRankings.isEmpty()) {
            statusLabel.setText("No rankings to export. Load rankings first.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Rankings PDF");
        fc.setInitialFileName("rankings.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        statusLabel.setText("Generating PDF...");
        List<Ranking> snapshot = new ArrayList<>(allRankings);
        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() {
                Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
                for (Ranking r : snapshot) {
                    String div = r.getDivisionName() != null ? r.getDivisionName() : "General";
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("fullName", r.getFighterName() != null ? r.getFighterName() : "Unknown");
                    entry.put("org", r.getOrganization() != null ? r.getOrganization() : "");
                    entry.put("pts", (long) r.getPoints());
                    grouped.computeIfAbsent(div, k -> new ArrayList<>()).add(entry);
                }
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("groupedRankings", grouped);
                ctx.put("lastUpdated", now);
                ctx.put("year", String.valueOf(LocalDateTime.now().getYear()));
                ctx.put("qrSvg", "");
                String html = PebbleRenderer.render("templates/ranking/pdf.html.twig", ctx);
                return PdfRenderer.render(html);
            }
        };
        task.setOnSucceeded(e -> {
            try {
                Files.write(file.toPath(), task.getValue());
                statusLabel.setText("PDF saved: " + file.getName());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Write PDF failed", ex);
                statusLabel.setText("Failed to write PDF.");
            }
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "PDF export failed", task.getException());
            statusLabel.setText("PDF generation failed.");
        });
        new Thread(task, "rankings-pdf").start();
    }

    @FXML
    private void onRecomputeAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will RESET all fighter stats (ELO, wins, losses, draws, etc.) and replay every completed fight.\n\nContinue?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Recompute All Rankings from Scratch");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                statusLabel.setText("Recomputing — this may take a moment...");
                Task<Void> task = new Task<>() {
                    @Override protected Void call() { service.recomputeAllRankings(); return null; }
                };
                task.setOnSucceeded(e -> loadData());
                task.setOnFailed(e -> {
                    LOG.log(Level.SEVERE, "recomputeAllRankings failed", task.getException());
                    statusLabel.setText("Recompute failed: " + task.getException().getMessage());
                });
                new Thread(task, "ranking-recompute").start();
            }
        });
    }
}
