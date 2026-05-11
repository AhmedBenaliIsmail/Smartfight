package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.smartfight.config.DBConnection;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinanceController {
    private static final Logger LOG = Logger.getLogger(FinanceController.class.getName());

    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalBookings;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();

    @FXML
    public void initialize() { loadData(); }

    @FXML
    private void onRefresh() { loadData(); }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<FinanceData> task = new Task<>() {
            @Override protected FinanceData call() throws Exception { return fetchData(); }
        };
        task.setOnSucceeded(e -> {
            FinanceData d = task.getValue();
            populateCharts(d);
            statusLabel.setText("Data loaded.");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Finance load failed", task.getException());
            statusLabel.setText("Failed to load finance data.");
        });
        new Thread(task, "finance-load").start();
    }

    private FinanceData fetchData() throws Exception {
        FinanceData d = new FinanceData();
        try (Connection c = ds.getConnection()) {

            // daily revenue last 30 days
            String sql30 = "SELECT DATE(booking_date) AS day, COALESCE(SUM(total_price),0) AS revenue " +
                    "FROM event_booking WHERE booking_status='CONFIRMED' " +
                    "AND booking_date >= CURDATE() - INTERVAL 30 DAY " +
                    "GROUP BY DATE(booking_date) ORDER BY day";
            try (PreparedStatement ps = c.prepareStatement(sql30);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.dailyLabels.add(rs.getString("day"));
                    d.dailyRevenue.add(rs.getBigDecimal("revenue"));
                }
            }

            // by ticket type
            String sqlType = "SELECT ticket_type, COALESCE(SUM(total_price),0) AS total " +
                    "FROM event_booking WHERE booking_status='CONFIRMED' GROUP BY ticket_type";
            try (PreparedStatement ps = c.prepareStatement(sqlType);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.typeLabels.add(rs.getString("ticket_type").replace("_", " "));
                    d.typeRevenue.add(rs.getBigDecimal("total"));
                }
            }

            // by event (top 10)
            String sqlEvent = "SELECT e.eventName, COALESCE(SUM(eb.total_price),0) AS total " +
                    "FROM event_booking eb JOIN events e ON e.eventId=eb.event_id " +
                    "WHERE eb.booking_status='CONFIRMED' " +
                    "GROUP BY e.eventId, e.eventName ORDER BY total DESC LIMIT 10";
            try (PreparedStatement ps = c.prepareStatement(sqlEvent);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    d.eventLabels.add(rs.getString("eventName"));
                    d.eventRevenue.add(rs.getBigDecimal("total"));
                }
            }

            // totals
            String sqlTotal = "SELECT COALESCE(SUM(total_price),0) AS rev, COUNT(*) AS cnt " +
                    "FROM event_booking WHERE booking_status='CONFIRMED'";
            try (PreparedStatement ps = c.prepareStatement(sqlTotal);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.totalRevenue = rs.getBigDecimal("rev");
                    d.totalBookings = rs.getInt("cnt");
                }
            }
        }
        return d;
    }

    private void populateCharts(FinanceData d) {
        lblTotalRevenue.setText("$" + (d.totalRevenue != null ? d.totalRevenue.toPlainString() : "0.00"));
        lblTotalBookings.setText(String.valueOf(d.totalBookings));

        // Line chart
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue");
        for (int i = 0; i < d.dailyLabels.size(); i++) {
            revSeries.getData().add(new XYChart.Data<>(d.dailyLabels.get(i), d.dailyRevenue.get(i)));
        }
        revenueChart.getData().clear();
        revenueChart.getData().add(revSeries);

        // Pie chart
        List<PieChart.Data> pieData = new ArrayList<>();
        for (int i = 0; i < d.typeLabels.size(); i++) {
            pieData.add(new PieChart.Data(d.typeLabels.get(i), d.typeRevenue.get(i).doubleValue()));
        }
        pieChart.setData(FXCollections.observableArrayList(pieData));

        // Bar chart
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Revenue");
        for (int i = 0; i < d.eventLabels.size(); i++) {
            String label = d.eventLabels.get(i).length() > 20
                    ? d.eventLabels.get(i).substring(0, 20) + "..." : d.eventLabels.get(i);
            barSeries.getData().add(new XYChart.Data<>(label, d.eventRevenue.get(i)));
        }
        barChart.getData().clear();
        barChart.getData().add(barSeries);
    }

    private static class FinanceData {
        List<String> dailyLabels = new ArrayList<>();
        List<BigDecimal> dailyRevenue = new ArrayList<>();
        List<String> typeLabels = new ArrayList<>();
        List<BigDecimal> typeRevenue = new ArrayList<>();
        List<String> eventLabels = new ArrayList<>();
        List<BigDecimal> eventRevenue = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalBookings = 0;
    }
}
