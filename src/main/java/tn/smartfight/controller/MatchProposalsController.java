package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.smartfight.dao.MatchProposalDao;
import tn.smartfight.model.MatchProposal;
import tn.smartfight.service.MatchmakingService;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchProposalsController {
    private static final Logger LOG = Logger.getLogger(MatchProposalsController.class.getName());

    @FXML private TableView<MatchProposal> table;
    @FXML private TableColumn<MatchProposal, String>  colMatchup;
    @FXML private TableColumn<MatchProposal, Number>  colScore;
    @FXML private TableColumn<MatchProposal, String>  colStatus;
    @FXML private TableColumn<MatchProposal, Number>  colVotes;
    @FXML private TableColumn<MatchProposal, String>  colDivision;
    @FXML private TableColumn<MatchProposal, String>  colNotes;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label statusLabel;

    private final MatchProposalDao dao = new MatchProposalDao();
    private final MatchmakingService matchmaker = new MatchmakingService();
    private List<MatchProposal> allProposals;

    @FXML
    public void initialize() {
        colMatchup.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMatchup()));
        colScore.setCellValueFactory(c -> {
            BigDecimal s = c.getValue().getCompatibility();
            return new javafx.beans.property.SimpleDoubleProperty(s != null ? s.doubleValue() : 0);
        });
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colVotes.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVoteCount()));
        colDivision.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDivisionName() != null ? c.getValue().getDivisionName() : "—"));
        colNotes.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNotes()));

        statusFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "APPROVED", "REJECTED"));
        statusFilter.setValue("ALL");
        statusFilter.setOnAction(e -> applyFilter());

        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<List<MatchProposal>> task = new Task<>() {
            @Override protected List<MatchProposal> call() { return dao.findAll(); }
        };
        task.setOnSucceeded(e -> {
            allProposals = task.getValue();
            applyFilter();
            statusLabel.setText(allProposals.size() + " proposals");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load proposals failed", task.getException());
            statusLabel.setText("Failed to load");
        });
        new Thread(task, "proposals-load").start();
    }

    private void applyFilter() {
        if (allProposals == null) return;
        String f = statusFilter.getValue();
        List<MatchProposal> filtered = "ALL".equals(f) ? allProposals
                : allProposals.stream().filter(p -> f.equals(p.getStatus())).toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void onRefresh() { loadData(); }

    @FXML
    private void onGenerate() {
        TextInputDialog dlg = new TextInputDialog("3");
        dlg.setTitle("Generate AI Proposals");
        dlg.setHeaderText("How many proposals to generate?");
        dlg.setContentText("Count (1-10):");
        dlg.showAndWait().ifPresent(input -> {
            int count;
            try { count = Math.min(10, Math.max(1, Integer.parseInt(input.trim()))); }
            catch (NumberFormatException ex) { count = 3; }
            final int n = count;
            statusLabel.setText("Generating " + n + " proposals...");
            Task<List<MatchProposal>> task = new Task<>() {
                @Override protected List<MatchProposal> call() { return matchmaker.generateProposalsLocal(n); }
            };
            task.setOnSucceeded(e -> {
                List<MatchProposal> generated = task.getValue();
                statusLabel.setText("Generated " + generated.size() + " proposals.");
                loadData();
            });
            task.setOnFailed(e -> {
                LOG.log(Level.SEVERE, "Generate failed", task.getException());
                statusLabel.setText("Generation failed.");
            });
            new Thread(task, "proposals-generate").start();
        });
    }

    @FXML
    private void onApprove() { changeStatus("APPROVED"); }

    @FXML
    private void onReject() { changeStatus("REJECTED"); }

    private void changeStatus(String newStatus) {
        MatchProposal selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a proposal."); return; }
        Task<Void> task = new Task<>() {
            @Override protected Void call() { dao.updateStatus(selected.getId(), newStatus); return null; }
        };
        task.setOnSucceeded(e -> loadData());
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "Status update failed", task.getException()));
        new Thread(task, "proposals-status").start();
    }

    @FXML
    private void onDelete() {
        MatchProposal selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a proposal."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete proposal: " + selected.getMatchup() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() { dao.deleteById(selected.getId()); return null; }
                };
                task.setOnSucceeded(e -> loadData());
                new Thread(task, "proposals-delete").start();
            }
        });
    }
}
