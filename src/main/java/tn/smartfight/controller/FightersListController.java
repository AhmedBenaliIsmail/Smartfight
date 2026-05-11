package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.config.AppConfig;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.model.Fighter;
import tn.smartfight.model.FighterDetails;
import tn.smartfight.service.RankingService;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FightersListController {
    private static final Logger LOG = Logger.getLogger(FightersListController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> styleFilter;
    @FXML private TextField eloMinField;
    @FXML private TextField winRateField;
    @FXML private FlowPane fightersFlow;
    @FXML private Label statusLabel;

    private final FighterDao dao = new FighterDao();
    private List<Fighter> allFighters;

    @FXML
    public void initialize() {
        styleFilter.getSelectionModel().selectFirst();
        load();
    }

    @FXML private void onFilter() { renderCards(); }

    @FXML private void onCreateFighter() { openForm(null); }

    @FXML private void onInjuryAI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/InjuryPredictions.fxml"));
            Scene scene = new Scene(loader.load(), 900, 680);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Injury Prediction Analyst");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not open injury page", e);
        }
    }

    @FXML private void onRecalcRankings() {
        statusLabel.setText("Recalculating...");
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                new RankingService().recomputeAllRankings();
                return null;
            }
        };
        task.setOnSucceeded(e -> statusLabel.setText("Rankings recalculated."));
        task.setOnFailed(e -> statusLabel.setText("Recalc failed."));
        new Thread(task, "recalc").start();
    }

    private void load() {
        statusLabel.setText("Loading...");
        Task<List<Fighter>> task = new Task<>() {
            @Override protected List<Fighter> call() { return dao.findAll(); }
        };
        task.setOnSucceeded(e -> {
            allFighters = task.getValue();
            renderCards();
        });
        task.setOnFailed(e -> statusLabel.setText("Load failed."));
        new Thread(task, "fighters-load").start();
    }

    private void renderCards() {
        if (allFighters == null) return;
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String style  = styleFilter.getValue();
        double eloMin = parseDouble(eloMinField.getText(), 0);
        double wrMin  = parseDouble(winRateField.getText(), 0);

        fightersFlow.getChildren().clear();
        int count = 0;
        for (Fighter f : allFighters) {
            String fullName = (f.getFirstName() + " " + f.getLastName()).toLowerCase();
            if (!search.isEmpty() && !fullName.contains(search)) continue;
            if (style != null && !"All Styles".equals(style) && f.getAiStyleTag() != null
                    && !style.equalsIgnoreCase(f.getAiStyleTag())) continue;
            if (f.getEloRating() < eloMin) continue;
            double total = f.getWins() + f.getLosses() + f.getDraws();
            double wr = total > 0 ? (f.getWins() / total) * 100 : 0;
            if (wr < wrMin) continue;

            fightersFlow.getChildren().add(buildFighterCard(f, wr));
            count++;
        }
        statusLabel.setText(count + " fighters");
    }

    private VBox buildFighterCard(Fighter f, double winRate) {
        VBox card = new VBox(0);
        card.getStyleClass().add("fighter-card");
        card.setPrefWidth(280);

        /* Photo area */
        StackPane photoArea = new StackPane();
        photoArea.setMinHeight(160);
        photoArea.setMaxHeight(160);

        String photoFilename = f.getPhotoFilename();
        boolean photoLoaded = false;
        if (photoFilename != null && !photoFilename.isBlank()) {
            File photoFile = new File(AppConfig.get().uploadDir + "/boxers/" + photoFilename);
            if (photoFile.exists()) {
                String uri = photoFile.toURI().toString();
                photoArea.setStyle(
                    "-fx-background-image:url('" + uri + "');" +
                    "-fx-background-size:cover;" +
                    "-fx-background-position:center center;" +
                    "-fx-background-radius:10 10 0 0;"
                );
                photoLoaded = true;
            }
        }
        if (!photoLoaded) {
            photoArea.setStyle("-fx-background-color:#1f1f1f;-fx-background-radius:10 10 0 0;");
            String initials = (f.getFirstName() != null ? f.getFirstName().substring(0, 1) : "?")
                    + (f.getLastName() != null ? f.getLastName().substring(0, 1) : "");
            Label initialsLbl = new Label(initials.toUpperCase());
            initialsLbl.setStyle("-fx-font-size:40;-fx-font-weight:bold;-fx-text-fill:#374151;");
            photoArea.getChildren().add(initialsLbl);
        }

        /* Style badge overlay */
        if (f.getAiStyleTag() != null && !f.getAiStyleTag().isEmpty()) {
            Label styleBadge = new Label(f.getAiStyleTag().toUpperCase());
            styleBadge.getStyleClass().add(styleTagCss(f.getAiStyleTag()));
            styleBadge.setStyle(styleBadge.getStyle() + "-fx-font-size:9;");
            StackPane.setAlignment(styleBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(styleBadge, new Insets(8));
            photoArea.getChildren().add(styleBadge);
        }

        /* Info area */
        VBox info = new VBox(8);
        info.setPadding(new Insets(14));

        Label name = new Label((f.getFirstName() + " " + f.getLastName()).toUpperCase());
        name.setStyle("-fx-font-size:15;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
        name.setWrapText(true);

        Label division = new Label(f.getWeightDivisionId() != null ? "Division #" + f.getWeightDivisionId() : "Unknown Division");
        division.setStyle("-fx-font-size:11;-fx-text-fill:#9ca3af;");

        /* Stats row */
        HBox statsRow = new HBox(0);
        statsRow.setStyle("-fx-background-color:#111111;-fx-background-radius:6;");
        statsRow.getChildren().addAll(
                statCell("WIN RATE", String.format("%.0f%%", winRate)),
                statDivider(),
                statCell("ELO", String.format("%.0f", f.getEloRating())),
                statDivider(),
                statCell("RECORD", f.getWins() + "-" + f.getLosses() + "-" + f.getDraws()));

        /* Progress bar */
        double progress = Math.min(f.getEloRating() / 5000.0, 1.0);
        ProgressBar pb = new ProgressBar(progress);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar");
        Label pbLabel = new Label(String.format("%.0f/5000 ELO", f.getEloRating()));
        pbLabel.setStyle("-fx-font-size:10;-fx-text-fill:#6b7280;");

        /* Action buttons */
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("btn-icon");
        editBtn.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:14;");
        editBtn.setOnAction(e -> editFighter(f));
        Button delBtn = new Button("🗑");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-font-size:12;-fx-padding:4 8;");
        delBtn.setOnAction(e -> deleteFighter(f));
        actions.getChildren().addAll(editBtn, delBtn);

        info.getChildren().addAll(name, division, statsRow,
                new Label("DIVISION STANDING") {{ setStyle("-fx-font-size:9;-fx-text-fill:#6b7280;-fx-font-weight:bold;"); }},
                pb, pbLabel, actions);
        card.getChildren().addAll(photoArea, info);
        return card;
    }

    private VBox statCell(String label, String value) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(8, 12, 8, 12));
        HBox.setHgrow(cell, Priority.ALWAYS);
        Label v = new Label(value);
        v.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size:9;-fx-text-fill:#6b7280;-fx-font-weight:bold;");
        cell.getChildren().addAll(v, l);
        return cell;
    }

    private Region statDivider() {
        Region r = new Region();
        r.setStyle("-fx-background-color:#2d2d2d;");
        r.setMinWidth(1); r.setMaxWidth(1);
        return r;
    }

    private String styleTagCss(String tag) {
        if (tag == null) return "badge-gray";
        return switch (tag.toUpperCase()) {
            case "SLUGGER"         -> "badge-orange";
            case "TACTICIAN"       -> "badge-purple";
            case "BOXER"           -> "badge-blue";
            case "BRAWLER"         -> "badge-red";
            case "COUNTER-PUNCHER" -> "badge-amber";
            default                -> "badge-gray";
        };
    }

    private void editFighter(Fighter f) {
        FighterDetails full = dao.getById(f.getFighterId());
        openForm(full);
    }

    private void deleteFighter(Fighter f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + f.getFirstName() + " " + f.getLastName() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                dao.deleteById(f.getFighterId());
                load();
            }
        });
    }

    private void openForm(FighterDetails fighter) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/FighterForm.fxml"));
            Scene scene = new Scene(loader.load(), 580, 800);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            FighterFormController ctrl = loader.getController();
            ctrl.setFighter(fighter);
            Stage stage = new Stage();
            stage.setTitle(fighter == null ? "Add Boxer" : "Edit Boxer");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            load();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open FighterForm", e);
        }
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
}
