package tn.smartfight.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import tn.smartfight.dao.PredictionDao;

import java.util.List;

public class LeaderboardController {

    @FXML private TableView<Object[]> leaderTable;
    @FXML private TableColumn<Object[], Number> colRank;
    @FXML private TableColumn<Object[], String> colUser;
    @FXML private TableColumn<Object[], Number> colPoints;
    @FXML private TableColumn<Object[], String> colStatus;
    @FXML private HBox   podiumRow;
    @FXML private Label  statusLabel;

    private final PredictionDao dao = new PredictionDao();

    @FXML
    public void initialize() {
        colRank.setCellValueFactory(cd -> new SimpleIntegerProperty((int) cd.getValue()[0]));
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                int rank = item.intValue();
                setText(rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank));
            }
        });

        colUser.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[1]));

        colPoints.setCellValueFactory(cd -> new SimpleIntegerProperty((int) cd.getValue()[2]));
        colPoints.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.intValue() + " PTS");
                setStyle("-fx-font-weight:bold;");
            }
        });

        colStatus.setCellValueFactory(cd -> {
            int rank = (int) cd.getValue()[0];
            int pts  = (int) cd.getValue()[2];
            return new SimpleStringProperty(tierLabel(rank, pts));
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item) {
                    case "VIP CONTENDER" -> "#f59e0b";
                    case "PRO ANALYST"   -> "#8b5cf6";
                    default              -> "#a1a1aa";
                };
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-size:11px;");
            }
        });

        load();
    }

    @FXML private void onRefresh() { load(); }

    private void load() {
        statusLabel.setText("Loading…");
        Task<List<Object[]>> task = new Task<>() {
            @Override protected List<Object[]> call() { return dao.findLeaderboard(50); }
        };
        task.setOnSucceeded(e -> {
            List<Object[]> rows = task.getValue();
            leaderTable.setItems(FXCollections.observableArrayList(rows));
            buildPodium(rows);
            statusLabel.setText(rows.size() + " fans ranked");
        });
        task.setOnFailed(e -> statusLabel.setText("Load failed."));
        new Thread(task, "leaderboard-load").start();
    }

    private void buildPodium(List<Object[]> rows) {
        podiumRow.getChildren().clear();
        if (rows.size() < 1) return;

        // Podium visual order: 2nd, 1st, 3rd
        int[] order = { 1, 0, 2 };
        String[] heights = { "110", "140", "90" };
        String[] colors  = { "#9ca3af", "#f59e0b", "#cd7c2f" };
        String[] crowns  = { "🥈", "🏆", "🥉" };

        for (int i = 0; i < Math.min(order.length, rows.size()); i++) {
            Object[] row = rows.get(order[i]);
            int rank  = (int) row[0];
            String username = (String) row[1];
            int pts   = (int) row[2];
            podiumRow.getChildren().add(
                    buildPodiumCard(rank, username, pts, heights[i], colors[i], crowns[i]));
        }
    }

    private VBox buildPodiumCard(int rank, String username, int pts,
                                  String height, String color, String crown) {
        // Avatar circle
        Circle avatar = new Circle(28);
        avatar.setStyle("-fx-fill:#27272a;");
        StackPane avatarPane = new StackPane(avatar);
        Label initLbl = new Label(username.substring(0, 1).toUpperCase());
        initLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-size:18px;-fx-font-weight:bold;");
        avatarPane.getChildren().add(initLbl);

        Label crownLbl = new Label(crown);
        crownLbl.setStyle("-fx-font-size:22px;");

        Label userLbl = new Label(username);
        userLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-weight:bold;-fx-font-size:13px;");
        userLbl.setWrapText(true);
        userLbl.setAlignment(Pos.CENTER);

        Label ptsLbl = new Label(pts + " PTS");
        ptsLbl.setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-size:12px;");

        // Podium block
        Region block = new Region();
        block.setPrefHeight(Double.parseDouble(height));
        block.setMinWidth(90);
        block.setMaxWidth(90);
        block.setStyle("-fx-background-color:" + color + ";-fx-background-radius:4 4 0 0;-fx-opacity:0.25;");

        Label rankNumLbl = new Label(String.valueOf(rank));
        rankNumLbl.setStyle("-fx-text-fill:" + color + ";-fx-font-size:24px;-fx-font-weight:bold;");

        StackPane blockPane = new StackPane(block, rankNumLbl);

        VBox card = new VBox(6, crownLbl, avatarPane, userLbl, ptsLbl, blockPane);
        card.setAlignment(Pos.BOTTOM_CENTER);
        card.setPadding(new Insets(0, 8, 0, 8));
        return card;
    }

    private String tierLabel(int rank, int pts) {
        if (rank <= 3)   return "VIP CONTENDER";
        if (pts > 500)   return "PRO ANALYST";
        return "CONTENDER";
    }
}
