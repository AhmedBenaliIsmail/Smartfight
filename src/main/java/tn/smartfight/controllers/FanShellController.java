package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;

public class FanShellController {

    @FXML private VBox    sidebar;
    @FXML private VBox    vboxBrand;
    @FXML private VBox    vboxUser;
    @FXML private HBox    hboxNotif;
    @FXML private Label   lblBrand;
    @FXML private Label   lblSubtitle;
    @FXML private Label   lblSignedIn;
    @FXML private Label   lblFanName;
    @FXML private Label   lblUnreadCount;
    @FXML private Button  btnBlog;
    @FXML private Button  btnBookings;
    @FXML private Button  btnPredictions;
    @FXML private Button  btnReactions;
    @FXML private Button  btnNotifications;
    @FXML private Button  btnSwitchRole;
    @FXML private Button  btnCollapse;
    @FXML private StackPane contentArea;

    private final FanNotificationService notifService = new FanNotificationService();
    private boolean collapsed = false;
    private static final double W_EXPANDED  = 256;
    private static final double W_COLLAPSED = 64;

    private static final String[][] NAV = {
        {"NEWS", "N"}, {"BOOKINGS", "Bk"}, {"PREDICTIONS", "P"},
        {"REACTIONS", "R"}, {"NOTIFICATIONS", "Nt"}, {"SWITCH ROLE", "↩"}
    };

    @FXML
    private void initialize() {
        lblFanName.setText(SessionManager.getCurrentUserName());
        refreshUnreadBadge();
        navigateTo("BlogReadView.fxml");
    }

    @FXML private void onBlogClicked()        { navigateTo("BlogReadView.fxml"); }
    @FXML private void onBookingsClicked()    { navigateTo("BookingView.fxml"); }
    @FXML private void onPredictionsClicked() { navigateTo("PredictionView.fxml"); }
    @FXML private void onReactionsClicked()   { navigateTo("ReactionWallView.fxml"); }

    @FXML
    private void onNotificationsClicked() {
        navigateTo("NotificationView.fxml");
        refreshUnreadBadge();
    }

    @FXML
    private void onSwitchRoleClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onToggleSidebar() {
        collapsed = !collapsed;
        double target = collapsed ? W_COLLAPSED : W_EXPANDED;

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(200),
            new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
            new KeyValue(sidebar.maxWidthProperty(),  target, Interpolator.EASE_BOTH)
        ));

        if (collapsed) {
            setLabelsVisible(false);
            setNavTexts(NAV[0][1], NAV[1][1], NAV[2][1], NAV[3][1], NAV[4][1], NAV[5][1]);
            setNavAlignment(true);
            // Hide badge in collapsed mode — it'll show on the button itself
            lblUnreadCount.setVisible(false);
            lblUnreadCount.setManaged(false);
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabelsVisible(true);
                setNavTexts(NAV[0][0], NAV[1][0], NAV[2][0], NAV[3][0], NAV[4][0], NAV[5][0]);
                setNavAlignment(false);
                refreshUnreadBadge();
            });
            btnCollapse.setText("‹");
        }

        tl.play();
    }

    public void refreshUnreadBadge() {
        int count = notifService.getUnreadCount(SessionManager.getCurrentUserId());
        lblUnreadCount.setText(count > 0 ? String.valueOf(count) : "");
        lblUnreadCount.setVisible(!collapsed && count > 0);
        lblUnreadCount.setManaged(!collapsed && count > 0);
    }

    private void setLabelsVisible(boolean visible) {
        for (Node n : new Node[]{lblBrand, lblSubtitle, lblSignedIn, lblFanName}) {
            n.setVisible(visible);
            n.setManaged(visible);
        }
        vboxUser.setStyle(visible
            ? "-fx-padding: 4 16 24 20; -fx-border-color: transparent transparent #27272a transparent; -fx-border-width: 0 0 1 0;"
            : "-fx-padding: 0; -fx-border-width: 0;"
        );
    }

    private void setNavTexts(String news, String book, String pred, String react, String notif, String sw) {
        btnBlog.setText(news);
        btnBookings.setText(book);
        btnPredictions.setText(pred);
        btnReactions.setText(react);
        btnNotifications.setText(notif);
        btnSwitchRole.setText(sw);
    }

    private void setNavAlignment(boolean center) {
        String align = center ? "-fx-alignment: CENTER;" : "-fx-alignment: CENTER-LEFT;";
        for (Button b : new Button[]{btnBlog, btnBookings, btnPredictions, btnReactions, btnNotifications, btnSwitchRole}) {
            b.setStyle(b.getStyle().replaceAll("-fx-alignment:[^;]+;", "") + align);
        }
    }

    private void navigateTo(String viewName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/" + viewName));
            view.setTranslateX(24);
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0); fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(200), view);
            slide.setFromX(24); slide.setToX(0);

            new ParallelTransition(fade, slide).play();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
