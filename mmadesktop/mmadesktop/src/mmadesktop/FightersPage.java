package mmadesktop;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;
import service.Fighterservice;
import service.RankingService;
import model.Fighter;
import javafx.util.Callback;

/**
 * FIGHTERS PAGE — Full CRUD for fighters (Admin) / View only (User).
 * Now includes advanced ranking columns (ELO, Performance Score, Win Streak, SOS)
 * and a "Recalc Rankings" button for admin.
 */
public class FightersPage {

    private final Fighterservice fighterService = new Fighterservice();
    private final RankingService rankingService = new RankingService();
    private final BorderPane root = new BorderPane();
    private TableView<Fighter> table;
    private final boolean isAdmin;

    public FightersPage(boolean isAdmin) {
        this.isAdmin = isAdmin;
        root.setStyle("-fx-background-color: #0a0a0b;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color: #0a0a0b;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("FIGHTERS");
        title.getStyleClass().add("page-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(title, sp);

        // Admin buttons
        if (isAdmin) {
            Button addBtn = new Button("+ ADD FIGHTER");
            addBtn.getStyleClass().add("btn-red");
            addBtn.setOnAction(e -> showFighterDialog(null));
            header.getChildren().add(addBtn);

            Button recalcBtn = new Button("⟳ RECALC RANKINGS");
            recalcBtn.getStyleClass().add("btn-dark");
            recalcBtn.setOnAction(e -> {
                new Thread(() -> {
                    try {
                        rankingService.recomputeAllRankings();
                        Platform.runLater(() -> {
                            loadData();
                            showAlert("Success", "Rankings recalculated from all fight data.");
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Platform.runLater(() -> showAlert("Error", "Recalculation failed: " + ex.getMessage()));
                    }
                }).start();
            });
            header.getChildren().add(recalcBtn);
        }

        // Table
        table = buildTable();

        VBox tableCard = new VBox(14, buildSearchBar(), table);
        tableCard.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                           "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                           "-fx-padding: 20;");

        content.getChildren().addAll(header, tableCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0a0a0b; -fx-background-color: #0a0a0b;");
        root.setCenter(scroll);

        loadData();
    }

    public Node getRoot() { return root; }

    private HBox buildSearchBar() {
        TextField search = new TextField();
        search.setPromptText("🔍  Search fighters...");
        search.getStyleClass().add("text-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        search.textProperty().addListener((obs, old, val) -> filterTable(val));

        HBox row = new HBox(10, search);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TableView<Fighter> buildTable() {
        TableView<Fighter> t = new TableView<>();
        t.getStyleClass().add("table-view");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(480);

        // Basic columns
        TableColumn<Fighter, String> idCol = col("ID", 50,
            d -> new SimpleStringProperty(String.valueOf(d.getValue().getFighterId())));
        TableColumn<Fighter, String> nameCol = col("FIGHTER NAME", 170,
            d -> new SimpleStringProperty(d.getValue().getFullName()));
        TableColumn<Fighter, String> nickCol = col("NICKNAME", 120,
            d -> new SimpleStringProperty(d.getValue().getNickname() != null ? "\"" + d.getValue().getNickname() + "\"" : "—"));
        TableColumn<Fighter, String> weightCol = col("WEIGHT CLASS", 120,
            d -> new SimpleStringProperty(d.getValue().getWeightClass()));
        TableColumn<Fighter, String> countryCol = col("COUNTRY", 100,
            d -> new SimpleStringProperty(d.getValue().getCountry() != null ? d.getValue().getCountry() : "—"));

        // Advanced ranking columns (new)
        TableColumn<Fighter, String> eloCol = col("ELO", 70,
            d -> new SimpleStringProperty(String.valueOf((int) d.getValue().getEloRating())));
        TableColumn<Fighter, String> perfCol = col("PERF.", 70,
            d -> new SimpleStringProperty(String.valueOf((int) d.getValue().getPerformanceScore())));
        TableColumn<Fighter, String> streakCol = col("STREAK", 70,
            d -> new SimpleStringProperty(String.valueOf(d.getValue().getWinStreak())));
        TableColumn<Fighter, String> sosCol = col("SOS", 70,
            d -> new SimpleStringProperty(String.valueOf((int) d.getValue().getStrengthOfSchedule())));

        // Traditional record columns
        TableColumn<Fighter, String> recordCol = col("W - L - D", 90,
            d -> { Fighter f = d.getValue();
                   return new SimpleStringProperty(f.getWins() + " - " + f.getLosses() + " - " + f.getDraws()); });
        TableColumn<Fighter, String> koCol = col("KOs", 60,
            d -> new SimpleStringProperty(String.valueOf(d.getValue().getKoWins())));
        TableColumn<Fighter, String> subCol = col("SUBS", 60,
            d -> new SimpleStringProperty(String.valueOf(d.getValue().getSubmissionWins())));

        t.getColumns().addAll(idCol, nameCol, nickCol, weightCol, countryCol,
                              eloCol, perfCol, streakCol, sosCol, recordCol, koCol, subCol);

        // Actions column (only for admin)
        if (isAdmin) {
            TableColumn<Fighter, Void> actCol = new TableColumn<>("ACTIONS");
            actCol.setPrefWidth(120);
            actCol.setCellFactory(c -> new TableCell<>() {
                final Button edit = new Button("Edit");
                final Button del  = new Button("Delete");
                final HBox box    = new HBox(6, edit, del);
                {
                    edit.getStyleClass().add("btn-dark");
                    del.setStyle("-fx-background-color: rgba(232,0,28,0.15); -fx-text-fill: #e8001c;" +
                                 "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10;" +
                                 "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                    edit.setOnAction(e -> showFighterDialog(getTableView().getItems().get(getIndex())));
                    del.setOnAction(e  -> deleteFighter(getTableView().getItems().get(getIndex())));
                    box.setAlignment(Pos.CENTER_LEFT);
                }
                @Override protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setGraphic(empty ? null : box);
                }
            });
            t.getColumns().add(actCol);
        }

        return t;
    }

    private TableColumn<Fighter, String> col(String name, double width,
            Callback<TableColumn.CellDataFeatures<Fighter, String>,
            javafx.beans.value.ObservableValue<String>> fn) {
        TableColumn<Fighter, String> c = new TableColumn<>(name);
        c.setPrefWidth(width);
        c.setCellValueFactory(fn);
        return c;
    }

    private void loadData() {
        table.getItems().clear();
        try {
            table.getItems().addAll(fighterService.getAllFighters());
        } catch (Exception e) {
            showAlert("DB Error", e.getMessage());
        }
    }

    private void filterTable(String query) {
        table.getItems().clear();
        try {
            String q = query.toLowerCase();
            fighterService.getAllFighters().stream()
                .filter(f -> f.getFullName().toLowerCase().contains(q) ||
                             (f.getNickname() != null && f.getNickname().toLowerCase().contains(q)) ||
                             (f.getWeightClass() != null && f.getWeightClass().toLowerCase().contains(q)))
                .forEach(table.getItems()::add);
        } catch (Exception e) { /* ignore */ }
    }

    private void showFighterDialog(Fighter existing) {
        boolean isEdit = existing != null;
        Dialog<Fighter> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Fighter" : "Add Fighter");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #16161b;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-red");
        okBtn.setText(isEdit ? "SAVE" : "ADD");

        TextField firstName  = styledField("First Name");
        TextField lastName   = styledField("Last Name");
        TextField nickname   = styledField("Nickname");
        TextField weightClass= styledField("Weight Class");
        TextField country    = styledField("Country");
        TextField wins       = styledField("Wins");
        TextField losses     = styledField("Losses");
        TextField draws      = styledField("Draws");
        TextField koWins     = styledField("KO Wins");
        TextField subWins    = styledField("Submission Wins");
        TextField decWins    = styledField("Decision Wins");

        if (isEdit) {
            firstName.setText(existing.getFirstName());
            lastName.setText(existing.getLastName());
            nickname.setText(existing.getNickname());
            weightClass.setText(existing.getWeightClass());
            country.setText(existing.getCountry());
            wins.setText(String.valueOf(existing.getWins()));
            losses.setText(String.valueOf(existing.getLosses()));
            draws.setText(String.valueOf(existing.getDraws()));
            koWins.setText(String.valueOf(existing.getKoWins()));
            subWins.setText(String.valueOf(existing.getSubmissionWins()));
            decWins.setText(String.valueOf(existing.getDecisionWins()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #16161b;");

        addFormRow(grid, 0, "First Name *", firstName);
        addFormRow(grid, 1, "Last Name *", lastName);
        addFormRow(grid, 2, "Nickname", nickname);
        addFormRow(grid, 3, "Weight Class *", weightClass);
        addFormRow(grid, 4, "Country", country);
        addFormRow(grid, 5, "Wins", wins);
        addFormRow(grid, 6, "Losses", losses);
        addFormRow(grid, 7, "Draws", draws);
        addFormRow(grid, 8, "KO Wins", koWins);
        addFormRow(grid, 9, "Sub Wins", subWins);
        addFormRow(grid, 10, "Decision Wins", decWins);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Fighter f = isEdit ? existing : new Fighter();
                f.setFirstName(firstName.getText().trim());
                f.setLastName(lastName.getText().trim());
                f.setNickname(nickname.getText().trim());
                f.setWeightClass(weightClass.getText().trim());
                f.setCountry(country.getText().trim());
                f.setWins(parseInt(wins.getText()));
                f.setLosses(parseInt(losses.getText()));
                f.setDraws(parseInt(draws.getText()));
                f.setKoWins(parseInt(koWins.getText()));
                f.setSubmissionWins(parseInt(subWins.getText()));
                f.setDecisionWins(parseInt(decWins.getText()));
                return f;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            boolean ok = isEdit ? fighterService.updateFighter(f) : fighterService.addFighter(f);
            if (ok) {
                loadData();
                // Optionally, after editing a fighter's record, rankings might need refresh
                if (!isEdit) {
                    // new fighter – ask to recalc rankings? Not necessary immediately.
                }
            } else {
                showAlert("Error", "Could not save fighter. Check required fields.");
            }
        });
    }

    private void deleteFighter(Fighter f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + f.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.getDialogPane().setStyle("-fx-background-color: #16161b;");
        confirm.showAndWait().filter(r -> r == ButtonType.YES)
            .ifPresent(r -> { fighterService.deleteFighter(f.getFighterId()); loadData(); });
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        return tf;
    }

    private void addFormRow(GridPane g, int row, String label, TextField field) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("muted-label");
        g.add(lbl, 0, row);
        g.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color: #16161b;");
        a.showAndWait();
    }
}