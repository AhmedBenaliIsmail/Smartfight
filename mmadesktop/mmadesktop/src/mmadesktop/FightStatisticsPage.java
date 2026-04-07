package mmadesktop;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import service.FightStatisticService;
import service.FightResultservice;
import service.Fighterservice;
import model.FightStatistic;
import model.FightResult;
import model.Fighter;
import java.util.List;
import java.util.stream.Collectors;

public class FightStatisticsPage {

    private final FightStatisticService statService = new FightStatisticService();
    private final FightResultservice fightResultService = new FightResultservice();
    private final Fighterservice fighterService = new Fighterservice();
    private final BorderPane root = new BorderPane();
    private TableView<FightStatistic> table;

    public FightStatisticsPage() {
        root.setStyle("-fx-background-color: #0a0a0b;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color: #0a0a0b;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("FIGHT STATISTICS");
        title.getStyleClass().add("page-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ ADD STATISTICS");
        addBtn.getStyleClass().add("btn-red");
        addBtn.setOnAction(e -> showStatisticDialog(null));

        header.getChildren().addAll(title, sp, addBtn);

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

    public Node getRoot() {
        return root;
    }

    private HBox buildSearchBar() {
        TextField search = new TextField();
        search.setPromptText("🔍  Search by fighter name...");
        search.getStyleClass().add("text-field");
        HBox.setHgrow(search, Priority.ALWAYS);

        search.textProperty().addListener((obs, old, val) -> filterTable(val));

        HBox row = new HBox(10, search);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TableView<FightStatistic> buildTable() {
        TableView<FightStatistic> t = new TableView<>();
        t.getStyleClass().add("table-view");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(480);

        TableColumn<FightStatistic, String> idCol = col("ID", 50,
                d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<FightStatistic, String> fighterCol = col("FIGHTER", 150,
                d -> {
                    try {
                        Fighter f = fighterService.getFighterById(d.getValue().getFighterId());
                        return new SimpleStringProperty(f != null ? f.getFullName() : "?");
                    } catch (Exception e) {
                        return new SimpleStringProperty("?");
                    }
                });

        TableColumn<FightStatistic, String> fightCol = col("FIGHT", 80,
                d -> {
                    try {
                        FightResult fr = fightResultService.getFightResultById(d.getValue().getFightResultId());
                        return new SimpleStringProperty("#" + (fr != null ? fr.getFightNumber() : "?"));
                    } catch (Exception e) {
                        return new SimpleStringProperty("?");
                    }
                });

        // Strikes (landed/thrown) + accuracy
        TableColumn<FightStatistic, String> strikesCol = new TableColumn<>("STRIKES");
        strikesCol.setPrefWidth(120);
        strikesCol.setCellValueFactory(d -> {
            FightStatistic s = d.getValue();
            String text = s.getStrikesLanded() + "/" + s.getStrikesThrown();
            if (s.getStrikesThrown() > 0) {
                int acc = (int)(s.getStrikeAccuracy());
                text += " (" + acc + "%)";
            }
            return new SimpleStringProperty(text);
        });

        // Takedowns (landed/attempts) + accuracy
        TableColumn<FightStatistic, String> takedownsCol = new TableColumn<>("TAKEDOWNS");
        takedownsCol.setPrefWidth(120);
        takedownsCol.setCellValueFactory(d -> {
            FightStatistic s = d.getValue();
            String text = s.getTakedowns() + "/" + s.getTakedownAttempts();
            if (s.getTakedownAttempts() > 0) {
                int acc = (int)(s.getTakedownAccuracy());
                text += " (" + acc + "%)";
            }
            return new SimpleStringProperty(text);
        });

        TableColumn<FightStatistic, String> submissionsCol = col("SUBMISSIONS", 90,
                d -> new SimpleStringProperty(String.valueOf(d.getValue().getSubmissions())));

        TableColumn<FightStatistic, String> knockdownsCol = col("KNOCKDOWNS", 90,
                d -> new SimpleStringProperty(String.valueOf(d.getValue().getKnockdowns())));

        TableColumn<FightStatistic, Void> actCol = new TableColumn<>("ACTIONS");
        actCol.setPrefWidth(120);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button edit = new Button("Edit");
            final Button del = new Button("Delete");
            final HBox box = new HBox(6, edit, del);

            {
                edit.getStyleClass().add("btn-dark");
                del.setStyle("-fx-background-color: rgba(232,0,28,0.15); -fx-text-fill: #e8001c;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10;" +
                        "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
                edit.setOnAction(e -> showStatisticDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteStatistic(getTableView().getItems().get(getIndex())));
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        t.getColumns().addAll(idCol, fighterCol, fightCol, strikesCol, takedownsCol, submissionsCol, knockdownsCol, actCol);
        return t;
    }

    private <T> TableColumn<FightStatistic, T> col(String name, double width,
            javafx.util.Callback<TableColumn.CellDataFeatures<FightStatistic, T>,
            javafx.beans.value.ObservableValue<T>> fn) {
        TableColumn<FightStatistic, T> c = new TableColumn<>(name);
        c.setPrefWidth(width);
        c.setCellValueFactory(fn);
        return c;
    }

    private void loadData() {
        table.getItems().clear();
        try {
            List<FightStatistic> all = statService.getAllStatistics();
            table.getItems().addAll(all);
        } catch (Exception e) {
            showAlert("DB Error", e.getMessage());
        }
    }

    private void filterTable(String query) {
        table.getItems().clear();
        try {
            String q = query.toLowerCase();
            List<FightStatistic> filtered = statService.getAllStatistics().stream()
                    .filter(stat -> {
                        try {
                            Fighter f = fighterService.getFighterById(stat.getFighterId());
                            return f != null && f.getFullName().toLowerCase().contains(q);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            table.getItems().addAll(filtered);
        } catch (Exception e) {
            // ignore
        }
    }

    private void showStatisticDialog(FightStatistic existing) {
        boolean isEdit = existing != null;
        Dialog<FightStatistic> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Statistics" : "Add Statistics");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #16161b;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-red");
        okBtn.setText(isEdit ? "SAVE" : "ADD");

        ComboBox<Fighter> fighterCombo = new ComboBox<>();
        try {
            fighterCombo.getItems().addAll(fighterService.getAllFighters());
        } catch (Exception e) {
            e.printStackTrace();
        }
        fighterCombo.setPromptText("Select Fighter");
        fighterCombo.getStyleClass().add("combo-box");

        ComboBox<FightResult> fightCombo = new ComboBox<>();
        try {
            fightCombo.getItems().addAll(fightResultService.getAllFightResults().stream()
                    .filter(fr -> "COMPLETED".equals(fr.getStatus()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        fightCombo.setPromptText("Select Fight");
        fightCombo.getStyleClass().add("combo-box");
        fightCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(FightResult fr) {
                if (fr == null) return "";
                return "Event #" + fr.getEventId() + " - Fight " + fr.getFightNumber();
            }
            @Override
            public FightResult fromString(String s) {
                return null;
            }
        });

        TextField strikesLanded = styledField("Strikes Landed");
        TextField strikesThrown = styledField("Strikes Thrown");
        TextField takedowns = styledField("Takedowns Landed");
        TextField takedownAttempts = styledField("Takedown Attempts");
        TextField submissions = styledField("Submissions");
        TextField knockdowns = styledField("Knockdowns");

        if (isEdit) {
            try {
                Fighter fighter = fighterService.getFighterById(existing.getFighterId());
                fighterCombo.setValue(fighter);
                FightResult fr = fightResultService.getFightResultById(existing.getFightResultId());
                fightCombo.setValue(fr);
            } catch (Exception e) {}
            strikesLanded.setText(String.valueOf(existing.getStrikesLanded()));
            strikesThrown.setText(String.valueOf(existing.getStrikesThrown()));
            takedowns.setText(String.valueOf(existing.getTakedowns()));
            takedownAttempts.setText(String.valueOf(existing.getTakedownAttempts()));
            submissions.setText(String.valueOf(existing.getSubmissions()));
            knockdowns.setText(String.valueOf(existing.getKnockdowns()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #16161b;");

        addFormRow(grid, 0, "Fighter *", fighterCombo);
        addFormRow(grid, 1, "Fight *", fightCombo);
        addFormRow(grid, 2, "Strikes Landed", strikesLanded);
        addFormRow(grid, 3, "Strikes Thrown", strikesThrown);
        addFormRow(grid, 4, "Takedowns Landed", takedowns);
        addFormRow(grid, 5, "Takedown Attempts", takedownAttempts);
        addFormRow(grid, 6, "Submissions", submissions);
        addFormRow(grid, 7, "Knockdowns", knockdowns);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (fighterCombo.getValue() == null || fightCombo.getValue() == null) {
                    showAlert("Validation Error", "Fighter and Fight are required.");
                    return null;
                }
                FightStatistic stat = isEdit ? existing : new FightStatistic();
                stat.setFighterId(fighterCombo.getValue().getFighterId());
                stat.setFightResultId(fightCombo.getValue().getResultId());
                stat.setStrikesLanded(parseInt(strikesLanded.getText()));
                stat.setStrikesThrown(parseInt(strikesThrown.getText()));
                stat.setTakedowns(parseInt(takedowns.getText()));
                stat.setTakedownAttempts(parseInt(takedownAttempts.getText()));
                stat.setSubmissions(parseInt(submissions.getText()));
                stat.setKnockdowns(parseInt(knockdowns.getText()));
                return stat;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(stat -> {
            boolean ok;
            try {
                if (isEdit) {
                    ok = statService.updateFightStatistic(stat);
                } else {
                    ok = statService.addFightStatistic(stat);
                }
                if (ok) {
                    loadData();
                    // Optional: notify user to recalc rankings
                    showAlert("Success", "Statistics saved. Use 'Recalculate All' in Rankings page to update scores.");
                } else {
                    showAlert("Error", "Could not save statistics.");
                }
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void deleteStatistic(FightStatistic stat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete statistics for fighter? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.getDialogPane().setStyle("-fx-background-color: #16161b;");
        confirm.showAndWait().filter(r -> r == ButtonType.YES)
                .ifPresent(r -> {
                    try {
                        statService.deleteFightStatistic(stat.getId());
                        loadData();
                        showAlert("Deleted", "Statistics removed. Recalculate rankings to update scores.");
                    } catch (Exception e) {
                        showAlert("Error", e.getMessage());
                    }
                });
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        return tf;
    }

    private void addFormRow(GridPane g, int row, String label, Node node) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("muted-label");
        g.add(lbl, 0, row);
        g.add(node, 1, row);
        GridPane.setHgrow(node, Priority.ALWAYS);
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color: #16161b;");
        a.showAndWait();
    }
}