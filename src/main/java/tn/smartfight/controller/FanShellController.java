package tn.smartfight.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.smartfight.config.Session;
import tn.smartfight.model.User;

public class FanShellController extends BaseShellController {

    @FXML private Label  welcomeLabel;
    @FXML private Label  pointsLabel;
    @FXML private VBox   sidebarBox;
    @FXML private Button sidebarToggle;

    @FXML private Button btnDashboard;
    @FXML private Button btnLeaderboard;
    @FXML private Button btnBoxers;
    @FXML private Button btnEvents;
    @FXML private Button btnResults;
    @FXML private Button btnStats;
    @FXML private Button btnBlog;
    @FXML private Button btnTickets;
    @FXML private Button btnBookings;
    @FXML private Button btnReactions;
    @FXML private Button btnVoting;
    @FXML private Button btnPredictions;
    @FXML private Button btnProfile;

    private Button  activeBtn;
    private boolean sidebarCollapsed = false;

    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user != null) {
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + user.getUsername());
            }
            if (pointsLabel != null) {
                pointsLabel.setText(user.getPredictionPoints() + " PTS");
            }
        }
        startNotificationPolling();
        onShowDashboard();
    }

    @FXML private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        if (sidebarCollapsed) {
            sidebarBox.getStyleClass().add("sidebar-collapsed");
            sidebarToggle.setText("»");
        } else {
            sidebarBox.getStyleClass().remove("sidebar-collapsed");
            sidebarToggle.setText("«");
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("nav-btn-active");
        activeBtn = btn;
        if (btn != null && !btn.getStyleClass().contains("nav-btn-active")) {
            btn.getStyleClass().add("nav-btn-active");
        }
    }

    @FXML public void onShowDashboard() {
        loadContent("/tn/smartfight/views/fan/FanDashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML public void onShowLeaderboard() {
        loadContent("/tn/smartfight/views/fan/Leaderboard.fxml");
        setActive(btnLeaderboard);
    }

    @FXML public void onShowBoxers() {
        loadContent("/tn/smartfight/views/admin/FightersList.fxml");
        setActive(btnBoxers);
    }

    @FXML public void onShowEvents() {
        loadContent("/tn/smartfight/views/fan/BookingForm.fxml");
        setActive(btnEvents);
    }

    @FXML public void onShowResults() {
        loadContent("/tn/smartfight/views/admin/ResultsList.fxml");
        setActive(btnResults);
    }

    @FXML public void onShowStats() {
        loadContent("/tn/smartfight/views/admin/StatsBoutIndex.fxml");
        setActive(btnStats);
    }

    @FXML public void onShowBlog() {
        loadContent("/tn/smartfight/views/fan/Blog.fxml");
        setActive(btnBlog);
    }

    @FXML public void onShowBookings() {
        loadContent("/tn/smartfight/views/fan/Bookings.fxml");
        setActive(btnBookings);
    }

    @FXML public void onShowReactions() {
        loadContent("/tn/smartfight/views/fan/Reactions.fxml");
        setActive(btnReactions);
    }

    @FXML public void onShowVoting() {
        loadContent("/tn/smartfight/views/fan/FanDashboard.fxml");
        setActive(btnVoting);
    }

    @FXML public void onShowPredictions() {
        loadContent("/tn/smartfight/views/fan/Predictions.fxml");
        setActive(btnPredictions);
    }

    @FXML public void onShowProfile() {
        loadContent("/tn/smartfight/views/fan/FanProfile.fxml");
        setActive(btnProfile);
    }
}
