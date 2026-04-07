package mmadesktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import service.Eventservice;
import service.FightResultservice;
import service.Fighterservice;
import model.Event;
import model.FightResult;
import model.Fighter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EVENTS PAGE — Full CRUD for events with fight management.
 * Each event can have up to 3 fights, each fighter can only fight once per event.
 */
public class EventsPage {

    private final Eventservice eventService = new Eventservice();
    private final FightResultservice fightResultService = new FightResultservice();
    private final Fighterservice fighterService = new Fighterservice();
    private final BorderPane root = new BorderPane();
    private TableView<Event> table;

    // Store the CSS path so we can apply it to every dialog
    private String cssPath = null;

    public EventsPage() {
        root.setStyle("-fx-background-color: #0a0a0b;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color: #0a0a0b;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("EVENTS");
        title.getStyleClass().add("page-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button addBtn = new Button("+ ADD EVENT");
        addBtn.getStyleClass().add("btn-red");
        addBtn.setOnAction(e -> showEventDialog(null));
        header.getChildren().addAll(title, sp, addBtn);

        // Table
        table = buildTable();

        VBox tableCard = new VBox(14, table);
        tableCard.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                           "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                           "-fx-padding: 20;");

        content.getChildren().addAll(header, tableCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0a0b; -fx-background-color: #0a0a0b;");
        root.setCenter(scroll);

        // Detect CSS path from scene when attached
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !newScene.getStylesheets().isEmpty()) {
                cssPath = newScene.getStylesheets().get(0);
            }
        });

        loadData();
    }

    public Node getRoot() { return root; }

    /**
     * Applies the app stylesheet to a dialog's scene.
     * Must be called via setOnShown so the scene exists.
     */
    private void applyDialogCss(Dialog<?> dialog) {
        dialog.setOnShown(e -> {
            if (cssPath != null) {
                dialog.getDialogPane().getScene().getStylesheets().add(cssPath);
            }
            // Dark background for the dialog window itself
            dialog.getDialogPane().getScene().getRoot()
                  .setStyle("-fx-background-color: #16161b;");
        });
    }

    private TableView<Event> buildTable() {
        TableView<Event> t = new TableView<>();
        t.getStyleClass().add("table-view");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(520);

        TableColumn<Event, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(60);
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(d.getValue().getEventId())));

        TableColumn<Event, String> nameCol = new TableColumn<>("EVENT NAME");
        nameCol.setPrefWidth(250);
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getEventName()));

        TableColumn<Event, String> dateCol = new TableColumn<>("DATE");
        dateCol.setPrefWidth(140);
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getEventDate().toString()));

        TableColumn<Event, String> locCol = new TableColumn<>("LOCATION");
        locCol.setPrefWidth(220);
        locCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getLocation()));

        TableColumn<Event, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(d -> {
            boolean upcoming = d.getValue().getEventDate().isAfter(LocalDate.now()) ||
                               d.getValue().getEventDate().isEqual(LocalDate.now());
            return new javafx.beans.property.SimpleStringProperty(upcoming ? "UPCOMING" : "PAST");
        });
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(s.equals("UPCOMING") ? "status-upcoming" : "status-past");
                setGraphic(badge); setText(null);
            }
        });

        // Fight count column
        TableColumn<Event, String> fightsCol = new TableColumn<>("FIGHTS");
        fightsCol.setPrefWidth(80);
        fightsCol.setCellValueFactory(d -> {
            int eventId = d.getValue().getEventId();
            int count = fightResultService.getFightResultsByEvent(eventId).size();
            return new javafx.beans.property.SimpleStringProperty(count + "/3");
        });
        fightsCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                int current = Integer.parseInt(s.substring(0, 1));
                if (current == 0)
                    badge.setStyle("-fx-background-color: rgba(100,100,120,0.2); -fx-text-fill: #9898aa;");
                else if (current == 3)
                    badge.setStyle("-fx-background-color: rgba(0,230,118,0.15); -fx-text-fill: #00e676;");
                else
                    badge.setStyle("-fx-background-color: rgba(255,170,0,0.15); -fx-text-fill: #ffaa00;");
                badge.setStyle(badge.getStyle() +
                    "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
                setGraphic(badge); setText(null);
            }
        });

        // Actions column
        TableColumn<Event, Void> actCol = new TableColumn<>("ACTIONS");
        actCol.setPrefWidth(220);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button edit   = new Button("Edit");
            final Button del    = new Button("Delete");
            final Button fights = new Button("Manage Fights");
            final HBox box      = new HBox(6, edit, del, fights);
            {
                edit.getStyleClass().add("btn-dark");
                del.setStyle(
                    "-fx-background-color: rgba(232,0,28,0.15); -fx-text-fill: #e8001c;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10;" +
                    "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                fights.setStyle(
                    "-fx-background-color: #2a2a32; -fx-text-fill: #00cc66;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10;" +
                    "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");

                edit.setOnAction(e   -> showEventDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e    -> deleteEvent(getTableView().getItems().get(getIndex())));
                fights.setOnAction(e -> showFightManagementDialog(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        t.getColumns().addAll(idCol, nameCol, dateCol, locCol, statusCol, fightsCol, actCol);
        return t;
    }

    private void loadData() {
        table.getItems().clear();
        try {
            table.getItems().addAll(eventService.getAllEvents());
        } catch (Exception e) {
            showAlert("DB Error", e.getMessage());
        }
    }

    private void showEventDialog(Event existing) {
        boolean isEdit = existing != null;
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Event" : "Add Event");

        applyDialogCss(dialog); // <-- CSS fix

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #16161b;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-red");
        okBtn.setText(isEdit ? "SAVE" : "ADD");

        TextField nameField = new TextField();
        nameField.setPromptText("Event name");
        nameField.getStyleClass().add("text-field");

        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("date-picker");
        datePicker.setStyle("-fx-background-color: #222228; -fx-text-fill: white;");

        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        locationField.getStyleClass().add("text-field");

        if (isEdit) {
            nameField.setText(existing.getEventName());
            datePicker.setValue(existing.getEventDate());
            locationField.setText(existing.getLocation());
        }

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #16161b;");

        Label l1 = new Label("Event Name *"); l1.getStyleClass().add("muted-label");
        Label l2 = new Label("Date *");       l2.getStyleClass().add("muted-label");
        Label l3 = new Label("Location *");   l3.getStyleClass().add("muted-label");

        grid.add(l1, 0, 0); grid.add(nameField, 1, 0);
        grid.add(l2, 0, 1); grid.add(datePicker, 1, 1);
        grid.add(l3, 0, 2); grid.add(locationField, 1, 2);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(datePicker, Priority.ALWAYS);
        GridPane.setHgrow(locationField, Priority.ALWAYS);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        pane.setContent(grid);
        pane.setPrefWidth(420);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String loc  = locationField.getText().trim();
                LocalDate date = datePicker.getValue();
                if (name.isEmpty() || loc.isEmpty() || date == null) return null;
                Event ev = isEdit ? existing : new Event();
                ev.setEventName(name);
                ev.setEventDate(date);
                ev.setLocation(loc);
                return ev;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(ev -> {
            boolean ok = isEdit ? eventService.updateEvent(ev) : eventService.addEvent(ev);
            if (ok) {
                loadData();
            } else {
                showAlert("Error", "Could not save event. Check all fields are filled.");
            }
        });
    }

    private void deleteEvent(Event ev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete event \"" + ev.getEventName() + "\"?\nThis will also delete all fights associated with this event.",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.getDialogPane().setStyle("-fx-background-color: #16161b;");
        applyDialogCss(confirm);
        confirm.showAndWait().filter(r -> r == ButtonType.YES)
            .ifPresent(r -> { eventService.deleteEvent(ev.getEventId()); loadData(); });
    }

    // -------------------------------------------------------------------------
    // MANAGE FIGHTS DIALOG
    // -------------------------------------------------------------------------
    private void showFightManagementDialog(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Fights - " + event.getEventName());

        applyDialogCss(dialog); // <-- CSS fix

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("dialog-pane");
        pane.setPrefWidth(720);
        pane.setPrefHeight(520);
        pane.setStyle("-fx-background-color: #16161b;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #16161b;");

        // Event info header
        Label eventInfo = new Label(
            "Event: " + event.getEventName() +
            " | Date: " + event.getEventDate() +
            " | Location: " + event.getLocation());
        eventInfo.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Fight table
        TableView<FightResult> fightTable = buildFightTable(event);
        fightTable.setPrefHeight(260);

        // Refresh fights from DB — this is the live observable list
        refreshFightTable(fightTable, event);

        // ADD FIGHT button — disable label updated dynamically
        Button addFightBtn = new Button("+ ADD FIGHT");
        addFightBtn.getStyleClass().add("btn-red");
        addFightBtn.setMaxWidth(Double.MAX_VALUE);
        addFightBtn.setStyle(addFightBtn.getStyle() +
            "-fx-font-size: 13px; -fx-padding: 10 0;");

        // Dynamically enable/disable based on current fight count
        updateAddFightButton(addFightBtn, fightTable);

        addFightBtn.setOnAction(e -> {
            showAddFightDialog(event, fightTable, addFightBtn);
        });

        // Whenever fightTable items change, re-evaluate button state
        fightTable.getItems().addListener(
            (javafx.collections.ListChangeListener<FightResult>) c ->
                updateAddFightButton(addFightBtn, fightTable));

        content.getChildren().addAll(eventInfo, fightTable, addFightBtn);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().add(closeBtn);
        pane.setContent(content);

        // Style the Close button
        dialog.setOnShown(e -> {
            Button close = (Button) pane.lookupButton(closeBtn);
            if (close != null) {
                close.getStyleClass().add("btn-dark");
            }
        });

        dialog.showAndWait();
        loadData(); // Refresh main table fight-count badge after closing
    }

    private TableView<FightResult> buildFightTable(Event event) {
        TableView<FightResult> fightTable = new TableView<>();
        fightTable.getStyleClass().add("table-view");
        fightTable.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;");
        fightTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fightTable.setPlaceholder(new Label("No fights scheduled yet."));

        TableColumn<FightResult, String> numCol = new TableColumn<>("Fight #");
        numCol.setPrefWidth(80);
        numCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(d.getValue().getFightNumber())));

        TableColumn<FightResult, String> f1Col = new TableColumn<>("Fighter 1");
        f1Col.setPrefWidth(190);
        f1Col.setCellValueFactory(d -> {
            Fighter f = fighterService.getFighterById(d.getValue().getFighter1Id());
            return new javafx.beans.property.SimpleStringProperty(f != null ? f.getFullName() : "Unknown");
        });

        TableColumn<FightResult, String> f2Col = new TableColumn<>("Fighter 2");
        f2Col.setPrefWidth(190);
        f2Col.setCellValueFactory(d -> {
            Fighter f = fighterService.getFighterById(d.getValue().getFighter2Id());
            return new javafx.beans.property.SimpleStringProperty(f != null ? f.getFullName() : "Unknown");
        });

        TableColumn<FightResult, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(110);
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatus()));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(
                    s.equals("SCHEDULED") ? "status-upcoming" : "status-past");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<FightResult, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(90);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button delBtn = new Button("Delete");
            {
                delBtn.setStyle(
                    "-fx-background-color: rgba(232,0,28,0.15); -fx-text-fill: #e8001c;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8;" +
                    "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                delBtn.setOnAction(e -> {
                    FightResult fr = getTableView().getItems().get(getIndex());
                    deleteFight(fr, fightTable, event);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : delBtn);
            }
        });

        fightTable.getColumns().addAll(numCol, f1Col, f2Col, statusCol, actCol);
        return fightTable;
    }

    /** Reloads fight data from DB into the given TableView. */
    private void refreshFightTable(TableView<FightResult> fightTable, Event event) {
        List<FightResult> fights = fightResultService.getFightResultsByEvent(event.getEventId());
        fightTable.setItems(FXCollections.observableArrayList(fights));
    }

    private void updateAddFightButton(Button btn, TableView<FightResult> fightTable) {
        int count = fightTable.getItems().size();
        if (count >= 3) {
            btn.setDisable(true);
            btn.setText("MAX FIGHTS REACHED (3/3)");
        } else {
            btn.setDisable(false);
            btn.setText("+ ADD FIGHT  (" + count + "/3)");
        }
    }

    private void deleteFight(FightResult fight, TableView<FightResult> fightTable, Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete fight #" + fight.getFightNumber() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.getDialogPane().setStyle("-fx-background-color: #16161b;");
        applyDialogCss(confirm);
        confirm.showAndWait().filter(r -> r == ButtonType.YES)
            .ifPresent(r -> {
                fightResultService.deleteFightResult(fight.getResultId());
                refreshFightTable(fightTable, event);
            });
    }

    // -------------------------------------------------------------------------
    // ADD FIGHT DIALOG
    // -------------------------------------------------------------------------
    private void showAddFightDialog(Event event, TableView<FightResult> fightTable, Button addFightBtn) {
        Dialog<FightResult> dialog = new Dialog<>();
        dialog.setTitle("Add Fight to " + event.getEventName());

        applyDialogCss(dialog); // <-- CSS fix

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("dialog-pane");
        pane.setStyle("-fx-background-color: #16161b;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-red");
        okBtn.setText("ADD FIGHT");

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #16161b;");

        // Fight Number
        Label numLabel = new Label("Fight Number:");
        numLabel.getStyleClass().add("muted-label");
        ComboBox<Integer> fightNumberBox = new ComboBox<>();
        fightNumberBox.getStyleClass().add("text-field");
        List<Integer> availableNumbers = fightResultService.getAvailableFightNumbers(event.getEventId());
        if (availableNumbers.isEmpty()) {
            showAlert("No available fight numbers", "This event already has 3 fights.");
            return;
        }
        fightNumberBox.setItems(FXCollections.observableArrayList(availableNumbers));
        fightNumberBox.setPromptText("Select fight number");
        fightNumberBox.setMaxWidth(Double.MAX_VALUE);
        fightNumberBox.getSelectionModel().selectFirst(); // auto-select first

        // Fighter 1
        Label f1Label = new Label("Fighter 1:");
        f1Label.getStyleClass().add("muted-label");
        ComboBox<Fighter> fighter1Box = new ComboBox<>();
        fighter1Box.getStyleClass().add("text-field");
        fighter1Box.setConverter(fighterConverter());
        fighter1Box.setMaxWidth(Double.MAX_VALUE);

        // Fighter 2
        Label f2Label = new Label("Fighter 2:");
        f2Label.getStyleClass().add("muted-label");
        ComboBox<Fighter> fighter2Box = new ComboBox<>();
        fighter2Box.getStyleClass().add("text-field");
        fighter2Box.setConverter(fighterConverter());
        fighter2Box.setMaxWidth(Double.MAX_VALUE);

        // Build available fighters (exclude those already in this event)
        List<Fighter> allFighters = fighterService.getAllFighters();
        List<FightResult> existingFights = fightResultService.getFightResultsByEvent(event.getEventId());
        List<Integer> usedIds = existingFights.stream()
            .flatMap(f -> java.util.stream.Stream.of(f.getFighter1Id(), f.getFighter2Id()))
            .collect(Collectors.toList());
        List<Fighter> availableFighters = allFighters.stream()
            .filter(f -> !usedIds.contains(f.getFighterId()))
            .collect(Collectors.toList());

        if (availableFighters.size() < 2) {
            showAlert("Not enough fighters",
                "Need at least 2 available fighters not already scheduled in this event.");
            return;
        }

        fighter1Box.setItems(FXCollections.observableArrayList(availableFighters));
        fighter2Box.setItems(FXCollections.observableArrayList(availableFighters));

        // When fighter1 changes, remove them from fighter2's options
        fighter1Box.valueProperty().addListener((obs, old, selected) -> {
            Fighter prev2 = fighter2Box.getValue();
            if (selected != null) {
                List<Fighter> opts = availableFighters.stream()
                    .filter(f -> f.getFighterId() != selected.getFighterId())
                    .collect(Collectors.toList());
                fighter2Box.setItems(FXCollections.observableArrayList(opts));
                // Re-select previous fighter2 if still valid
                if (prev2 != null && prev2.getFighterId() != selected.getFighterId()) {
                    fighter2Box.setValue(prev2);
                } else {
                    fighter2Box.setValue(null);
                }
            } else {
                fighter2Box.setItems(FXCollections.observableArrayList(availableFighters));
            }
        });

        // Validation: disable OK if any combo is empty
        okBtn.setDisable(true);
        javafx.beans.value.ChangeListener<Object> validator = (obs, o, n) ->
            okBtn.setDisable(
                fightNumberBox.getValue() == null ||
                fighter1Box.getValue() == null ||
                fighter2Box.getValue() == null);
        fightNumberBox.valueProperty().addListener(validator);
        fighter1Box.valueProperty().addListener(validator);
        fighter2Box.valueProperty().addListener(validator);

        content.getChildren().addAll(numLabel, fightNumberBox, f1Label, fighter1Box, f2Label, fighter2Box);
        pane.setContent(content);
        pane.setPrefWidth(460);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (fightNumberBox.getValue() == null ||
                    fighter1Box.getValue() == null ||
                    fighter2Box.getValue() == null) return null;

                FightResult fr = new FightResult();
                fr.setEventId(event.getEventId());
                fr.setFightNumber(fightNumberBox.getValue());
                fr.setFighter1Id(fighter1Box.getValue().getFighterId());
                fr.setFighter2Id(fighter2Box.getValue().getFighterId());
                fr.setStatus("SCHEDULED");
                return fr;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(fr -> {
            try {
                boolean added = fightResultService.addScheduledFight(
                    fr.getEventId(),
                    fr.getFightNumber(),
                    fr.getFighter1Id(),
                    fr.getFighter2Id()
                );
                if (added) {
                    // Refresh fight table in the parent dialog
                    refreshFightTable(fightTable, event);
                    showStyledInfo("Success", "Fight added successfully!");
                } else {
                    showAlert("Error", "Failed to add fight. Please try again.");
                }
            } catch (Exception e) {
                showAlert("Error", "Exception: " + e.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private StringConverter<Fighter> fighterConverter() {
        return new StringConverter<>() {
            @Override public String toString(Fighter f) {
                return f != null ? f.getFullName() + " (" + f.getWeightClass() + ")" : "";
            }
            @Override public Fighter fromString(String s) { return null; }
        };
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color: #16161b;");
        applyDialogCss(a);
        a.showAndWait();
    }

    private void showStyledInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        DialogPane pane = a.getDialogPane();
        pane.setStyle("-fx-background-color: #16161b;");
        pane.getStyleClass().add("dialog-pane");
        applyDialogCss(a);
        a.setOnShown(e -> pane.lookupButton(ButtonType.OK).getStyleClass().add("btn-red"));
        a.showAndWait();
    }
}