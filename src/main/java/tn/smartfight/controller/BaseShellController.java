package tn.smartfight.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.smartfight.config.Session;
import tn.smartfight.dao.NotificationDao;
import tn.smartfight.model.Notification;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseShellController {
    private static final Logger LOG = Logger.getLogger(BaseShellController.class.getName());

    @FXML protected StackPane contentHost;
    @FXML protected Button bellBtn;
    @FXML protected Label badgeLabel;

    private final NotificationDao notifDao = new NotificationDao();
    private Timeline notifPoller;
    private Popup notifPopup;
    private ListView<Notification> notifListView;

    protected void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            contentHost.getChildren().clear();
            contentHost.getChildren().add(loader.load());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load content: " + fxmlPath, e);
        }
    }

    protected void startNotificationPolling() {
        if (bellBtn == null) return;
        buildNotifPopup();
        refreshBadge();
        notifPoller = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshBadge()));
        notifPoller.setCycleCount(Timeline.INDEFINITE);
        notifPoller.play();
    }

    private void refreshBadge() {
        if (Session.getUser() == null) return;
        int uid = Session.getUser().getUserId();
        Task<Integer> t = new Task<>() {
            @Override protected Integer call() { return notifDao.countUnread(uid); }
        };
        t.setOnSucceeded(e -> {
            int count = t.getValue();
            if (badgeLabel != null) {
                badgeLabel.setVisible(count > 0);
                badgeLabel.setText(count > 9 ? "9+" : String.valueOf(count));
            }
        });
        new Thread(t, "notif-poll").start();
    }

    private void buildNotifPopup() {
        notifListView = new ListView<>();
        notifListView.setPrefSize(330, 280);
        notifListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                String time = n.getCreatedAt() != null
                        ? n.getCreatedAt().toLocalTime().withNano(0).toString()
                        : "";
                setText((n.isRead() ? "  " : "● ") + n.getMessage() + "  [" + time + "]");
                setStyle(n.isRead() ? "-fx-text-fill: #666;" : "-fx-font-weight: bold;");
            }
        });
        notifListView.setOnMouseClicked(e -> {
            Notification sel = notifListView.getSelectionModel().getSelectedItem();
            if (sel != null && !sel.isRead()) {
                Task<Void> t = new Task<>() {
                    @Override protected Void call() {
                        notifDao.markAsRead(sel.getNotificationId());
                        return null;
                    }
                };
                t.setOnSucceeded(ev -> { sel.setRead(true); notifListView.refresh(); refreshBadge(); });
                new Thread(t, "notif-mark").start();
            }
        });

        Button markAll = new Button("Mark all read");
        markAll.setOnAction(e -> {
            if (Session.getUser() == null) return;
            int uid = Session.getUser().getUserId();
            Task<Void> t = new Task<>() {
                @Override protected Void call() { notifDao.markAllReadForUser(uid); return null; }
            };
            t.setOnSucceeded(ev -> {
                notifListView.getItems().forEach(n -> n.setRead(true));
                notifListView.refresh();
                refreshBadge();
            });
            new Thread(t, "notif-mark-all").start();
        });

        Button close = new Button("Close");
        close.setOnAction(e -> notifPopup.hide());

        HBox btns = new HBox(8, markAll, close);
        btns.setPadding(new Insets(6, 8, 8, 8));

        Label title = new Label("Notifications");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        title.setPadding(new Insets(8, 8, 4, 8));

        VBox box = new VBox(title, notifListView, btns);
        box.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");

        notifPopup = new Popup();
        notifPopup.getContent().add(box);
        notifPopup.setAutoHide(true);
    }

    @FXML
    protected void onBellClick() {
        if (notifPopup == null || bellBtn == null) return;
        if (notifPopup.isShowing()) { notifPopup.hide(); return; }
        if (Session.getUser() != null) {
            int uid = Session.getUser().getUserId();
            Task<List<Notification>> t = new Task<>() {
                @Override protected List<Notification> call() {
                    return notifDao.findRecentByUser(uid, 20);
                }
            };
            t.setOnSucceeded(e -> {
                notifListView.getItems().setAll(t.getValue());
                Bounds b = bellBtn.localToScreen(bellBtn.getBoundsInLocal());
                notifPopup.show(bellBtn, b.getMinX() - 250, b.getMaxY() + 4);
            });
            new Thread(t, "notif-load").start();
        }
    }

    @FXML
    protected void onLogout() {
        if (notifPoller != null) notifPoller.stop();
        try {
            Session.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/smartfight/views/Login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            var css = getClass().getResource("/styles/smartfight.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) contentHost.getScene().getWindow();
            stage.setMaximized(false);
            stage.setMinWidth(860);
            stage.setMinHeight(580);
            stage.setScene(scene);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Logout failed", e);
        }
    }
}
