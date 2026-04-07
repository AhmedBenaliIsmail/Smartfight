package tn.smartfight.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.models.Fighter;
import tn.smartfight.services.FighterService;

import java.util.List;

public class FightersAdminController {

    @FXML private TextField             tfSearch;
    @FXML private TableView<Fighter>    table;
    @FXML private TableColumn<Fighter, String> colId;
    @FXML private TableColumn<Fighter, String> colName;
    @FXML private TableColumn<Fighter, String> colNickname;
    @FXML private TableColumn<Fighter, String> colWeight;
    @FXML private TableColumn<Fighter, String> colNationality;
    @FXML private TableColumn<Fighter, String> colRecord;
    @FXML private TableColumn<Fighter, String> colStatus;
    @FXML private Label lblStatus;

    private final FighterService service = new FighterService();
    private final ObservableList<Fighter> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colNickname.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNickname() != null ? "\"" + c.getValue().getNickname() + "\"" : "—"));
        colWeight.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getWeightClass() != null ? c.getValue().getWeightClass() : "—"));
        colNationality.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNationality() != null ? c.getValue().getNationality() : "—"));
        colRecord.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecord()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getStatus() != null ? c.getValue().getStatus() : "ACTIVE"));

        table.setItems(data);
        tfSearch.textProperty().addListener((obs, o, n) -> filter(n));
        loadData();
    }

    @FXML
    private void onAddClicked() {
        showDialog(null);
    }

    @FXML
    private void onEditClicked() {
        Fighter sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a fighter to edit."); return; }
        showDialog(sel);
    }

    @FXML
    private void onDeleteClicked() {
        Fighter sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a fighter to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + sel.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            if (service.deleteFighter(sel.getId())) {
                loadData();
                setStatus("Fighter deleted.");
            } else {
                setStatus("Delete failed — fighter may have related records.");
            }
        });
    }

    @FXML private void onRefreshClicked() { loadData(); }

    private void loadData() {
        List<Fighter> fighters = service.getAllFighters();
        data.setAll(fighters);
        setStatus(fighters.size() + " fighters");
    }

    private void filter(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lower = q.toLowerCase();
        ObservableList<Fighter> filtered = FXCollections.observableArrayList();
        for (Fighter f : data) {
            if (f.getFullName().toLowerCase().contains(lower) ||
                (f.getNickname() != null && f.getNickname().toLowerCase().contains(lower)) ||
                (f.getWeightClass() != null && f.getWeightClass().toLowerCase().contains(lower)) ||
                (f.getNationality() != null && f.getNationality().toLowerCase().contains(lower))) {
                filtered.add(f);
            }
        }
        table.setItems(filtered);
    }

    private void showDialog(Fighter existing) {
        boolean isEdit = existing != null;
        Dialog<Fighter> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit Fighter" : "Add Fighter");

        DialogPane pane = dlg.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #16161b;");

        TextField tfNickname    = field("Nickname");
        TextField tfNationality = field("Nationality");
        TextField tfDob         = field("Date of Birth (YYYY-MM-DD)");
        TextField tfWins        = field("Wins");
        TextField tfLosses      = field("Losses");
        TextField tfDraws       = field("Draws");
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("ACTIVE", "INACTIVE", "SUSPENDED");
        cbStatus.setValue("ACTIVE");
        cbStatus.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;");

        if (isEdit) {
            tfNickname.setText(existing.getNickname() != null ? existing.getNickname() : "");
            tfNationality.setText(existing.getNationality() != null ? existing.getNationality() : "");
            tfDob.setText(existing.getDateOfBirth() != null ? existing.getDateOfBirth() : "");
            tfWins.setText(String.valueOf(existing.getWins()));
            tfLosses.setText(String.valueOf(existing.getLosses()));
            tfDraws.setText(String.valueOf(existing.getDraws()));
            if (existing.getStatus() != null) cbStatus.setValue(existing.getStatus());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:#16161b;");
        addRow(grid, 0, "Nickname", tfNickname);
        addRow(grid, 1, "Nationality", tfNationality);
        addRow(grid, 2, "Date of Birth", tfDob);
        addRow(grid, 3, "Wins", tfWins);
        addRow(grid, 4, "Losses", tfLosses);
        addRow(grid, 5, "Draws", tfDraws);
        addRow(grid, 6, "Status", cbStatus);
        pane.setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Fighter f = isEdit ? existing : new Fighter();
            f.setNickname(tfNickname.getText().trim());
            f.setNationality(tfNationality.getText().trim());
            f.setDateOfBirth(tfDob.getText().trim());
            f.setWins(parseInt(tfWins.getText()));
            f.setLosses(parseInt(tfLosses.getText()));
            f.setDraws(parseInt(tfDraws.getText()));
            f.setStatus(cbStatus.getValue());
            return f;
        });

        dlg.showAndWait().ifPresent(f -> {
            boolean ok = isEdit ? service.updateFighter(f) : service.createFighter(f);
            if (ok) { loadData(); setStatus(isEdit ? "Fighter updated." : "Fighter added."); }
            else      setStatus("Save failed.");
        });
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;-fx-prompt-text-fill:#888;-fx-background-radius:4;-fx-padding:6 10;");
        return tf;
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node ctrl) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#aaa;-fx-font-size:11px;");
        g.add(lbl, 0, row);
        g.add(ctrl, 1, row);
        GridPane.setHgrow(ctrl, Priority.ALWAYS);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }
}
