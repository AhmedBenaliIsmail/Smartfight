package tn.smartfight.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.smartfight.config.DBConnection;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminLeaderboardController {

    @FXML private Label rank1Init, rank1Name, rank1Pts;
    @FXML private Label rank2Init, rank2Name, rank2Pts;
    @FXML private Label rank3Init, rank3Name, rank3Pts;

    @FXML private TextField searchField;
    @FXML private TableView<LeaderRow> leaderTable;
    @FXML private TableColumn<LeaderRow, Number>  colRank;
    @FXML private TableColumn<LeaderRow, String>  colUser;
    @FXML private TableColumn<LeaderRow, Number>  colPred;
    @FXML private TableColumn<LeaderRow, Number>  colPoints;
    @FXML private TableColumn<LeaderRow, String>  colStatus;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();
    private List<LeaderRow> allRows = new ArrayList<>();

    @FXML
    public void initialize() {
        colRank  .setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().rank));
        colUser  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().username));
        colPred  .setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().predCount));
        colPoints.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().points));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(statusLabel(c.getValue().rank)));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(statusCss(s));
                setGraphic(badge);
                setText(null);
            }
        });

        colUser.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                setText(s);
                setStyle("-fx-text-fill:#ffffff;-fx-font-weight:bold;");
            }
        });

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        load();
    }

    @FXML private void onSearch() { applyFilter(); }

    private void load() {
        Task<List<LeaderRow>> task = new Task<>() {
            @Override protected List<LeaderRow> call() throws Exception {
                String sql =
                    "SELECT u.userId, u.username, u.predictionPoints, " +
                    "       COUNT(p.predictionId) AS pred_count " +
                    "FROM users u " +
                    "LEFT JOIN predictions p ON p.userId = u.userId " +
                    "GROUP BY u.userId, u.username, u.predictionPoints " +
                    "ORDER BY u.predictionPoints DESC " +
                    "LIMIT 50";
                List<LeaderRow> rows = new ArrayList<>();
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    int rank = 1;
                    while (rs.next()) {
                        rows.add(new LeaderRow(
                                rank++,
                                rs.getString("username"),
                                rs.getInt("pred_count"),
                                rs.getInt("predictionPoints")
                        ));
                    }
                }
                return rows;
            }
        };
        task.setOnSucceeded(e -> {
            allRows = task.getValue();
            populatePodium();
            applyFilter();
            statusLabel.setText(allRows.size() + " contenders ranked");
        });
        task.setOnFailed(e -> statusLabel.setText("Failed to load leaderboard."));
        new Thread(task, "leaderboard-load").start();
    }

    private void populatePodium() {
        if (allRows.size() >= 1) {
            LeaderRow r1 = allRows.get(0);
            rank1Init.setText(initials(r1.username));
            rank1Name.setText(r1.username.toUpperCase());
            rank1Pts.setText(r1.points + " PTS");
        }
        if (allRows.size() >= 2) {
            LeaderRow r2 = allRows.get(1);
            rank2Init.setText(initials(r2.username));
            rank2Name.setText(r2.username.toUpperCase());
            rank2Pts.setText(r2.points + " PTS");
        }
        if (allRows.size() >= 3) {
            LeaderRow r3 = allRows.get(2);
            rank3Init.setText(initials(r3.username));
            rank3Name.setText(r3.username.toUpperCase());
            rank3Pts.setText(r3.points + " PTS");
        }
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        ObservableList<LeaderRow> filtered = FXCollections.observableArrayList();
        for (LeaderRow r : allRows) {
            if (q.isEmpty() || r.username.toLowerCase().contains(q)) {
                filtered.add(r);
            }
        }
        leaderTable.setItems(filtered);
    }

    private static String statusLabel(int rank) {
        if (rank <= 10)  return "IN CONTENTION";
        if (rank <= 25)  return "CONTENDER";
        return "CONTINUE";
    }

    private static String statusCss(String s) {
        return switch (s) {
            case "IN CONTENTION" -> "badge-green";
            case "CONTENDER"     -> "badge-amber";
            default              -> "badge-gray";
        };
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.split("[_\\s]+");
        if (parts.length >= 2)
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                 + String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    /* ── inner record ── */
    static final class LeaderRow {
        final int rank, predCount, points;
        final String username;
        LeaderRow(int rank, String username, int predCount, int points) {
            this.rank = rank; this.username = username;
            this.predCount = predCount; this.points = points;
        }
    }
}
