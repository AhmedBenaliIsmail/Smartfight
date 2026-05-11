package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.dao.EventDao;
import tn.smartfight.dao.FightResultDao;
import tn.smartfight.dao.FighterDao;
import tn.smartfight.model.Event;
import tn.smartfight.model.FightResult;
import tn.smartfight.model.Fighter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageCardController {
    private static final Logger LOG = Logger.getLogger(ManageCardController.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label cardFullLabel;
    @FXML private VBox boutsContainer;
    @FXML private VBox addFightSection;
    @FXML private ComboBox<Fighter> fighter1Combo;
    @FXML private ComboBox<Fighter> fighter2Combo;
    @FXML private ComboBox<String> fightNumCombo;
    @FXML private Label statusLabel;

    private Event event;
    private final FightResultDao resultDao = new FightResultDao();
    private final FighterDao fighterDao = new FighterDao();

    public void setEvent(Event ev) {
        this.event = ev;
        titleLabel.setText("MANAGE CARD: " + (ev.getEventName() != null ? ev.getEventName().toUpperCase() : ""));
        loadFighters();
        refresh();
    }

    @FXML
    public void initialize() {}

    private void loadFighters() {
        Task<List<Fighter>> t = new Task<>() {
            @Override protected List<Fighter> call() { return fighterDao.findAll(); }
        };
        t.setOnSucceeded(e -> {
            List<Fighter> fighters = t.getValue();
            fighter1Combo.setCellFactory(lv -> fighterCell());
            fighter1Combo.setButtonCell(fighterCell());
            fighter2Combo.setCellFactory(lv -> fighterCell());
            fighter2Combo.setButtonCell(fighterCell());
            fighter1Combo.setItems(FXCollections.observableArrayList(fighters));
            fighter2Combo.setItems(FXCollections.observableArrayList(fighters));
        });
        new Thread(t, "mc-fighters").start();
    }

    private ListCell<Fighter> fighterCell() {
        return new ListCell<>() {
            @Override protected void updateItem(Fighter f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getFirstName() + " " + f.getLastName()
                        + " [" + String.format("%.0f", f.getEloRating()) + " ELO]");
            }
        };
    }

    private void refresh() {
        if (event == null) return;
        Task<List<FightResult>> t = new Task<>() {
            @Override protected List<FightResult> call() {
                return resultDao.findByEventId(event.getEventId());
            }
        };
        t.setOnSucceeded(e -> {
            List<FightResult> bouts = t.getValue();
            boutsContainer.getChildren().clear();

            boolean cardFull = bouts.size() >= 3;
            cardFullLabel.setVisible(cardFull);
            addFightSection.setVisible(!cardFull);
            addFightSection.setManaged(!cardFull);

            if (bouts.isEmpty()) {
                Label empty = new Label("No fights scheduled yet.");
                empty.setStyle("-fx-text-fill:#9ca3af;-fx-font-size:14;-fx-padding:20;");
                boutsContainer.getChildren().add(empty);
            } else {
                for (FightResult fr : bouts) {
                    boutsContainer.getChildren().add(buildBoutRow(fr));
                }
            }
        });
        new Thread(t, "mc-refresh").start();
    }

    private HBox buildBoutRow(FightResult fr) {
        HBox row = new HBox(16);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);

        /* Bout badge */
        Label boutBadge = new Label("BOUT #" + fr.getFightNumber());
        boutBadge.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:20;-fx-padding:4 12;-fx-font-size:11;");

        /* Fighter 1 */
        VBox f1Box = new VBox(4);
        f1Box.setAlignment(Pos.CENTER);
        Label f1Init = new Label(fr.getFighter1Name() != null ? initials(fr.getFighter1Name()) : "?");
        f1Init.setStyle("-fx-background-color:#1f1f1f;-fx-text-fill:#ffffff;-fx-font-size:20;" +
                "-fx-font-weight:bold;-fx-background-radius:40;-fx-min-width:50;-fx-min-height:50;" +
                "-fx-max-width:50;-fx-max-height:50;-fx-alignment:center;");
        Label f1Name = new Label(fr.getFighter1Name() != null ? fr.getFighter1Name().toUpperCase() : "TBD");
        f1Name.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13;");
        f1Box.getChildren().addAll(f1Init, f1Name);

        /* VS */
        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size:18;-fx-font-weight:bold;-fx-text-fill:#dc2626;");

        /* Fighter 2 */
        VBox f2Box = new VBox(4);
        f2Box.setAlignment(Pos.CENTER);
        Label f2Init = new Label(fr.getFighter2Name() != null ? initials(fr.getFighter2Name()) : "?");
        f2Init.setStyle("-fx-background-color:#1f1f1f;-fx-text-fill:#ffffff;-fx-font-size:20;" +
                "-fx-font-weight:bold;-fx-background-radius:40;-fx-min-width:50;-fx-min-height:50;" +
                "-fx-max-width:50;-fx-max-height:50;-fx-alignment:center;");
        Label f2Name = new Label(fr.getFighter2Name() != null ? fr.getFighter2Name().toUpperCase() : "TBD");
        f2Name.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13;");
        f2Box.getChildren().addAll(f2Init, f2Name);

        /* Status */
        Label statusBadge = new Label(fr.getStatus() != null ? fr.getStatus() : "PENDING");
        statusBadge.getStyleClass().add(statusCss(fr.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        /* Actions */
        Button enterBtn = new Button("✏ Enter Result");
        enterBtn.getStyleClass().add("btn-primary");
        enterBtn.setOnAction(e -> openResultForm(fr));

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setStyle("-fx-padding:6 10;");
        deleteBtn.setOnAction(e -> deleteFight(fr));

        row.getChildren().addAll(boutBadge, f1Box, vs, f2Box, statusBadge, spacer, enterBtn, deleteBtn);
        return row;
    }

    @FXML private void onAddFight() {
        Fighter f1 = fighter1Combo.getValue();
        Fighter f2 = fighter2Combo.getValue();
        String numStr = fightNumCombo.getValue();
        if (f1 == null || f2 == null || numStr == null) {
            statusLabel.setText("Select both fighters and bout number.");
            return;
        }
        if (f1.getFighterId() == f2.getFighterId()) {
            statusLabel.setText("Fighters must be different.");
            return;
        }
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                FightResult fr = new FightResult();
                fr.setEventId(event.getEventId());
                fr.setFighter1Id(f1.getFighterId());
                fr.setFighter2Id(f2.getFighterId());
                fr.setFightNumber(Integer.parseInt(numStr));
                fr.setStatus("SCHEDULED");
                fr.setScheduledRounds(12);
                resultDao.create(fr);
                return null;
            }
        };
        t.setOnSucceeded(e -> { statusLabel.setText("Fight added."); refresh(); });
        t.setOnFailed(e -> statusLabel.setText("Error: " + t.getException().getMessage()));
        new Thread(t, "mc-add").start();
    }

    @FXML private void onAISuggest() {
        statusLabel.setText("AI matching requires fighter pool — use manual selection.");
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "AI Matchmaking is available from the Voting Pool page.\n" +
                "Select fighters manually above for this event card.");
        a.setHeaderText("AI Match Suggestion");
        a.showAndWait();
    }

    @FXML private void onBackToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/EventsList.fxml"));
            javafx.scene.Node pane = loader.load();
            var host = boutsContainer.getScene().lookup("#contentHost");
            if (host instanceof StackPane sp) sp.getChildren().setAll(pane);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Back to events failed", e);
        }
    }

    private void openResultForm(FightResult fr) {
        try {
            FightResult full = resultDao.getById(fr.getResultId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/ResultForm.fxml"));
            Scene scene = new Scene(loader.load(), 560, 720);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            ResultFormController ctrl = loader.getController();
            ctrl.setResult(full != null ? full : fr);
            Stage stage = new Stage();
            stage.setTitle("Enter Fight Result");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
            refresh();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open ResultForm from ManageCard", e);
        }
    }

    private void deleteFight(FightResult fr) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove Bout #" + fr.getFightNumber() + " from this card?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn.getButtonData().isDefaultButton()) {
                resultDao.deleteById(fr.getResultId());
                refresh();
            }
        });
    }

    private static String initials(String fullName) {
        if (fullName == null) return "?";
        String[] parts = fullName.split(" ");
        return parts.length >= 2
                ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
                : fullName.substring(0, Math.min(2, fullName.length()));
    }

    private static String statusCss(String status) {
        if (status == null) return "badge-gray";
        return switch (status.toUpperCase()) {
            case "COMPLETED" -> "badge-green";
            case "PENDING"   -> "badge-amber";
            case "SCHEDULED" -> "badge-gray";
            default          -> "badge-gray";
        };
    }
}
