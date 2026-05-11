package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.model.Fighter;
import tn.smartfight.service.AIService;
import tn.smartfight.service.AnalyticsEngine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerformanceListController {
    private static final Logger LOG = Logger.getLogger(PerformanceListController.class.getName());

    @FXML private TableView<Fighter> table;
    @FXML private TableColumn<Fighter, String>  colName;
    @FXML private TableColumn<Fighter, Number>  colElo;
    @FXML private TableColumn<Fighter, Number>  colPerf;
    @FXML private TableColumn<Fighter, Number>  colEff;
    @FXML private TableColumn<Fighter, String>  colMomentum;
    @FXML private TableColumn<Fighter, Number>  colWinStreak;
    @FXML private TableColumn<Fighter, String>  colRecord;
    @FXML private TableColumn<Fighter, Number>  colTitleDef;
    @FXML private Label statusLabel;

    private final FighterDao fighterDao = new FighterDao();
    private final AnalyticsEngine analytics = new AnalyticsEngine();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFirstName() + " " + c.getValue().getLastName()));
        colElo.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getEloRating()));
        colPerf.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPerformanceScore()));
        colEff.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(
                AIService.calculateEfficiencyScore(c.getValue())));
        colMomentum.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                AIService.getMomentum(c.getValue())));
        colWinStreak.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getWinStreak()));
        colRecord.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRecord()));
        colTitleDef.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTitleDefenses()));

        table.getSortOrder().add(colPerf);
        colPerf.setSortType(TableColumn.SortType.DESCENDING);

        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<List<Fighter>> task = new Task<>() {
            @Override protected List<Fighter> call() { return fighterDao.findAll(); }
        };
        task.setOnSucceeded(e -> {
            List<Fighter> fighters = task.getValue();
            table.setItems(FXCollections.observableArrayList(fighters));
            table.sort();
            statusLabel.setText(fighters.size() + " fighters");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load fighters failed", task.getException());
            statusLabel.setText("Failed to load");
        });
        new Thread(task, "perf-load").start();
    }

    @FXML
    private void onRefresh() { loadData(); }

    @FXML
    private void onRecalcAll() {
        statusLabel.setText("Recalculating performance scores...");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                List<Fighter> fighters = fighterDao.findAll();
                for (Fighter f : fighters) analytics.recalculateAndPersist(f.getFighterId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadData());
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Recalc all failed", task.getException());
            statusLabel.setText("Recalc failed");
        });
        new Thread(task, "perf-recalc").start();
    }
}
