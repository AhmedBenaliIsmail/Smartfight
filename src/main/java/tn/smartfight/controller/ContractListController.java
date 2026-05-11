package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.smartfight.dao.ContractDao;
import tn.smartfight.integration.PdfRenderer;
import tn.smartfight.integration.PebbleRenderer;
import tn.smartfight.model.FighterContract;
import tn.smartfight.service.ContractService;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContractListController {
    private static final Logger LOG = Logger.getLogger(ContractListController.class.getName());

    @FXML private TableView<FighterContract> table;
    @FXML private TableColumn<FighterContract, String>  colFighter;
    @FXML private TableColumn<FighterContract, String>  colEvent;
    @FXML private TableColumn<FighterContract, Number>  colBase;
    @FXML private TableColumn<FighterContract, Number>  colBonus;
    @FXML private TableColumn<FighterContract, Number>  colPayout;
    @FXML private TableColumn<FighterContract, String>  colPaid;
    @FXML private TableColumn<FighterContract, String>  colMissed;
    @FXML private TableColumn<FighterContract, Number>  colFee;
    @FXML private Label statusLabel;

    private final ContractDao contractDao = new ContractDao();
    private final ContractService contractService = new ContractService();

    @FXML
    public void initialize() {
        colFighter.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFighterName()));
        colEvent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEventName()));
        colBase.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getBasePay()));
        colBonus.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getWinBonus()));
        colPayout.setCellValueFactory(c -> {
            BigDecimal p = c.getValue().getCalculatedPayout();
            return new javafx.beans.property.SimpleObjectProperty<>(p);
        });
        colPaid.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isPaid() ? "Yes" : "No"));
        colMissed.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isMissedWeight() ? "Yes" : "No"));
        colFee.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                c.getValue().getManagerFeePercent() * 100));

        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<List<FighterContract>> task = new Task<>() {
            @Override protected List<FighterContract> call() { return contractDao.findAll(); }
        };
        task.setOnSucceeded(e -> {
            List<FighterContract> list = task.getValue();
            table.setItems(FXCollections.observableArrayList(list));
            statusLabel.setText(list.size() + " contracts");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load contracts failed", task.getException());
            statusLabel.setText("Failed to load");
        });
        new Thread(task, "contract-load").start();
    }

    @FXML
    private void onRefresh() { loadData(); }

    @FXML
    private void onExportPdf() {
        FighterContract selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a contract to export."); return; }

        String safeName = selected.getFighterName() != null
                ? selected.getFighterName().replaceAll("\\s+", "_") : "contract";
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Contract PDF");
        fc.setInitialFileName("contract_" + safeName + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;

        statusLabel.setText("Generating PDF...");
        final FighterContract c = selected;
        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() {
                Map<String, Object> ctx = new HashMap<>();
                ctx.put("fighterName", c.getFighterName() != null ? c.getFighterName() : "N/A");
                ctx.put("eventName", c.getEventName() != null ? c.getEventName() : "N/A");
                ctx.put("eventDate", "N/A");
                ctx.put("divisionName", "N/A");
                ctx.put("basePay", fmt(c.getBasePay()));
                ctx.put("winBonus", fmt(c.getWinBonus()));
                boolean hasPayout = c.getCalculatedPayout() != null;
                ctx.put("hasPayout", hasPayout);
                ctx.put("calculatedPayout", hasPayout ? fmt(c.getCalculatedPayout()) : "");
                BigDecimal max = (c.getBasePay() != null ? c.getBasePay() : BigDecimal.ZERO)
                        .add(c.getWinBonus() != null ? c.getWinBonus() : BigDecimal.ZERO);
                ctx.put("maxPurse", fmt(max));
                ctx.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                String html = PebbleRenderer.render("templates/admin/contract/pdf.html.twig", ctx);
                return PdfRenderer.render(html);
            }
        };
        task.setOnSucceeded(e -> {
            try {
                Files.write(file.toPath(), task.getValue());
                statusLabel.setText("PDF saved: " + file.getName());
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Write contract PDF failed", ex);
                statusLabel.setText("Failed to write PDF.");
            }
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Contract PDF export failed", task.getException());
            statusLabel.setText("PDF generation failed.");
        });
        new Thread(task, "contract-pdf").start();
    }

    private String fmt(BigDecimal v) {
        return v == null ? "0.00" : String.format("%,.2f", v);
    }

    @FXML
    private void onNew() {
        // Basic dialog to create a contract
        Dialog<FighterContract> dlg = new Dialog<>();
        dlg.setTitle("New Contract");
        dlg.setHeaderText("Create Fighter Contract");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField tfFighterId = new TextField(); tfFighterId.setPromptText("Fighter ID");
        TextField tfEventId = new TextField(); tfEventId.setPromptText("Event ID");
        TextField tfBase = new TextField(); tfBase.setPromptText("Base Pay");
        TextField tfBonus = new TextField(); tfBonus.setPromptText("Win Bonus");
        TextField tfFee = new TextField("0.1"); tfFee.setPromptText("Manager Fee %");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Fighter ID:"), tfFighterId);
        grid.addRow(1, new Label("Event ID:"), tfEventId);
        grid.addRow(2, new Label("Base Pay ($):"), tfBase);
        grid.addRow(3, new Label("Win Bonus ($):"), tfBonus);
        grid.addRow(4, new Label("Manager Fee (0.1=10%):"), tfFee);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(bt -> {
            if (bt == saveBtn) {
                try {
                    FighterContract fc = new FighterContract();
                    fc.setFighterId(Integer.parseInt(tfFighterId.getText().trim()));
                    fc.setEventId(Integer.parseInt(tfEventId.getText().trim()));
                    fc.setBasePay(new BigDecimal(tfBase.getText().trim()));
                    fc.setWinBonus(new BigDecimal(tfBonus.getText().trim()));
                    fc.setManagerFeePercent(Double.parseDouble(tfFee.getText().trim()));
                    return fc;
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Invalid input: " + ex.getMessage()).showAndWait();
                }
            }
            return null;
        });

        Optional<FighterContract> result = dlg.showAndWait();
        result.ifPresent(fc -> {
            Task<Void> t = new Task<>() {
                @Override protected Void call() { contractDao.create(fc); return null; }
            };
            t.setOnSucceeded(e -> loadData());
            new Thread(t, "contract-create").start();
        });
    }

    @FXML
    private void onPay() {
        FighterContract selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a contract."); return; }
        if (selected.isPaid()) { statusLabel.setText("Already paid."); return; }

        ContractService.PurseBreakdown breakdown = contractService.calculateFinalPurse(selected, false);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Pay " + selected.getFighterName() + "?\n" +
                "Calculated payout: $" + breakdown.calculatedPayout() + "\n" +
                "Net to fighter: $" + breakdown.netToFighter() + "\n" +
                "Manager fee: $" + breakdown.managerFee(),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Payment");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                selected.setCalculatedPayout(breakdown.calculatedPayout());
                selected.setPaid(true);
                Task<Void> t = new Task<>() {
                    @Override protected Void call() { contractDao.update(selected); return null; }
                };
                t.setOnSucceeded(e -> loadData());
                new Thread(t, "contract-pay").start();
            }
        });
    }

    @FXML
    private void onToggleMissed() {
        FighterContract selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a contract."); return; }
        selected.setMissedWeight(!selected.isMissedWeight());
        Task<Void> t = new Task<>() {
            @Override protected Void call() { contractDao.update(selected); return null; }
        };
        t.setOnSucceeded(e -> loadData());
        new Thread(t, "contract-toggle").start();
    }

    @FXML
    private void onDelete() {
        FighterContract selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a contract."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete contract for " + selected.getFighterName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Task<Void> t = new Task<>() {
                    @Override protected Void call() { contractDao.deleteById(selected.getId()); return null; }
                };
                t.setOnSucceeded(e -> loadData());
                new Thread(t, "contract-delete").start();
            }
        });
    }
}
