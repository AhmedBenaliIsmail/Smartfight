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
import java.util.List;
import java.util.stream.Collectors;

public class RankingsPage {

    private final RankingService rankingService = new RankingService();
    private final BorderPane root = new BorderPane();
    private TableView<Fighter> tableView;
    private final ObservableList<Fighter> masterList = FXCollections.observableArrayList();
    private TextField searchField;
    private Label notifLabel;

    public RankingsPage() {
        root.setStyle("-fx-background-color:#0a0a0b;");
        VBox content = new VBox(20);
        content.setPadding(new Insets(28,32,32,32));
        content.setStyle("-fx-background-color:#0a0a0b;");
        content.getChildren().addAll(buildNotifBar(), buildHeader(), buildSummaryCards(), buildToolbar(), buildTableCard());
        ScrollPane sc = new ScrollPane(content);
        sc.setFitToWidth(true);
        sc.setStyle("-fx-background:#0a0a0b;-fx-background-color:#0a0a0b;");
        root.setCenter(sc);
        loadData();
    }

    public Node getRoot() { return root; }

    // ---------- Notification bar ----------
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

    // ---------- Header with buttons ----------
    private HBox buildHeader() {
        Label title = new Label("FIGHTER RANKINGS");
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
                        notify("All rankings recalculated from fight results!");
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

    // ---------- Summary cards (top 3 fighters) ----------
    private HBox buildSummaryCards() {
        HBox cards = new HBox(16);
        cards.setPadding(new Insets(0,0,8,0));

        List<Fighter> topFighters = rankingService.getRankedFighters();
        for (int i = 0; i < Math.min(3, topFighters.size()); i++) {
            Fighter f = topFighters.get(i);
            String medal = i == 0 ? "🥇 " : i == 1 ? "🥈 " : "🥉 ";
            String label = medal + "#" + (i+1) + " " + f.getFullName();
            String value = String.format("%.0f pts", f.getEloRating() + f.getPerformanceScore() * 10);
            String color = i == 0 ? "#f5a623" : i == 1 ? "#aaa" : "#cd7f32";
            VBox card = statCard(label, value, color);
            cards.getChildren().add(card);
            HBox.setHgrow(card, Priority.ALWAYS);
        }
        return cards;
    }

    private VBox statCard(String label, String value, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#888;-fx-font-size:11px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:" + color + ";-fx-font-size:22px;-fx-font-weight:bold;");
        VBox card = new VBox(4, lbl, val);
        card.setStyle("-fx-background-color:#16161b;-fx-border-color:#222228;-fx-border-width:1;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:16 18;");
        return card;
    }

    // ---------- Search toolbar ----------
    private HBox buildToolbar() {
        searchField = new TextField();
        searchField.setPromptText("🔍  Search fighter...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((o, ov, nv) -> applyFilter());
        return new HBox(10, searchField);
    }

    // ---------- Main table with advanced columns ----------
    private VBox buildTableCard() {
        tableView = new TableView<>();
        tableView.getStyleClass().add("table-view");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);

        // Rank column (dynamic)
        TableColumn<Fighter, Number> rankCol = new TableColumn<>("RANK");
        rankCol.setMinWidth(60);
        rankCol.setMaxWidth(80);
        rankCol.setCellValueFactory(cellData -> {
            int idx = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(idx);
        });
        rankCol.setCellFactory(col -> new TableCell<Fighter, Number>() {
            @Override
            protected void updateItem(Number rank, boolean empty) {
                super.updateItem(rank, empty);
                if (empty || rank == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText("#" + rank.intValue());
                if (rank.intValue() == 1)
                    setStyle("-fx-text-fill:#f5a623;-fx-font-weight:bold;-fx-font-size:14px;");
                else if (rank.intValue() == 2)
                    setStyle("-fx-text-fill:#aaa;-fx-font-weight:bold;");
                else if (rank.intValue() == 3)
                    setStyle("-fx-text-fill:#cd7f32;-fx-font-weight:bold;");
                else
                    setStyle("-fx-text-fill:#dde1f0;");
            }
        });

        // Fighter name
        TableColumn<Fighter, String> nameCol = new TableColumn<>("FIGHTER");
        nameCol.setPrefWidth(180);
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));

        // Weight class
        TableColumn<Fighter, String> weightCol = new TableColumn<>("WEIGHT CLASS");
        weightCol.setPrefWidth(120);
        weightCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeightClass()));

        // ELO Rating (new)
        TableColumn<Fighter, Number> eloCol = new TableColumn<>("ELO");
        eloCol.setPrefWidth(80);
        eloCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getEloRating()));

        // Performance Score (new)
        TableColumn<Fighter, Number> perfCol = new TableColumn<>("PERF.");
        perfCol.setPrefWidth(80);
        perfCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getPerformanceScore()));

        // Win Streak (new)
        TableColumn<Fighter, Number> streakCol = new TableColumn<>("STREAK");
        streakCol.setPrefWidth(70);
        streakCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getWinStreak()));

        // Strength of Schedule (new)
        TableColumn<Fighter, Number> sosCol = new TableColumn<>("SOS");
        sosCol.setPrefWidth(70);
        sosCol.setCellValueFactory(c -> new SimpleIntegerProperty((int) c.getValue().getStrengthOfSchedule()));

        // Traditional stats
        TableColumn<Fighter, Number> winsCol = new TableColumn<>("WINS");
        winsCol.setPrefWidth(70);
        winsCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getWins()));

        TableColumn<Fighter, Number> lossesCol = new TableColumn<>("LOSSES");
        lossesCol.setPrefWidth(70);
        lossesCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLosses()));

        tableView.getColumns().addAll(rankCol, nameCol, weightCol, eloCol, perfCol, streakCol, sosCol, winsCol, lossesCol);

        VBox card = new VBox(tableView);
        card.setStyle("-fx-background-color:#16161b;-fx-border-color:#222228;-fx-border-width:1;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:20;");
        return card;
    }

    private void loadData() {
        try {
            List<Fighter> fighters = rankingService.getRankedFighters();
            masterList.setAll(fighters);
            applyFilter();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load rankings: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String query = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        List<Fighter> filtered = masterList.stream()
                .filter(f -> query.isEmpty() || f.getFullName().toLowerCase().contains(query))
                .collect(Collectors.toList());
        tableView.setItems(FXCollections.observableArrayList(filtered));
    }

    // ---------- CSV Export ----------
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Rankings as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Rank,Fighter,Weight,ELO,Performance,Streak,SOS,Wins,Losses");
                int rank = 1;
                for (Fighter f : tableView.getItems()) {
                    writer.printf(
                        "%d,\"%s\",\"%s\",%d,%d,%d,%d,%d,%d%n",
                        rank++,
                        f.getFullName().replace("\"", "\"\""),
                        f.getWeightClass() == null ? "" : f.getWeightClass().replace("\"", "\"\""),
                        (int) f.getEloRating(),
                        (int) f.getPerformanceScore(),
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