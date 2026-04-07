package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.geometry.*;
import java.util.*;

public class BookingController {

    @FXML
    private ComboBox<Event> cbEvent;

    @FXML
    private Label lblCapacity;

    @FXML
    private Label lblPrice;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblMessage;

    @FXML
    private ComboBox<String> cbTicketType;

    @FXML
    private Spinner<Integer> spnQuantity;

    @FXML
    private Button btnConfirm;

    @FXML
    private TableView<EventBooking> bookingsTable;

    @FXML
    private TableColumn<EventBooking, String> colEvent;

    @FXML
    private TableColumn<EventBooking, String> colDate;

    @FXML
    private TableColumn<EventBooking, String> colType;

    @FXML
    private TableColumn<EventBooking, String> colStatus;

    @FXML
    private TableColumn<EventBooking, Integer> colQty;

    @FXML
    private TableColumn<EventBooking, Double> colPrice;

    @FXML
    private TableColumn<EventBooking, Void> colActions;

    private final EventService eventService = new EventService();
    private final EventBookingService bookingService = new EventBookingService();
    private final FanNotificationService notifService = new FanNotificationService();

    @FXML
    private void initialize() {
        cbEvent.getItems().addAll(eventService.getScheduledPublicEvents());
        cbTicketType.getItems().addAll("VIP", "REGULAR", "STANDING");
        cbTicketType.setValue("REGULAR");
        spnQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));

        cbEvent.setOnAction(e -> onEventSelected());
        cbTicketType.setOnAction(e -> updateTotal());
        spnQuantity.valueProperty().addListener((obs, oldVal, newVal) -> updateTotal());

        setupTableColumns();
        loadMyBookings();
    }

    private void onEventSelected() {
        Event e = cbEvent.getValue();
        if (e == null) return;
        int sold = bookingService.getTotalConfirmedQty(e.getId());
        lblCapacity.setText("Available: " + (e.getCapacity() - sold) + " seats");
        updateTotal();
    }

    private void updateTotal() {
        if (cbTicketType.getValue() == null) return;
        double price = EventBookingService.getPricePerTicket(cbTicketType.getValue());
        int qty = spnQuantity.getValue();
        lblPrice.setText(price + " TND/ticket");
        lblTotal.setText("Total: " + String.format("%.2f", price * qty) + " TND");
    }

    @FXML
    private void onConfirmBooking() {
        if (cbEvent.getValue() == null) {
            lblMessage.setText("Please select an event");
            return;
        }
        int userId = SessionManager.getCurrentUserId();
        int eventId = cbEvent.getValue().getId();

        if (bookingService.isAlreadyBooked(eventId, userId)) {
            lblMessage.setText("You already have an active booking for this event");
            return;
        }

        EventBooking b = new EventBooking();
        b.setEventId(eventId);
        b.setUserId(userId);
        b.setTicketType(cbTicketType.getValue());
        b.setTicketQuantity(spnQuantity.getValue());
        b.setTotalPrice(EventBookingService.getPricePerTicket(cbTicketType.getValue()) * spnQuantity.getValue());

        EventBooking saved = bookingService.insertBooking(b);

        // QR Code dialog
        String qrData = QRCodeUtil.buildQRData(saved);
        Image qrImg = QRCodeUtil.generateQRImage(qrData, 280);
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Booking Confirmed!");
        if (qrImg != null) {
            ImageView iv = new ImageView(qrImg);
            iv.setFitWidth(280);
            iv.setFitHeight(280);
            VBox content = new VBox(10,
                    new Label("Ref: " + saved.getBookingReference()),
                    new Label("Type: " + saved.getTicketType()),
                    new Label("Qty: " + saved.getTicketQuantity()),
                    new Label("Total: " + String.format("%.2f", saved.getTotalPrice()) + " TND"),
                    iv);
            dialog.getDialogPane().setContent(content);
        } else {
            VBox content = new VBox(10,
                    new Label("Ref: " + saved.getBookingReference()),
                    new Label("Type: " + saved.getTicketType()),
                    new Label("Qty: " + saved.getTicketQuantity()),
                    new Label("Total: " + String.format("%.2f", saved.getTotalPrice()) + " TND"));
            dialog.getDialogPane().setContent(content);
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();

        // Notification
        notifService.createNotification(
                userId,
                "ADMIN_BROADCAST",
                "Booking Confirmed",
                "Your booking for " + cbEvent.getValue().getName()
                        + " (Ref: " + saved.getBookingReference() + ") is confirmed!",
                eventId,
                null
        );

        lblMessage.setText("Booking confirmed! Ref: " + saved.getBookingReference());
        loadMyBookings();
    }

    private void setupTableColumns() {
        colEvent.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEventName()));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBookingDate()));
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTicketType()));
        colQty.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTicketQuantity()));
        colPrice.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTotalPrice()));
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBookingStatus()));
        addCancelButtonColumn();
    }

    private void addCancelButtonColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private EventBooking currentBooking;
            private final Button btnCancel = new Button("Cancel");
            private final Button btnQR = new Button("QR");
            private final HBox box = new HBox(6, btnQR, btnCancel);

            {
                box.setAlignment(Pos.CENTER);
                btnCancel.getStyleClass().add("btn-danger");
                btnCancel.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                btnQR.setStyle("-fx-background-color: #18181b; -fx-text-fill: #fafafa; " +
                        "-fx-border-color: #dc2626; -fx-border-radius: 4; -fx-background-radius: 4; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-cursor: hand;");
                btnCancel.setOnAction(e -> {
                    if (currentBooking == null || !currentBooking.isConfirmed()) return;
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Cancel booking " + currentBooking.getBookingReference() + "?",
                            ButtonType.YES, ButtonType.NO);
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            bookingService.cancelBooking(currentBooking.getId());
                            loadMyBookings();
                        }
                    });
                });
                btnQR.setOnAction(e -> {
                    if (currentBooking != null) showQRPopup(currentBooking);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    currentBooking = null;
                } else {
                    currentBooking = getTableView().getItems().get(getIndex());
                    setGraphic(currentBooking.isConfirmed() ? box : null);
                }
            }
        });
    }

    private void showQRPopup(EventBooking b) {
        String qrData = QRCodeUtil.buildQRData(b);
        Image qrImg = QRCodeUtil.generateQRImage(qrData, 250);
        if (qrImg == null) return;

        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initOwner(bookingsTable.getScene().getWindow());

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #18181b; -fx-background-radius: 12; " +
                "-fx-border-color: #dc2626; -fx-border-width: 2; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.4), 20, 0, 0, 0);");
        root.setPrefWidth(360);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 10 10 0 0; -fx-padding: 12 16;");
        Label titleLabel = new Label("YOUR TICKET");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-font-style: italic;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnClose = new Button("\u2715");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());
        titleBar.getChildren().addAll(titleLabel, spacer, btnClose);

        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> { dragOffset[0] = e.getScreenX() - popup.getX(); dragOffset[1] = e.getScreenY() - popup.getY(); });
        titleBar.setOnMouseDragged(e -> { popup.setX(e.getScreenX() - dragOffset[0]); popup.setY(e.getScreenY() - dragOffset[1]); });

        // QR image
        ImageView qrView = new ImageView(qrImg);
        qrView.setFitWidth(220);
        qrView.setFitHeight(220);
        qrView.setPreserveRatio(true);
        StackPane qrPane = new StackPane(qrView);
        qrPane.setStyle("-fx-padding: 24 0 16 0;");

        // Ticket details
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 0 28 20 28;");
        details.getChildren().addAll(
            ticketRow("REFERENCE", b.getBookingReference()),
            ticketRow("EVENT", b.getEventName()),
            ticketRow("DATE", b.getEventStartDate() != null ? b.getEventStartDate() : "TBD"),
            ticketRow("TYPE", b.getTicketType()),
            ticketRow("QTY", String.valueOf(b.getTicketQuantity())),
            ticketRow("TOTAL", String.format("%.2f TND", b.getTotalPrice()))
        );

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #27272a; -fx-pref-height: 1; -fx-max-height: 1;");
        VBox.setMargin(sep, new Insets(0, 28, 0, 28));

        Button btnDone = new Button("CLOSE");
        btnDone.setStyle("-fx-background-color: #27272a; -fx-text-fill: #fafafa; -fx-font-weight: bold; " +
                "-fx-padding: 10 40; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
        btnDone.setOnAction(e -> popup.close());
        HBox bottomBar = new HBox(btnDone);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-padding: 12 0 20 0;");

        root.getChildren().addAll(titleBar, qrPane, sep, details, bottomBar);

        Scene scene = new Scene(root, Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

    private HBox ticketRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        lbl.setMinWidth(80);
        Label val = new Label(value != null ? value : "-");
        val.setStyle("-fx-text-fill: #fafafa; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox row = new HBox(12, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void loadMyBookings() {
        bookingsTable.setItems(FXCollections.observableArrayList(
                bookingService.getBookingsByUser(SessionManager.getCurrentUserId())
        ));
    }
}
