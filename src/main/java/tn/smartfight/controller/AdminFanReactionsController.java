package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.config.DBConnection;

import javax.sql.DataSource;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminFanReactionsController {
    private static final Logger LOG = Logger.getLogger(AdminFanReactionsController.class.getName());

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @FXML private Label totalLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> fightFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private FlowPane reactionsFlow;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();
    private List<ReactionRow> allReactions = new ArrayList<>();

    @FXML
    public void initialize() {
        typeFilter.getSelectionModel().select("ALL");
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        loadFights();
        load();
    }

    @FXML private void onFilter() { applyFilter(); }

    private void loadFights() {
        Task<List<String>> t = new Task<>() {
            @Override protected List<String> call() throws Exception {
                List<String> items = new ArrayList<>();
                items.add("ALL");
                String sql =
                    "SELECT CONCAT(f1.firstName,' ',f1.lastName,' vs ',f2.firstName,' ',f2.lastName) AS matchup " +
                    "FROM fight_results fr " +
                    "JOIN fighters f1 ON f1.fighterId=fr.fighter1Id " +
                    "JOIN fighters f2 ON f2.fighterId=fr.fighter2Id " +
                    "ORDER BY fr.fightDate DESC";
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(rs.getString("matchup"));
                }
                return items;
            }
        };
        t.setOnSucceeded(e -> {
            fightFilter.setItems(FXCollections.observableArrayList(t.getValue()));
            fightFilter.getSelectionModel().select("ALL");
        });
        new Thread(t, "reactions-fights").start();
    }

    private void load() {
        Task<List<ReactionRow>> task = new Task<>() {
            @Override protected List<ReactionRow> call() throws Exception {
                String sql =
                    "SELECT r.id, u.username, r.reaction_type, r.comment, r.reacted_at, " +
                    "       r.is_pinned, " +
                    "       CONCAT(f1.firstName,' ',f1.lastName,' vs ',f2.firstName,' ',f2.lastName) AS matchup " +
                    "FROM fan_reaction r " +
                    "JOIN users u ON u.userId = r.fan_id " +
                    "JOIN fight_results fr ON fr.resultId = r.fight_result_id " +
                    "JOIN fighters f1 ON f1.fighterId = fr.fighter1Id " +
                    "JOIN fighters f2 ON f2.fighterId = fr.fighter2Id " +
                    "WHERE r.is_deleted = 0 " +
                    "ORDER BY r.reacted_at DESC";
                List<ReactionRow> rows = new ArrayList<>();
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("reacted_at");
                        String time = ts != null ? ts.toLocalDateTime().format(FMT) : "";
                        rows.add(new ReactionRow(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("reaction_type"),
                                rs.getString("comment"),
                                time,
                                rs.getInt("is_pinned") == 1,
                                rs.getString("matchup")
                        ));
                    }
                }
                return rows;
            }
        };
        task.setOnSucceeded(e -> {
            allReactions = task.getValue();
            totalLabel.setText(allReactions.size() + " reactions");
            applyFilter();
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load failed", task.getException());
            statusLabel.setText("Failed to load reactions.");
        });
        new Thread(task, "reactions-load").start();
    }

    private void applyFilter() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String type   = typeFilter.getValue();
        String fight  = fightFilter.getValue();

        reactionsFlow.getChildren().clear();
        int count = 0;
        for (ReactionRow r : allReactions) {
            if (!"ALL".equals(type)  && type  != null && !type.equals(r.reactionType))  continue;
            if (!"ALL".equals(fight) && fight != null && !fight.equals(r.matchup))       continue;
            if (!search.isEmpty()) {
                String hay = (nvl(r.username) + " " + nvl(r.comment) + " " + nvl(r.matchup)).toLowerCase();
                if (!hay.contains(search)) continue;
            }
            reactionsFlow.getChildren().add(buildCard(r));
            count++;
        }
        statusLabel.setText(count + " shown");
    }

    private VBox buildCard(ReactionRow r) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        String borderStyle = r.pinned
                ? "-fx-border-color:#f59e0b;-fx-border-width:1;-fx-border-radius:10;"
                : "-fx-border-color:#2d2d2d;-fx-border-width:1;-fx-border-radius:10;";
        card.setStyle("-fx-background-color:#1a1a1a;-fx-background-radius:10;-fx-padding:16;" + borderStyle);

        /* Top row: user + timestamp */
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(r.username.substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color:#374151;-fx-text-fill:white;-fx-font-size:13;" +
                "-fx-font-weight:bold;-fx-background-radius:20;-fx-min-width:36;-fx-min-height:36;" +
                "-fx-max-width:36;-fx-max-height:36;-fx-alignment:center;");

        VBox userInfo = new VBox(2);
        Label userLbl = new Label(r.username);
        userLbl.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13;");
        Label timeLbl = new Label(r.time);
        timeLbl.setStyle("-fx-text-fill:#6b7280;-fx-font-size:11;");
        userInfo.getChildren().addAll(userLbl, timeLbl);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        if (r.pinned) {
            Label pinnedBadge = new Label("📌 PINNED");
            pinnedBadge.setStyle("-fx-text-fill:#f59e0b;-fx-font-size:10;-fx-font-weight:bold;");
            topRow.getChildren().addAll(avatar, userInfo, pinnedBadge);
        } else {
            topRow.getChildren().addAll(avatar, userInfo);
        }

        /* Fight context */
        Label fightLbl = new Label(r.matchup);
        fightLbl.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:11;");
        fightLbl.setWrapText(true);

        /* Reaction type badge */
        Label typeBadge = new Label(r.reactionType);
        typeBadge.getStyleClass().add(reactionCss(r.reactionType));

        /* Comment */
        Label comment = new Label(r.comment != null ? r.comment : "");
        comment.setStyle("-fx-text-fill:#e5e7eb;-fx-font-size:13;");
        comment.setWrapText(true);

        /* Action buttons */
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button pinBtn = new Button(r.pinned ? "📌 UNPIN" : "📌 PIN");
        pinBtn.getStyleClass().add("button");
        pinBtn.setStyle("-fx-padding:4 10;-fx-font-size:11;");
        pinBtn.setOnAction(e -> togglePin(r, !r.pinned));

        Button delBtn = new Button("🗑");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-padding:4 10;-fx-font-size:11;");
        delBtn.setOnAction(e -> deleteReaction(r));

        actions.getChildren().addAll(pinBtn, delBtn);

        card.getChildren().addAll(topRow, fightLbl, typeBadge, comment, actions);
        return card;
    }

    private void togglePin(ReactionRow r, boolean pin) {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "UPDATE fan_reaction SET is_pinned=? WHERE id=?")) {
                    ps.setInt(1, pin ? 1 : 0);
                    ps.setInt(2, r.id);
                    ps.executeUpdate();
                }
                return null;
            }
        };
        t.setOnSucceeded(e -> load());
        t.setOnFailed(e -> statusLabel.setText("Pin update failed."));
        new Thread(t, "reaction-pin").start();
    }

    private void deleteReaction(ReactionRow r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this reaction from " + r.username + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (!btn.getButtonData().isDefaultButton()) return;
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    try (Connection c = ds.getConnection();
                         PreparedStatement ps = c.prepareStatement(
                                 "UPDATE fan_reaction SET is_deleted=1 WHERE id=?")) {
                        ps.setInt(1, r.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
            };
            t.setOnSucceeded(e -> load());
            t.setOnFailed(e -> statusLabel.setText("Delete failed."));
            new Thread(t, "reaction-delete").start();
        });
    }

    private static String reactionCss(String type) {
        if (type == null) return "badge-gray";
        return switch (type.toUpperCase()) {
            case "FIRE"     -> "badge-orange";
            case "LOVE"     -> "badge-red";
            case "SHOCK"    -> "badge-amber";
            case "DOMINANT" -> "badge-blue";
            case "UPSET"    -> "badge-purple";
            default         -> "badge-gray";
        };
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    record ReactionRow(int id, String username, String reactionType, String comment,
                       String time, boolean pinned, String matchup) {}
}
