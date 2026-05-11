package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.smartfight.config.DBConnection;
import tn.smartfight.model.FightResult;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardController {
    private static final Logger LOG = Logger.getLogger(DashboardController.class.getName());

    /* Financial summary */
    @FXML private Label lblGrossRevenue;
    @FXML private Label lblDeposits;
    @FXML private Label lblNetPL;
    @FXML private Label lblPendingPayouts;

    /* Fan Favorite Center */
    @FXML private HBox proposalsBox;
    @FXML private Label proposalsPlaceholder;

    /* Charts */
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> userChart;
    @FXML private Label lblTotalFans;
    @FXML private Label lblGrowthPct;

    /* Recent activity */
    @FXML private TableView<FightResult> recentTable;
    @FXML private TableColumn<FightResult, String> colEvent;
    @FXML private TableColumn<FightResult, String> colBoxers;
    @FXML private TableColumn<FightResult, String> colStatus;
    @FXML private Label lblEventCount;
    @FXML private Label lblFightCount;

    private final DataSource ds = DBConnection.getDataSource();

    /* shell back-reference for navigation */
    private AdminShellController shell;
    public void setShell(AdminShellController shell) { this.shell = shell; }

    @FXML
    public void initialize() {
        setupRecentTable();
        loadData();
    }

    private void setupRecentTable() {
        colEvent.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        colBoxers.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getFighter1Name() + " vs " + cd.getValue().getFighter2Name()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().add(
                        "COMPLETED".equalsIgnoreCase(v) ? "badge-green" :
                        "PENDING".equalsIgnoreCase(v)   ? "badge-amber" : "badge-gray");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void loadData() {
        Task<DashData> task = new Task<>() {
            @Override protected DashData call() throws Exception { return fetch(); }
        };
        task.setOnSucceeded(e -> populate(task.getValue()));
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "Dashboard load failed", task.getException()));
        new Thread(task, "dash-load").start();
    }

    private void populate(DashData d) {
        /* Section B — financials */
        lblGrossRevenue.setText("$" + fmt(d.grossRevenue));
        lblDeposits.setText("$" + fmt(d.deposits));
        lblNetPL.setText("$" + fmt(d.grossRevenue.subtract(d.pendingPayouts)));
        lblPendingPayouts.setText("$" + fmt(d.pendingPayouts));

        /* Section C — proposals */
        proposalsBox.getChildren().clear();
        if (d.proposals.isEmpty()) {
            Label lbl = new Label("No match proposals yet.");
            lbl.getStyleClass().add("text-muted");
            proposalsBox.getChildren().add(lbl);
        } else {
            String[] rankLabels = {"#1 MOST WANTED", "#2 MOST WANTED", "#3 MOST WANTED"};
            String[] rankStyles = {"badge-red", "badge-amber", "badge-gray"};
            for (int i = 0; i < Math.min(3, d.proposals.size()); i++) {
                Object[] p = d.proposals.get(i);
                VBox card = buildProposalCard(
                        rankLabels[i], rankStyles[i],
                        (String) p[0], (String) p[1],
                        (String) p[2], (String) p[3],
                        ((Number) p[4]).intValue());
                HBox.setHgrow(card, Priority.ALWAYS);
                proposalsBox.getChildren().add(card);
            }
        }

        /* Section D — charts */
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue $");
        d.dailyRevLabels.forEach(day -> revSeries.getData().add(
                new XYChart.Data<>(day, d.dailyRevValues.get(d.dailyRevLabels.indexOf(day)))));
        revenueChart.getData().clear();
        revenueChart.getData().add(revSeries);
        revenueChart.setLegendVisible(false);
        revenueChart.setAnimated(false);

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        userSeries.setName("New Fans");
        d.dailyRegLabels.forEach(day -> userSeries.getData().add(
                new XYChart.Data<>(day, d.dailyRegValues.get(d.dailyRegLabels.indexOf(day)))));
        userChart.getData().clear();
        userChart.getData().add(userSeries);
        userChart.setLegendVisible(false);
        userChart.setAnimated(false);

        lblTotalFans.setText(String.valueOf(d.totalFans));
        int growth = d.totalFans > 0 && d.prevFans > 0
                ? (int) Math.round(((d.totalFans - d.prevFans) * 100.0) / d.prevFans) : 0;
        lblGrowthPct.setText((growth >= 0 ? "+" : "") + growth + "%");

        /* Section E — recent activity */
        recentTable.setItems(FXCollections.observableArrayList(d.recentFights));
        lblEventCount.setText(String.valueOf(d.eventCount));
        lblFightCount.setText(String.valueOf(d.fightCount));
    }

    private VBox buildProposalCard(String rank, String rankStyle,
                                    String f1Name, String f1Div,
                                    String f2Name, String f2Div,
                                    int votes) {
        VBox card = new VBox(10);
        card.getStyleClass().add("proposal-card");

        Label rankBadge = new Label(rank);
        rankBadge.getStyleClass().add(rankStyle);

        VBox f1Box = new VBox(2,
                new Label(f1Name.toUpperCase()) {{
                    setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#ffffff;"); }},
                new Label(f1Div) {{ getStyleClass().add("text-muted"); }});

        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:#dc2626;");

        VBox f2Box = new VBox(2,
                new Label(f2Name.toUpperCase()) {{
                    setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#ffffff;"); }},
                new Label(f2Div) {{ getStyleClass().add("text-muted"); }});

        Label voteBadge = new Label("🗳 " + votes + " votes");
        voteBadge.setStyle("-fx-font-size:11;-fx-text-fill:#9ca3af;");

        card.getChildren().addAll(rankBadge, f1Box, vs, f2Box, voteBadge);
        return card;
    }

    /* ── Navigation actions ─────────────────────────────── */

    @FXML private void onScheduleFight() { navigateTo("/tn/smartfight/views/admin/ResultsList.fxml"); }
    @FXML private void onManageEvents()  { navigateTo("/tn/smartfight/views/admin/EventsList.fxml"); }
    @FXML private void onBoxerRoster()   { navigateTo("/tn/smartfight/views/admin/FightersList.fxml"); }
    @FXML private void onPublishNews()   { navigateTo("/tn/smartfight/views/admin/AdminBlogList.fxml"); }
    @FXML private void onCreateFanEvent() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Fan Event creation uses the top-3 voted proposals.\nGo to Events → Create Event.");
        a.setHeaderText("Create Fan Event");
        a.showAndWait();
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node pane = loader.load();
            var host = recentTable.getScene().lookup("#contentHost");
            if (host instanceof StackPane sp) {
                sp.getChildren().setAll(pane);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Dashboard navigation failed: " + fxmlPath, e);
        }
    }

    /* ── Data fetch ────────────────────────────────────── */

    private DashData fetch() throws Exception {
        DashData d = new DashData();
        try (Connection c = ds.getConnection()) {

            /* financial — gross revenue from confirmed bookings */
            String sqlRev = "SELECT COALESCE(SUM(total_price),0) FROM event_booking WHERE booking_status='CONFIRMED'";
            try (PreparedStatement ps = c.prepareStatement(sqlRev); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.grossRevenue = rs.getBigDecimal(1);
            }

            /* pending payouts = unpaid contracts */
            String sqlPay = "SELECT COALESCE(SUM(base_pay + win_bonus),0) FROM fighter_contract WHERE is_paid=0";
            try (PreparedStatement ps = c.prepareStatement(sqlPay); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.pendingPayouts = rs.getBigDecimal(1);
            }

            /* deposits = pending bookings (conceptual) */
            d.deposits = BigDecimal.ZERO;

            /* top 3 voted proposals */
            String sqlProp = "SELECT CONCAT(f1.firstName,' ',f1.lastName), " +
                    "COALESCE(wd1.name,'?'), " +
                    "CONCAT(f2.firstName,' ',f2.lastName), " +
                    "COALESCE(wd2.name,'?'), " +
                    "mp.vote_count " +
                    "FROM match_proposal mp " +
                    "JOIN fighters f1 ON f1.fighterId = mp.fighter1_id " +
                    "JOIN fighters f2 ON f2.fighterId = mp.fighter2_id " +
                    "LEFT JOIN weight_division wd1 ON wd1.id = f1.weight_division_id " +
                    "LEFT JOIN weight_division wd2 ON wd2.id = f2.weight_division_id " +
                    "WHERE mp.status IN ('PENDING','APPROVED') " +
                    "ORDER BY mp.vote_count DESC LIMIT 3";
            try (PreparedStatement ps = c.prepareStatement(sqlProp); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.proposals.add(new Object[]{
                            rs.getString(1), rs.getString(2),
                            rs.getString(3), rs.getString(4),
                            rs.getInt(5)});
                }
            }

            /* daily revenue last 7 days */
            String sqlD7 = "SELECT DATE_FORMAT(booking_date,'%a') AS day, " +
                    "COALESCE(SUM(total_price),0) AS rev " +
                    "FROM event_booking WHERE booking_status='CONFIRMED' " +
                    "AND booking_date >= CURDATE() - INTERVAL 7 DAY " +
                    "GROUP BY DATE(booking_date) ORDER BY booking_date";
            try (PreparedStatement ps = c.prepareStatement(sqlD7); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.dailyRevLabels.add(rs.getString("day"));
                    d.dailyRevValues.add(rs.getBigDecimal("rev").doubleValue());
                }
            }

            /* daily user registrations last 7 days */
            String sqlU7 = "SELECT DATE_FORMAT(createdDate,'%a') AS day, COUNT(*) AS cnt " +
                    "FROM users WHERE createdDate >= CURDATE() - INTERVAL 7 DAY " +
                    "GROUP BY DATE(createdDate) ORDER BY createdDate";
            try (PreparedStatement ps = c.prepareStatement(sqlU7); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.dailyRegLabels.add(rs.getString("day"));
                    d.dailyRegValues.add((double) rs.getInt("cnt"));
                }
            }

            /* total fans */
            String sqlFans = "SELECT COUNT(*) FROM users";
            try (PreparedStatement ps = c.prepareStatement(sqlFans); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.totalFans = rs.getInt(1);
            }

            /* fans last 30 days (for growth %) */
            String sqlPrevFans = "SELECT COUNT(*) FROM users WHERE createdDate < CURDATE() - INTERVAL 7 DAY";
            try (PreparedStatement ps = c.prepareStatement(sqlPrevFans); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.prevFans = rs.getInt(1);
            }

            /* recent 5 fights */
            String sqlFights = "SELECT fr.resultId, fr.status, " +
                    "e.eventName, " +
                    "CONCAT(f1.firstName,' ',f1.lastName) AS fighter1Name, " +
                    "CONCAT(f2.firstName,' ',f2.lastName) AS fighter2Name " +
                    "FROM fight_results fr " +
                    "LEFT JOIN events e ON e.eventId=fr.eventId " +
                    "LEFT JOIN fighters f1 ON f1.fighterId=fr.fighter1Id " +
                    "LEFT JOIN fighters f2 ON f2.fighterId=fr.fighter2Id " +
                    "ORDER BY fr.resultId DESC LIMIT 5";
            try (PreparedStatement ps = c.prepareStatement(sqlFights); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FightResult fr = new FightResult();
                    fr.setResultId(rs.getInt("resultId"));
                    fr.setStatus(rs.getString("status"));
                    fr.setEventName(rs.getString("eventName"));
                    fr.setFighter1Name(rs.getString("fighter1Name"));
                    fr.setFighter2Name(rs.getString("fighter2Name"));
                    d.recentFights.add(fr);
                }
            }

            /* counts */
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM events");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.eventCount = rs.getInt(1);
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM fight_results");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) d.fightCount = rs.getInt(1);
            }
        }
        return d;
    }

    private static String fmt(BigDecimal v) {
        return v != null ? String.format("%,.2f", v.doubleValue()) : "0.00";
    }

    private static class DashData {
        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal deposits     = BigDecimal.ZERO;
        BigDecimal pendingPayouts = BigDecimal.ZERO;
        List<Object[]> proposals = new ArrayList<>();
        List<String> dailyRevLabels = new ArrayList<>();
        List<Double> dailyRevValues = new ArrayList<>();
        List<String> dailyRegLabels = new ArrayList<>();
        List<Double> dailyRegValues = new ArrayList<>();
        int totalFans = 0, prevFans = 0;
        List<FightResult> recentFights = new ArrayList<>();
        int eventCount = 0, fightCount = 0;
    }
}
