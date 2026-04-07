package mmadesktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import service.Fighterservice;
import service.Eventservice;
import service.FightResultservice;
import model.Fighter;
import model.Event;
import model.FightResult;
import java.util.List;

/**
 * HOME PAGE — Dashboard overview matching the mockup highlights page.
 * Shows stats, fighter list, upcoming events, recent results.
 */
public class HomePage {

    private final Fighterservice fighterService = new Fighterservice();
    private final Eventservice eventService     = new Eventservice();
    private final FightResultservice resultService = new FightResultservice();

    private final BorderPane root = new BorderPane();

    public HomePage() {
        root.setStyle("-fx-background-color: #0a0a0b;");

        ScrollPane scroll = new ScrollPane(buildContent());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0a0b; -fx-background-color: #0a0a0b;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroll);
    }

    public Node getRoot() { return root; }

    private VBox buildContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color: #0a0a0b;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("DASHBOARD");
        title.getStyleClass().add("page-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label date = new Label("MMA Management System");
        date.getStyleClass().add("muted-label");
        header.getChildren().addAll(title, sp, date);

        // Stats row
        HBox statsRow = buildStatsRow();

        // Two column layout: fighters table + events
        HBox midRow = new HBox(20);
        midRow.getChildren().addAll(buildRecentFighters(), buildUpcomingEvents());
        HBox.setHgrow(midRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(midRow.getChildren().get(1), Priority.ALWAYS);

        // Recent results
        VBox results = buildRecentResults();

        content.getChildren().addAll(header, statsRow, midRow, results);
        return content;
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(16);

        List<Fighter> fighters = loadSafe(() -> fighterService.getAllFighters());
        List<Event> events     = loadSafe(() -> eventService.getAllEvents());
        List<FightResult> res  = loadSafe(() -> resultService.getAllFightResults());

        int totalFighters = fighters.size();
        int totalEvents   = events.size();
        int totalFights   = res.size();
        long koCount = res.stream()
            .filter(r -> r.getMethodOfVictory() != null &&
                    (r.getMethodOfVictory().equalsIgnoreCase("KO") ||
                     r.getMethodOfVictory().equalsIgnoreCase("TKO")))
            .count();

        row.getChildren().addAll(
            statCard("Total Fighters", String.valueOf(totalFighters), "Registered fighters", false),
            statCard("Total Events",   String.valueOf(totalEvents),   "All events", false),
            statCard("Total Fights",   String.valueOf(totalFights),   "Fight results logged", false),
            statCard("KO / TKO",       String.valueOf(koCount),       "Knockout finishes", true)
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox statCard(String label, String value, String sub, boolean green) {
        Label lbl = new Label(label.toUpperCase());
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add(green ? "stat-value-green" : "stat-value");
        Label subL = new Label(sub);
        subL.getStyleClass().add("muted-label");

        VBox box = new VBox(4, lbl, val, subL);
        box.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                     "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                     "-fx-padding: 16 20 16 20;");
        return box;
    }

    private VBox buildRecentFighters() {
        Label title = new Label("RECENT FIGHTERS");
        title.getStyleClass().add("section-title");

        TableView<Fighter> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(260);

        TableColumn<Fighter, String> nameCol = new TableColumn<>("FIGHTER");
        nameCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getFullName()));

        TableColumn<Fighter, String> weightCol = new TableColumn<>("WEIGHT CLASS");
        weightCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getWeightClass()));

        TableColumn<Fighter, String> recordCol = new TableColumn<>("RECORD (W-L-D)");
        recordCol.setCellValueFactory(d -> {
            Fighter f = d.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                f.getWins() + " - " + f.getLosses() + " - " + f.getDraws());
        });

        TableColumn<Fighter, String> countryCol = new TableColumn<>("COUNTRY");
        countryCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getCountry()));

        table.getColumns().addAll(nameCol, weightCol, recordCol, countryCol);

        List<Fighter> fighters = loadSafe(() -> fighterService.getAllFighters());
        table.getItems().addAll(fighters);

        VBox box = new VBox(14, title, table);
        box.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                     "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                     "-fx-padding: 20;");
        return box;
    }

    private VBox buildUpcomingEvents() {
        Label title = new Label("UPCOMING EVENTS");
        title.getStyleClass().add("section-title");

        VBox list = new VBox(0);

        List<Event> events = loadSafe(() -> eventService.getUpcomingEvents());

        if (events.isEmpty()) {
            Label empty = new Label("No upcoming events");
            empty.getStyleClass().add("muted-label");
            empty.setPadding(new Insets(16));
            list.getChildren().add(empty);
        } else {
            for (Event e : events) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 0, 12, 0));
                row.setStyle("-fx-border-color: transparent transparent #222228 transparent;" +
                             "-fx-border-width: 0 0 1 0;");

                Label icon = new Label("📅");
                icon.setStyle("-fx-font-size: 18px;");

                VBox info = new VBox(2);
                Label name = new Label(e.getEventName());
                name.getStyleClass().add("white-label");
                name.setStyle("-fx-font-weight: bold;");
                Label loc = new Label(e.getEventDate() + "  ·  " + e.getLocation());
                loc.getStyleClass().add("muted-label");
                info.getChildren().addAll(name, loc);

                row.getChildren().addAll(icon, info);
                list.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(260);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox box = new VBox(14, title, scroll);
        box.setMinWidth(280);
        box.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                     "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                     "-fx-padding: 20;");
        return box;
    }

    private VBox buildRecentResults() {
        Label title = new Label("RECENT FIGHT RESULTS");
        title.getStyleClass().add("section-title");

        TableView<FightResult> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);

        TableColumn<FightResult, String> evCol = new TableColumn<>("EVENT ID");
        evCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getEventId())));

        TableColumn<FightResult, String> f1Col = new TableColumn<>("FIGHTER 1");
        f1Col.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty("Fighter #" + d.getValue().getFighter1Id()));

        TableColumn<FightResult, String> f2Col = new TableColumn<>("FIGHTER 2");
        f2Col.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty("Fighter #" + d.getValue().getFighter2Id()));

        TableColumn<FightResult, String> methodCol = new TableColumn<>("METHOD");
        methodCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getMethodOfVictory()));

        TableColumn<FightResult, String> roundCol = new TableColumn<>("ROUND");
        roundCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getRoundNumber())));

        TableColumn<FightResult, String> dateCol = new TableColumn<>("DATE");
        dateCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                d.getValue().getFightDate() != null ? d.getValue().getFightDate().toLocalDate().toString() : "—"));

        table.getColumns().addAll(evCol, f1Col, f2Col, methodCol, roundCol, dateCol);

        List<FightResult> results = loadSafe(() -> resultService.getAllFightResults());
        table.getItems().addAll(results);

        VBox box = new VBox(14, title, table);
        box.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                     "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                     "-fx-padding: 20;");
        return box;
    }

    // Safe loader — returns empty list if DB not connected
    private <T> List<T> loadSafe(java.util.concurrent.Callable<List<T>> fn) {
        try { return fn.call(); }
        catch (Exception e) { return java.util.Collections.emptyList(); }
    }
}
