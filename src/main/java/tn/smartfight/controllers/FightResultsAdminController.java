package tn.smartfight.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.models.*;
import tn.smartfight.services.*;

import java.util.List;

public class FightResultsAdminController {

    @FXML private TextField                  tfSearch;
    @FXML private TableView<FightResult>     table;
    @FXML private TableColumn<FightResult, String> colId;
    @FXML private TableColumn<FightResult, String> colEvent;
    @FXML private TableColumn<FightResult, String> colRed;
    @FXML private TableColumn<FightResult, String> colBlue;
    @FXML private TableColumn<FightResult, String> colWinner;
    @FXML private TableColumn<FightResult, String> colMethod;
    @FXML private TableColumn<FightResult, String> colRound;
    @FXML private TableColumn<FightResult, String> colDate;
    @FXML private Label lblStatus;

    private final FightResultService service = new FightResultService();
    private final ObservableList<FightResult> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colEvent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventName()));
        colRed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFighterRedName()));
        colBlue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFighterBlueName()));
        colWinner.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getWinnerName() != null ? c.getValue().getWinnerName() : "DRAW"));
        colMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMethod()));
        colRound.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRoundEnded() != null ? "R" + c.getValue().getRoundEnded() : "—"));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFightDate()));

        table.setItems(data);
        tfSearch.textProperty().addListener((obs, o, n) -> filter(n));
        loadData();
    }

    @FXML private void onAddClicked()  { showDialog(null); }

    @FXML
    private void onEditClicked() {
        FightResult sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a result to edit."); return; }
        showDialog(sel);
    }

    @FXML
    private void onDeleteClicked() {
        FightResult sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a result to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete fight result?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            if (service.deleteFightResult(sel.getId())) { loadData(); setStatus("Result deleted."); }
            else setStatus("Delete failed.");
        });
    }

    @FXML private void onRefreshClicked() { loadData(); }

    private void loadData() {
        List<FightResult> results = service.getCompletedFightResults();
        data.setAll(results);
        setStatus(results.size() + " fight results");
    }

    private void filter(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lower = q.toLowerCase();
        ObservableList<FightResult> filtered = FXCollections.observableArrayList();
        for (FightResult fr : data) {
            if ((fr.getEventName()       != null && fr.getEventName().toLowerCase().contains(lower)) ||
                (fr.getFighterRedName()  != null && fr.getFighterRedName().toLowerCase().contains(lower)) ||
                (fr.getFighterBlueName() != null && fr.getFighterBlueName().toLowerCase().contains(lower)) ||
                (fr.getMethod()          != null && fr.getMethod().toLowerCase().contains(lower))) {
                filtered.add(fr);
            }
        }
        table.setItems(filtered);
    }

    private void showDialog(FightResult existing) {
        boolean isEdit = existing != null;
        Dialog<FightResult> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit Fight Result" : "Add Fight Result");

        DialogPane pane = dlg.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color:#16161b;");

        // When adding new, use IDs; when editing, only change winner/method/notes
        TextField tfEventId     = field("Event ID *");
        TextField tfRedId       = field("Red Corner Fighter ID *");
        TextField tfBlueId      = field("Blue Corner Fighter ID *");
        TextField tfWinnerId    = field("Winner Fighter ID (blank = draw)");
        ComboBox<String> cbMethod = new ComboBox<>();
        cbMethod.getItems().addAll("KO","TKO","SUBMISSION","DECISION","DRAW","NO_CONTEST");
        cbMethod.setValue("DECISION");
        cbMethod.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;");
        TextField tfRound       = field("Round ended");
        TextField tfDate        = field("Fight Date (YYYY-MM-DD) *");
        TextArea  tfNotes       = new TextArea();
        tfNotes.setPromptText("Notes (optional)");
        tfNotes.setPrefRowCount(2);
        tfNotes.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;-fx-prompt-text-fill:#888;");

        if (isEdit) {
            tfEventId.setText(String.valueOf(existing.getEventId()));
            tfEventId.setEditable(false);
            tfRedId.setText(String.valueOf(existing.getFighterRedId()));
            tfRedId.setEditable(false);
            tfBlueId.setText(String.valueOf(existing.getFighterBlueId()));
            tfBlueId.setEditable(false);
            if (existing.getWinnerId() > 0) tfWinnerId.setText(String.valueOf(existing.getWinnerId()));
            if (existing.getMethod() != null) cbMethod.setValue(existing.getMethod());
            if (existing.getRoundEnded() != null) tfRound.setText(String.valueOf(existing.getRoundEnded()));
            tfDate.setText(existing.getFightDate() != null ? existing.getFightDate() : "");
            tfNotes.setText(existing.getNotes() != null ? existing.getNotes() : "");
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:#16161b;");
        addRow(grid, 0, "Event ID",     tfEventId);
        addRow(grid, 1, "Red Fighter ID",  tfRedId);
        addRow(grid, 2, "Blue Fighter ID", tfBlueId);
        addRow(grid, 3, "Winner ID",    tfWinnerId);
        addRow(grid, 4, "Method",       cbMethod);
        addRow(grid, 5, "Round",        tfRound);
        addRow(grid, 6, "Date",         tfDate);
        addRow(grid, 7, "Notes",        tfNotes);
        pane.setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            FightResult fr = isEdit ? existing : new FightResult();
            fr.setEventId(parseInt(tfEventId.getText()));
            fr.setFighterRedId(parseInt(tfRedId.getText()));
            fr.setFighterBlueId(parseInt(tfBlueId.getText()));
            String wid = tfWinnerId.getText().trim();
            fr.setWinnerId(wid.isEmpty() ? 0 : parseInt(wid));
            fr.setMethod(cbMethod.getValue());
            String rnd = tfRound.getText().trim();
            fr.setRoundEnded(rnd.isEmpty() ? null : parseInt(rnd));
            fr.setFightDate(tfDate.getText().trim());
            fr.setNotes(tfNotes.getText().trim());
            return fr;
        });

        dlg.showAndWait().ifPresent(fr -> {
            boolean ok = isEdit ? service.updateFightResult(fr) : service.createFightResult(fr);
            if (ok) { loadData(); setStatus(isEdit ? "Result updated." : "Result added."); }
            else      setStatus("Save failed — check fighter/event IDs.");
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
