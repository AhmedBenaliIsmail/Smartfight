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

public class OrganizerShellController {

    @FXML private VBox      sidebar;
    @FXML private VBox      vboxUser;
    @FXML private Label     lblBrand;
    @FXML private Label     lblSubtitle;
    @FXML private Label     lblSignedIn;
    @FXML private Label     lblUserName;
    @FXML private Button    btnEvents;
    @FXML private Button    btnResults;
    @FXML private Button    btnLogout;
    @FXML private Button    btnCollapse;
    @FXML private StackPane contentArea;

    private boolean collapsed = false;
    private static final double W_EXPANDED  = 220;
    private static final double W_COLLAPSED = 58;

    @FXML
    private void initialize() {
        lblUserName.setText(SessionManager.getCurrentUserName());
        navigateTo("EventsAdminView.fxml");
    }

    @FXML private void onEventsClicked()  { navigateTo("EventsAdminView.fxml"); }
    @FXML private void onResultsClicked() { navigateTo("FightResultsAdminView.fxml"); }

    @FXML
    private void onLogoutClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            ((Stage) contentArea.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onToggleSidebar() {
        collapsed = !collapsed;
        double target = collapsed ? W_COLLAPSED : W_EXPANDED;
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(180),
            new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
            new KeyValue(sidebar.maxWidthProperty(),  target, Interpolator.EASE_BOTH)));
        if (collapsed) {
            setLabels(false);
            btnEvents.setText("E"); btnResults.setText("R"); btnLogout.setText("↩");
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabels(true);
                btnEvents.setText("EVENTS"); btnResults.setText("FIGHT RESULTS");
                btnLogout.setText("LOGOUT"); btnCollapse.setText("‹");
            });
        }
        tl.play();
    }

    private void setLabels(boolean v) {
        for (Node n : new Node[]{lblBrand, lblSubtitle, lblSignedIn, lblUserName}) {
            n.setVisible(v); n.setManaged(v);
        }
        vboxUser.setStyle(v
            ? "-fx-padding:4 14 18 18;-fx-border-color:transparent transparent #27272a transparent;-fx-border-width:0 0 1 0;"
            : "-fx-padding:0;-fx-border-width:0;");
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
