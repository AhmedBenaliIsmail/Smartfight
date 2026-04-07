package tn.smartfight.controllers;

import tn.smartfight.session.SessionManager;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;

public class AdminShellController {

    @FXML private VBox     sidebar;
    @FXML private VBox     vboxBrand;
    @FXML private VBox     vboxUser;
    @FXML private Label    lblBrand;
    @FXML private Label    lblSubtitle;
    @FXML private Label    lblSignedIn;
    @FXML private Label    lblAdminName;

    // Nav buttons
    @FXML private Button btnUsers;
    @FXML private Button btnFighters;
    @FXML private Button btnEvents;
    @FXML private Button btnResults;
    @FXML private Button btnBlog;
    @FXML private Button btnBookings;
    @FXML private Button btnPredictions;
    @FXML private Button btnReactions;
    @FXML private Button btnBroadcasts;
    @FXML private Button btnLogout;
    @FXML private Button btnCollapse;

    @FXML private StackPane contentArea;

    private boolean collapsed = false;
    private static final double W_EXPANDED  = 220;
    private static final double W_COLLAPSED = 58;

    @FXML
    private void initialize() {
        lblAdminName.setText(SessionManager.getCurrentUserName());
        navigateTo("UsersAdminView.fxml");
    }

    @FXML private void onUsersClicked()       { navigateTo("UsersAdminView.fxml"); }
    @FXML private void onFightersClicked()    { navigateTo("FightersAdminView.fxml"); }
    @FXML private void onEventsClicked()      { navigateTo("EventsAdminView.fxml"); }
    @FXML private void onResultsClicked()     { navigateTo("FightResultsAdminView.fxml"); }
    @FXML private void onBlogClicked()        { navigateTo("BlogAdminView.fxml"); }
    @FXML private void onBookingsClicked()    { navigateTo("BookingAdminView.fxml"); }
    @FXML private void onPredictionsClicked() { navigateTo("PredictionAdminView.fxml"); }
    @FXML private void onReactionsClicked()   { navigateTo("ReactionAdminView.fxml"); }
    @FXML private void onBroadcastsClicked()  { navigateTo("NotificationAdminView.fxml"); }

    @FXML
    private void onLogoutClicked() {
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

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(180),
            new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
            new KeyValue(sidebar.maxWidthProperty(),  target, Interpolator.EASE_BOTH)
        ));

        if (collapsed) {
            setLabelsVisible(false);
            setButtonIcons();
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabelsVisible(true);
                setButtonLabels();
                btnCollapse.setText("‹");
            });
        }
        tl.play();
    }

    private void setLabelsVisible(boolean v) {
        for (Node n : new Node[]{lblBrand, lblSubtitle, lblSignedIn, lblAdminName}) {
            n.setVisible(v); n.setManaged(v);
        }
        vboxUser.setStyle(v
            ? "-fx-padding:4 16 20 16;-fx-border-color:transparent transparent #27272a transparent;-fx-border-width:0 0 1 0;"
            : "-fx-padding:0;-fx-border-width:0;");
    }

    private void setButtonLabels() {
        btnUsers.setText("USERS");       btnFighters.setText("FIGHTERS");
        btnEvents.setText("EVENTS");     btnResults.setText("FIGHT RESULTS");
        btnBlog.setText("BLOG");         btnBookings.setText("BOOKINGS");
        btnPredictions.setText("PREDICTIONS"); btnReactions.setText("REACTIONS");
        btnBroadcasts.setText("BROADCASTS");   btnLogout.setText("LOGOUT");
        setAlignment(false);
    }

    private void setButtonIcons() {
        btnUsers.setText("U");  btnFighters.setText("F");
        btnEvents.setText("E"); btnResults.setText("R");
        btnBlog.setText("B");   btnBookings.setText("Bk");
        btnPredictions.setText("P"); btnReactions.setText("Rc");
        btnBroadcasts.setText("Bc"); btnLogout.setText("↩");
        setAlignment(true);
    }

    private void setAlignment(boolean center) {
        String align = center ? "-fx-alignment:CENTER;" : "-fx-alignment:CENTER-LEFT;";
        for (Button b : new Button[]{btnUsers, btnFighters, btnEvents, btnResults,
                                     btnBlog, btnBookings, btnPredictions, btnReactions,
                                     btnBroadcasts, btnLogout}) {
            b.setStyle(b.getStyle().replaceAll("-fx-alignment:[^;]+;", "") + align);
        }
    }

    private void navigateTo(String viewName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/" + viewName));
            view.setTranslateX(20); view.setOpacity(0);
            contentArea.getChildren().setAll(view);
            FadeTransition fade = new FadeTransition(Duration.millis(180), view);
            fade.setFromValue(0); fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(Duration.millis(180), view);
            slide.setFromX(20); slide.setToX(0);
            new ParallelTransition(fade, slide).play();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
