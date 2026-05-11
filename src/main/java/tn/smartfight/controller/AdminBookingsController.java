package tn.smartfight.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.smartfight.config.DBConnection;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminBookingsController {
    private static final Logger LOG = Logger.getLogger(AdminBookingsController.class.getName());

    @FXML private Label lblTotal, lblRevenue, lblConfirmed, lblCancelled;
    @FXML private ComboBox<String> eventFilter, statusFilter;
    @FXML private TableView<BookingRow> bookingsTable;
    @FXML private TableColumn<BookingRow, String> colUser, colEvent, colType,
            colEmail, colAmount, colQty, colStatus, colActions;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();
    private List<BookingRow> allBookings = new ArrayList<>();

    @FXML
    public void initialize() {
        colUser  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().username));
        colEvent .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().eventName));
        colType  .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ticketType));
        colEmail .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty("$" + String.format("%.2f", c.getValue().totalPrice)));
        colQty   .setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantity)));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().bookingStatus));

        colType.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(s.replace("_", " "));
                badge.getStyleClass().add(typeCss(s));
                setGraphic(badge);
                setText(null);
            }
        });

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

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("✖ CANCEL");
            {
                cancelBtn.getStyleClass().add("btn-danger");
                cancelBtn.setStyle("-fx-padding:3 8;-fx-font-size:11;");
                cancelBtn.setOnAction(e -> {
                    BookingRow row = getTableView().getItems().get(getIndex());
                    cancelBooking(row);
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); return; }
                BookingRow row = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                if (!"CANCELLED".equals(row.bookingStatus)) {
                    box.getChildren().add(cancelBtn);
                }
                setGraphic(box);
            }
        });

        statusFilter.getSelectionModel().select("ALL");
        loadEvents();
        load();
    }

    @FXML private void onFilter() { applyFilter(); }

    @FXML private void onCreateBooking() {
        statusLabel.setText("Manual booking creation not yet implemented.");
    }

    private void loadEvents() {
        Task<List<String>> t = new Task<>() {
            @Override protected List<String> call() throws Exception {
                List<String> items = new ArrayList<>();
                items.add("ALL");
                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "SELECT eventName FROM events ORDER BY eventDate DESC");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(rs.getString("eventName"));
                }
                return items;
            }
        };
        t.setOnSucceeded(e -> {
            eventFilter.setItems(FXCollections.observableArrayList(t.getValue()));
            eventFilter.getSelectionModel().select("ALL");
        });
        new Thread(t, "booking-events").start();
    }

    private void load() {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                loadStats();
                loadBookings();
                return null;
            }
        };
        t.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load failed", t.getException());
            statusLabel.setText("Failed to load bookings.");
        });
        new Thread(t, "bookings-load").start();
    }

    private void loadStats() throws Exception {
        String sql = "SELECT COUNT(*) AS total, " +
                "COALESCE(SUM(total_price),0) AS revenue, " +
                "SUM(CASE WHEN booking_status='CONFIRMED' THEN 1 ELSE 0 END) AS confirmed, " +
                "SUM(CASE WHEN booking_status='CANCELLED' THEN 1 ELSE 0 END) AS cancelled " +
                "FROM event_booking";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total");
                double revenue = rs.getDouble("revenue");
                int confirmed = rs.getInt("confirmed");
                int cancelled = rs.getInt("cancelled");
                javafx.application.Platform.runLater(() -> {
                    lblTotal.setText(String.valueOf(total));
                    lblRevenue.setText("$" + String.format("%.0f", revenue));
                    lblConfirmed.setText(String.valueOf(confirmed));
                    lblCancelled.setText(String.valueOf(cancelled));
                });
            }
        }
    }

    private void loadBookings() throws Exception {
        String sql =
            "SELECT eb.id, u.username, u.email, e.eventName, " +
            "       eb.ticket_type, eb.total_price, eb.ticket_quantity, eb.booking_status " +
            "FROM event_booking eb " +
            "JOIN users u ON u.userId = eb.user_id " +
            "JOIN events e ON e.eventId = eb.event_id " +
            "ORDER BY eb.created_at DESC";
        List<BookingRow> rows = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new BookingRow(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("eventName"),
                        rs.getString("ticket_type"),
                        rs.getDouble("total_price"),
                        rs.getInt("ticket_quantity"),
                        rs.getString("booking_status")
                ));
            }
        }
        javafx.application.Platform.runLater(() -> {
            allBookings = rows;
            applyFilter();
        });
    }

    private void applyFilter() {
        String ev     = eventFilter.getValue();
        String status = statusFilter.getValue();

        List<BookingRow> filtered = new ArrayList<>();
        for (BookingRow r : allBookings) {
            if (!"ALL".equals(ev) && ev != null && !ev.equals(r.eventName)) continue;
            if (!"ALL".equals(status) && status != null && !status.equals(r.bookingStatus)) continue;
            filtered.add(r);
        }
        bookingsTable.setItems(FXCollections.observableArrayList(filtered));
        statusLabel.setText(filtered.size() + " bookings");
    }

    private void cancelBooking(BookingRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel booking #" + row.id + " for " + row.username + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (!btn.getButtonData().isDefaultButton()) return;
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    try (Connection c = ds.getConnection();
                         PreparedStatement ps = c.prepareStatement(
                                 "UPDATE event_booking SET booking_status='CANCELLED', updated_at=NOW() WHERE id=?")) {
                        ps.setInt(1, row.id);
                        ps.executeUpdate();
                    }
                    return null;
                }
            };
            t.setOnSucceeded(e -> load());
            t.setOnFailed(e -> statusLabel.setText("Cancel failed."));
            new Thread(t, "booking-cancel").start();
        });
    }

    private static String typeCss(String type) {
        if (type == null) return "badge-gray";
        return switch (type) {
            case "VIP_RINGSIDE"     -> "badge-gold";
            case "PREMIUM_LOWER"   -> "badge-purple";
            case "REGULAR_SEATING" -> "badge-blue";
            case "BALCONY"         -> "badge-green";
            case "STANDING_ROOM"   -> "badge-gray";
            default                -> "badge-gray";
        };
    }

    private static String statusCss(String s) {
        if (s == null) return "badge-gray";
        return switch (s) {
            case "CONFIRMED"  -> "badge-green";
            case "CANCELLED"  -> "badge-red";
            case "PENDING"    -> "badge-amber";
            default           -> "badge-gray";
        };
    }

    record BookingRow(int id, String username, String email, String eventName,
                      String ticketType, double totalPrice, int quantity, String bookingStatus) {}
}
