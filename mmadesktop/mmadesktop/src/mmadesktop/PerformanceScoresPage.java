package mmadesktop;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import model.Fighter;
import service.RankingService;

import java.io.File;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PerformanceScoresPage {

    private final RankingService rankingService = new RankingService();
    private final BorderPane root = new BorderPane();
    private TableView<Fighter> tableView;
    private final ObservableList<Fighter> masterList = FXCollections.observableArrayList();
    private TextField searchField;
    private Label notifLabel;

    public PerformanceScoresPage() {
        root.setStyle("-fx-background-color:#0a0a0b;");
        VBox content = new VBox(20);
        content.setPadding(new Insets(28,32,32,32));
        content.setStyle("-fx-background-color:#0a0a0b;");
        content.getChildren().addAll(buildNotifBar(), buildHeader(), buildToolbar(), buildTableCard());
        ScrollPane sc = new ScrollPane(content);
        sc.setFitToWidth(true);
        sc.setStyle("-fx-background:#0a0a0b;-fx-background-color:#0a0a0b;");
        root.setCenter(sc);
        loadData();
    }

    public Node getRoot() { return root; }

    private HBox buildNotifBar() {
        notifLabel = new Label("");
        notifLabel.setVisible(false);
        notifLabel.setStyle("-fx-text-fill:#22c55e;-fx-font-size:12px;-fx-font-weight:bold;");
        HBox bar = new HBox(notifLabel);
        bar.setPadding(new Insets(0,0,4,0));
        return bar;
    }

    private void notify(String msg) {
        Platform.runLater(() -> {
            notifLabel.setText("✔  " + msg);
            notifLabel.setVisible(true);
        });
        new java.util.Timer().schedule(new java.util.TimerTask() {
            public void run() {
                Platform.runLater(() -> notifLabel.setVisible(false));
            }
        }, 4000);
    }

    private HBox buildHeader() {
        Label title = new Label("PERFORMANCE SCORES");
        title.getStyleClass().add("page-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button recalcBtn = new Button("⟳ RECALCULATE ALL");
        recalcBtn.getStyleClass().add("btn-red");
        recalcBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    rankingService.recomputeAllRankings();
                    Platform.runLater(() -> {
                        loadData();
                        notify("All performance scores recalculated!");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Recalculation failed: " + ex.getMessage()));
                }
            }).start();
        });

        Button exportCsvBtn = new Button("📄 EXPORT CSV");
        exportCsvBtn.getStyleClass().add("btn-dark");
        exportCsvBtn.setOnAction(e -> exportToCSV());

        Button refreshBtn = new Button("↻");
        refreshBtn.getStyleClass().add("btn-dark");
        refreshBtn.setOnAction(e -> { loadData(); notify("Refreshed"); });

        HBox hbox = new HBox(10, title, spacer, recalcBtn, exportCsvBtn, refreshBtn);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return hbox;
    }

    private HBox buildToolbar() {
        searchField = new TextField();
        searchField.setPromptText("🔍  Search fighter...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((o, ov, nv) -> applyFilter());

        ComboBox<String> sortCb = new ComboBox<>();
        sortCb.getItems().addAll("Performance ↓", "Performance ↑", "ELO ↓", "Win Streak ↓", "SOS ↓");
        sortCb.setValue("Performance ↓");
        sortCb.getStyleClass().add("combo-box");
        sortCb.setOnAction(e -> applyFilter(sortCb.getValue()));

        return new HBox(10, searchField, sortCb);
    }

    private VBox buildTableCard() {
        tableView = new TableView<>();
        tableView.getStyleClass().add("table-view");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);

        // Fighter name
        TableColumn<Fighter, String> nameCol = new TableColumn<>("FIGHTER");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));

        // Weight class
        TableColumn<Fighter, String> weightCol = new TableColumn<>("WEIGHT CLASS");
        weightCol.setPrefWidth(120);
        weightCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeightClass()));

        // Performance Score (0-100) with color coding
        TableColumn<Fighter, Number> perfCol = new TableColumn<>("PERFORMANCE");
        perfCol.setPrefWidth(100);
        perfCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getPerformanceScore()));
        perfCol.setCellFactory(col -> new TableCell<Fighter, Number>() {
            @Override
            protected void updateItem(Number score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                int val = score.intValue();
                setText(val + "");
                if (val >= 80) setStyle("-fx-text-fill:#22c55e;-fx-font-weight:bold;");
                else if (val >= 60) setStyle("-fx-text-fill:#f5a623;-fx-font-weight:bold;");
                else setStyle("-fx-text-fill:#e8001c;-fx-font-weight:bold;");
            }
        });

        // ELO Rating
        TableColumn<Fighter, Number> eloCol = new TableColumn<>("ELO");
        eloCol.setPrefWidth(80);
        eloCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getEloRating()));

        // Win Streak
        TableColumn<Fighter, Number> streakCol = new TableColumn<>("WIN STREAK");
        streakCol.setPrefWidth(90);
        streakCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getWinStreak()));

        // Strength of Schedule
        TableColumn<Fighter, Number> sosCol = new TableColumn<>("SOS");
        sosCol.setPrefWidth(80);
        sosCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getStrengthOfSchedule()));

        // Wins / Losses
        TableColumn<Fighter, Number> winsCol = new TableColumn<>("WINS");
        winsCol.setPrefWidth(70);
        winsCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getWins()));

        TableColumn<Fighter, Number> lossesCol = new TableColumn<>("LOSSES");
        lossesCol.setPrefWidth(70);
        lossesCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLosses()));

        tableView.getColumns().addAll(nameCol, weightCol, perfCol, eloCol, streakCol, sosCol, winsCol, lossesCol);

        VBox card = new VBox(tableView);
        card.setStyle("-fx-background-color:#16161b;-fx-border-color:#222228;-fx-border-width:1;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:20;");
        return card;
    }

    private void loadData() {
        loadData("Performance ↓");
    }

    private void loadData(String sort) {
        try {
            List<Fighter> fighters = rankingService.getRankedFighters();
            masterList.setAll(fighters);
            applyFilter(sort);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load performance data: " + e.getMessage());
        }
    }

    private void applyFilter() {
        applyFilter("Performance ↓");
    }

    private void applyFilter(String sort) {
        String query = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        List<Fighter> filtered = masterList.stream()
                .filter(f -> query.isEmpty() || f.getFullName().toLowerCase().contains(query))
                .collect(Collectors.toList());

        switch (sort) {
            case "Performance ↑":
                filtered.sort(Comparator.comparingDouble(Fighter::getPerformanceScore));
                break;
            case "ELO ↓":
                filtered.sort(Comparator.comparingDouble(Fighter::getEloRating).reversed());
                break;
            case "Win Streak ↓":
                filtered.sort(Comparator.comparingInt(Fighter::getWinStreak).reversed());
                break;
            case "SOS ↓":
                filtered.sort(Comparator.comparingDouble(Fighter::getStrengthOfSchedule).reversed());
                break;
            default: // Performance ↓
                filtered.sort(Comparator.comparingDouble(Fighter::getPerformanceScore).reversed());
                break;
        }
        tableView.setItems(FXCollections.observableArrayList(filtered));
    }

    // ---------- CSV Export ----------
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Performance Scores as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Fighter,Weight,Performance,ELO,Streak,SOS,Wins,Losses");
                for (Fighter f : tableView.getItems()) {
                    writer.printf(
                        "\"%s\",\"%s\",%d,%d,%d,%d,%d,%d%n",
                        f.getFullName().replace("\"", "\"\""),
                        f.getWeightClass() == null ? "" : f.getWeightClass().replace("\"", "\"\""),
                        (int) f.getPerformanceScore(),
                        (int) f.getEloRating(),
                        f.getWinStreak(),
                        (int) f.getStrengthOfSchedule(),
                        f.getWins(),
                        f.getLosses()
                    );
                }
                Platform.runLater(() -> showAlert("Export Successful", "CSV saved to:\n" + file.getAbsolutePath()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Export Failed", e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (root.getScene() != null) alert.initOwner(root.getScene().getWindow());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }
}