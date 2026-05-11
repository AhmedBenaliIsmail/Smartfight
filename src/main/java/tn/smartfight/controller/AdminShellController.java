package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.smartfight.auth.FaceIdRecognizer;
import tn.smartfight.config.Session;
import tn.smartfight.dao.UserDao;
import tn.smartfight.util.AccessGuard;

public class AdminShellController extends BaseShellController {

    @FXML private Label avatarLabel;
    @FXML private TextField searchField;
    @FXML private VBox sidebarBox;
    @FXML private Button sidebarToggle;
    private boolean sidebarCollapsed = false;

    /* sidebar buttons — used for active-state styling */
    @FXML private Button btnDashboard;
    @FXML private Button btnLeaderboard;
    @FXML private Button btnUsers;
    @FXML private Button btnFighters;
    @FXML private Button btnEvents;
    @FXML private Button btnChampions;
    @FXML private Button btnResults;
    @FXML private Button btnStats;
    @FXML private Button btnPerf;
    @FXML private Button btnRankings;
    @FXML private Button btnBlog;
    @FXML private Button btnBookings;
    @FXML private Button btnReactions;
    @FXML private Button btnProposals;
    @FXML private Button btnContracts;
    @FXML private Button btnFinance;
    @FXML private Button btnInjury;

    private Button activeBtn;

    @FXML
    public void initialize() {
        if (!AccessGuard.isAdmin()) {
            onLogout();
            return;
        }
        var user = Session.getUser();
        if (user != null && avatarLabel != null) {
            String initial = user.getUsername() != null && !user.getUsername().isEmpty()
                    ? String.valueOf(user.getUsername().charAt(0)).toUpperCase() : "A";
            avatarLabel.setText(initial);
        }
        if (searchField != null) {
            searchField.setOnAction(e -> {
                // global search — load dashboard with filter for now
                loadContent("/tn/smartfight/views/admin/AdminDashboard.fxml");
                setActive(btnDashboard);
            });
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

    /* ── sidebar navigation ─────────────────────────────── */

    @FXML public void onShowDashboard() {
        loadContent("/tn/smartfight/views/admin/AdminDashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML public void onShowLeaderboard() {
        loadContent("/tn/smartfight/views/admin/AdminLeaderboard.fxml");
        setActive(btnLeaderboard);
    }

    @FXML public void onShowUsers() {
        loadContent("/tn/smartfight/views/admin/AdminUsers.fxml");
        setActive(btnUsers);
    }

    @FXML public void onShowFighters() {
        loadContent("/tn/smartfight/views/admin/FightersList.fxml");
        setActive(btnFighters);
    }

    @FXML public void onShowEvents() {
        loadContent("/tn/smartfight/views/admin/EventsList.fxml");
        setActive(btnEvents);
    }

    @FXML public void onShowChampions() {
        loadContent("/tn/smartfight/views/admin/EventsList.fxml");
        setActive(btnChampions);
    }

    @FXML public void onShowResults() {
        loadContent("/tn/smartfight/views/admin/ResultsList.fxml");
        setActive(btnResults);
    }

    @FXML public void onShowStats() {
        loadContent("/tn/smartfight/views/admin/StatsBoutIndex.fxml");
        setActive(btnStats);
    }

    @FXML public void onShowPerformance() {
        loadContent("/tn/smartfight/views/admin/Performance.fxml");
        setActive(btnPerf);
    }

    @FXML public void onShowRankings() {
        loadContent("/tn/smartfight/views/admin/RankingsList.fxml");
        setActive(btnRankings);
    }

    @FXML public void onShowBlog() {
        loadContent("/tn/smartfight/views/admin/AdminBlogList.fxml");
        setActive(btnBlog);
    }

    @FXML public void onShowBookings() {
        loadContent("/tn/smartfight/views/admin/AdminBookings.fxml");
        setActive(btnBookings);
    }

    @FXML public void onShowReactions() {
        loadContent("/tn/smartfight/views/admin/AdminFanReactions.fxml");
        setActive(btnReactions);
    }

    @FXML public void onShowProposals() {
        loadContent("/tn/smartfight/views/admin/MatchProposals.fxml");
        setActive(btnProposals);
    }

    @FXML public void onShowContracts() {
        loadContent("/tn/smartfight/views/admin/ContractList.fxml");
        setActive(btnContracts);
    }

    @FXML public void onShowFinance() {
        loadContent("/tn/smartfight/views/admin/Finance.fxml");
        setActive(btnFinance);
    }

    @FXML public void onShowInjury() {
        loadContent("/tn/smartfight/views/admin/InjuryPredictions.fxml");
        setActive(btnInjury);
    }

    @FXML public void onRegisterFaceId() {
        if (Session.getUser() == null) return;
        int userId = Session.getUser().getUserId();
        UserDao userDao = new UserDao();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                FaceIdRecognizer.registerFaceId(userId, userDao);
                return null;
            }
        };
        task.setOnSucceeded(e -> new Alert(Alert.AlertType.INFORMATION, "Face ID registered.").showAndWait());
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            new Alert(Alert.AlertType.ERROR, ex != null ? ex.getMessage() : "Face ID failed.").showAndWait();
        });
        new Thread(task, "faceid-register").start();
    }

    /* ── active nav state ───────────────────────────────── */

    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("nav-btn-active");
        }
        activeBtn = btn;
        if (btn != null && !btn.getStyleClass().contains("nav-btn-active")) {
            btn.getStyleClass().add("nav-btn-active");
        }
    }
}
