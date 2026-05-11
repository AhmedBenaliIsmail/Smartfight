package tn.smartfight.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.config.Session;
import tn.smartfight.dao.BookingDao;
import tn.smartfight.model.Booking;
import tn.smartfight.util.QrCodeGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingsController {
    private static final Logger LOG = Logger.getLogger(BookingsController.class.getName());

    @FXML private TableView<Booking>              bookingsTable;
    @FXML private TableColumn<Booking, String>    eventCol;
    @FXML private TableColumn<Booking, String>    dateCol;
    @FXML private TableColumn<Booking, String>    refCol;
    @FXML private TableColumn<Booking, String>    typeCol;
    @FXML private TableColumn<Booking, Integer>   qtyCol;
    @FXML private TableColumn<Booking, String>    priceCol;
    @FXML private TableColumn<Booking, String>    statusCol;
    @FXML private Label statusLabel;

    private final BookingDao dao = new BookingDao();

    @FXML
    public void initialize() {
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateCol.setCellValueFactory(cd -> {
            Booking b = cd.getValue();
            String d = b.getBookingDate() != null ? b.getBookingDate().toString() : "";
            return new SimpleStringProperty(d);
        });
        refCol.setCellValueFactory(new PropertyValueFactory<>("bookingReference"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("ticketType"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("ticketQuantity"));
        priceCol.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getTotalPrice() != null
                                ? cd.getValue().getTotalPrice().toString() : ""));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));

        // Style status cells
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item) {
                    case "CONFIRMED" -> "#16a34a";
                    case "CANCELLED" -> "#dc2626";
                    default          -> "#a1a1aa";
                };
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;");
            }
        });

        refresh();
    }

    @FXML
    private void onCancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a booking to cancel."); return; }
        if ("CANCELLED".equals(selected.getBookingStatus())) {
            statusLabel.setText("Booking is already cancelled.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel booking " + selected.getBookingReference() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                dao.updateStatus(selected.getId(), "CANCELLED");
                refresh();
            }
        });
    }

    @FXML
    private void onViewTicket() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a booking to view the ticket."); return; }

        String qrText = "SMARTFIGHT-BOOKING\n" +
                "Ref: " + selected.getBookingReference() + "\n" +
                "Event: " + selected.getEventName() + "\n" +
                "Type: " + selected.getTicketType() + "\n" +
                "Qty: " + selected.getTicketQuantity() + "\n" +
                "Status: " + selected.getBookingStatus();

        statusLabel.setText("Generating QR…");
        Task<javafx.scene.image.WritableImage> task = new Task<>() {
            @Override protected javafx.scene.image.WritableImage call() throws Exception {
                return QrCodeGenerator.toWritableImage(qrText);
            }
        };
        task.setOnSucceeded(e -> {
            statusLabel.setText("");
            openTicketModal(selected, task.getValue());
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "QR generation failed", task.getException());
            statusLabel.setText("QR generation failed.");
        });
        new Thread(task, "qr-gen").start();
    }

    private void openTicketModal(Booking b, javafx.scene.image.WritableImage qr) {
        Stage stage = new Stage();
        stage.setTitle("Ticket – " + b.getBookingReference());
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView qrView = new ImageView(qr);
        qrView.setFitWidth(240); qrView.setFitHeight(240);
        qrView.setSmooth(true);

        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        infoRow(info, "Reference", b.getBookingReference());
        infoRow(info, "Event",     b.getEventName());
        infoRow(info, "Date",      b.getBookingDate() != null ? b.getBookingDate().toString() : "TBD");
        infoRow(info, "Type",      b.getTicketType());
        infoRow(info, "Quantity",  String.valueOf(b.getTicketQuantity()));
        infoRow(info, "Total",     b.getTotalPrice() != null ? b.getTotalPrice().toString() : "-");
        infoRow(info, "Status",    b.getBookingStatus());

        HBox body = new HBox(24, qrView, info);
        body.setAlignment(Pos.CENTER_LEFT);
        body.setPadding(new Insets(24));

        Label header = new Label("SMARTFIGHT — EVENT TICKET");
        header.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#fafafa;" +
                "-fx-padding:16 24 12 24;-fx-border-color:#27272a;-fx-border-width:0 0 1 0;");
        header.setMaxWidth(Double.MAX_VALUE);

        VBox root = new VBox(header, body);
        root.setStyle("-fx-background-color:#111113;");

        Scene scene = new Scene(root, 560, 360);
        var css = getClass().getResource("/styles/smartfight.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void infoRow(VBox parent, String label, String value) {
        HBox row = new HBox(8);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill:#71717a;-fx-font-size:12px;-fx-min-width:80;");
        Label val = new Label(value != null ? value : "-");
        val.setStyle("-fx-text-fill:#fafafa;-fx-font-size:12px;-fx-font-weight:bold;");
        row.getChildren().addAll(lbl, val);
        parent.getChildren().add(row);
    }

    private void refresh() {
        if (Session.getUser() == null) return;
        Task<java.util.List<Booking>> task = new Task<>() {
            @Override protected java.util.List<Booking> call() {
                return dao.findByUserId(Session.getUser().getUserId());
            }
        };
        task.setOnSucceeded(e -> bookingsTable.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> statusLabel.setText("Load failed."));
        new Thread(task, "bookings-load").start();
    }
}
