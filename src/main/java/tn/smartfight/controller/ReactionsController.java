package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import tn.smartfight.config.Session;
import tn.smartfight.dao.FanReactionDao;
import tn.smartfight.model.FanReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReactionsController {
    private static final Logger LOG = Logger.getLogger(ReactionsController.class.getName());

    @FXML private VBox          feedContainer;
    @FXML private ComboBox<String[]> fightCombo;
    @FXML private VBox          reactionTypeBox;
    @FXML private TextArea      commentArea;
    @FXML private Label         charCountLabel;
    @FXML private Label         postStatusLabel;

    private final FanReactionDao dao = new FanReactionDao();
    private final ToggleGroup    reactionGroup = new ToggleGroup();

    private static final String[] REACTION_TYPES = {
        "FIRE", "SHOCK", "RESPECT", "DOMINANT", "CONTROVERSIAL"
    };
    private static final String[] REACTION_LABELS = {
        "🔥  Fire — incredible performance",
        "😱  Shock — unexpected result",
        "👏  Respect — great fighter",
        "🏆  Dominant — no contest",
        "💔  Controversial — bad decision"
    };

    @FXML
    public void initialize() {
        buildReactionToggles();
        attachCharCounter();
        loadFightCombo();
        loadFeed();
    }

    private void buildReactionToggles() {
        reactionTypeBox.getChildren().clear();
        for (int i = 0; i < REACTION_TYPES.length; i++) {
            ToggleButton tb = new ToggleButton(REACTION_LABELS[i]);
            tb.setToggleGroup(reactionGroup);
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setUserData(REACTION_TYPES[i]);
            String base = "-fx-background-color:#18181b;-fx-text-fill:#d4d4d8;-fx-font-size:12px;" +
                    "-fx-border-color:#27272a;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;" +
                    "-fx-padding:8 10;-fx-alignment:CENTER_LEFT;";
            String sel  = "-fx-background-color:rgba(220,38,38,0.15);-fx-text-fill:#fafafa;-fx-font-size:12px;" +
                    "-fx-border-color:#dc2626;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;" +
                    "-fx-padding:8 10;-fx-alignment:CENTER_LEFT;";
            tb.setStyle(base);
            tb.selectedProperty().addListener((obs, was, now) ->
                    tb.setStyle(now ? sel : base));
            reactionTypeBox.getChildren().add(tb);
        }
    }

    private void attachCharCounter() {
        commentArea.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.length() > 140) {
                commentArea.setText(val.substring(0, 140));
                return;
            }
            int len = val != null ? val.length() : 0;
            charCountLabel.setText(len + " / 140");
            charCountLabel.setStyle(len > 120
                    ? "-fx-text-fill:#dc2626;-fx-font-size:10px;"
                    : "-fx-text-fill:#52525b;-fx-font-size:10px;");
        });
    }

    private void loadFightCombo() {
        Task<List<String[]>> task = new Task<>() {
            @Override protected List<String[]> call() { return dao.findCompletedFights(); }
        };
        task.setOnSucceeded(e -> {
            List<String[]> fights = task.getValue();
            fightCombo.setItems(FXCollections.observableArrayList(fights));
            fightCombo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(String[] item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item[1]);
                }
            });
            fightCombo.setButtonCell(fightCombo.getCellFactory().call(null));
        });
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "loadFightCombo failed", task.getException()));
        new Thread(task, "reactions-fights-load").start();
    }

    private void loadFeed() {
        Task<List<FanReaction>> task = new Task<>() {
            @Override protected List<FanReaction> call() { return dao.findRecent(30); }
        };
        task.setOnSucceeded(e -> renderFeed(task.getValue()));
        task.setOnFailed(e -> LOG.log(Level.SEVERE, "loadFeed failed", task.getException()));
        new Thread(task, "reactions-feed-load").start();
    }

    private void renderFeed(List<FanReaction> reactions) {
        feedContainer.getChildren().clear();
        if (reactions.isEmpty()) {
            Label empty = new Label("No reactions yet — be the first!");
            empty.setStyle("-fx-text-fill:#52525b;-fx-font-size:14px;-fx-padding:32;");
            feedContainer.getChildren().add(empty);
            return;
        }
        for (FanReaction r : reactions) {
            feedContainer.getChildren().add(buildReactionCard(r));
        }
    }

    private HBox buildReactionCard(FanReaction r) {
        // Avatar circle
        Circle avatar = new Circle(20);
        avatar.setStyle("-fx-fill:#27272a;");
        String init = (r.getFanUsername() != null && !r.getFanUsername().isEmpty())
                ? String.valueOf(r.getFanUsername().charAt(0)).toUpperCase() : "?";
        Label initLbl = new Label(init);
        initLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-size:14px;-fx-font-weight:bold;");
        StackPane avatarPane = new StackPane(avatar, initLbl);

        // Card body
        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        // Header row: username + pinned badge + timestamp
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label userLbl = new Label(r.getFanUsername() != null ? r.getFanUsername() : "Fan");
        userLbl.setStyle("-fx-text-fill:#fafafa;-fx-font-weight:bold;-fx-font-size:13px;");
        header.getChildren().add(userLbl);
        if (r.isPinned()) {
            Label pinnedBadge = new Label("📌 PINNED");
            pinnedBadge.setStyle("-fx-background-color:rgba(245,158,11,0.15);-fx-text-fill:#f59e0b;" +
                    "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:2 6;-fx-background-radius:8;");
            header.getChildren().add(pinnedBadge);
        }
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().add(sp);
        String timeStr = r.getReactedAt() != null
                ? r.getReactedAt().toLocalDate().toString() : "";
        Label timeLbl = new Label(timeStr);
        timeLbl.setStyle("-fx-text-fill:#52525b;-fx-font-size:10px;");
        header.getChildren().add(timeLbl);

        // Fight label
        Label fightLbl = new Label(r.getFightLabel());
        fightLbl.setStyle("-fx-text-fill:#71717a;-fx-font-size:11px;");

        // Emoji + comment
        HBox reactionRow = new HBox(8);
        reactionRow.setAlignment(Pos.CENTER_LEFT);
        Label emojiLbl = new Label(r.getEmoji());
        emojiLbl.setStyle("-fx-font-size:24px;");
        Label commentLbl = new Label(r.getComment() != null ? r.getComment() : "");
        commentLbl.setStyle("-fx-text-fill:#d4d4d8;-fx-font-size:13px;");
        commentLbl.setWrapText(true);
        HBox.setHgrow(commentLbl, Priority.ALWAYS);
        reactionRow.getChildren().addAll(emojiLbl, commentLbl);

        body.getChildren().addAll(header, fightLbl, reactionRow);

        HBox card = new HBox(12, avatarPane, body);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(12));
        String bg = r.isPinned()
                ? "-fx-background-color:rgba(245,158,11,0.05);-fx-border-color:rgba(245,158,11,0.3);"
                : "-fx-background-color:#18181b;-fx-border-color:#27272a;";
        card.setStyle(bg + "-fx-border-radius:10;-fx-background-radius:10;");
        return card;
    }

    @FXML
    private void onPostReaction() {
        if (Session.getUser() == null) { postStatusLabel.setText("Not logged in."); return; }

        String[] selectedFight = fightCombo.getValue();
        if (selectedFight == null) { postStatusLabel.setText("Select a fight."); return; }

        Toggle selectedToggle = reactionGroup.getSelectedToggle();
        if (selectedToggle == null) { postStatusLabel.setText("Select a reaction type."); return; }

        String comment = commentArea.getText() != null ? commentArea.getText().trim() : "";
        if (comment.isEmpty()) { postStatusLabel.setText("Write a comment."); return; }

        String reactionType = (String) selectedToggle.getUserData();
        int fightResultId;
        try { fightResultId = Integer.parseInt(selectedFight[0]); }
        catch (NumberFormatException ex) { postStatusLabel.setText("Invalid fight selection."); return; }

        FanReaction reaction = new FanReaction();
        reaction.setReactionType(reactionType);
        reaction.setComment(comment);
        reaction.setFightResultId(fightResultId);
        reaction.setFanId(Session.getUser().getUserId());

        postStatusLabel.setText("Posting…");
        Task<Void> task = new Task<>() {
            @Override protected Void call() { dao.create(reaction); return null; }
        };
        task.setOnSucceeded(e -> {
            postStatusLabel.setText("Reaction posted!");
            commentArea.clear();
            reactionGroup.selectToggle(null);
            loadFeed();
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "onPostReaction failed", task.getException());
            postStatusLabel.setText("Post failed.");
        });
        new Thread(task, "reaction-post").start();
    }
}
