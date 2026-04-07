package mmadesktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.User;

public class MainView {
    private final BorderPane root = new BorderPane();
    private Button activeNavBtn;
    private final User loggedUser;

    // Pages (lazy loaded)
    private Node homePage;
    private Node fightersPage;
    private Node eventsPage;
    private Node resultsPage;
    private Node usersPage;
    private Node statsPage;        // Fight Statistics
    private Node performancePage;  // Performance Scores
    private Node rankingsPage;     // Rankings

    public MainView(User loggedUser) {
        this.loggedUser = loggedUser;
        root.getStyleClass().add("root-pane");
        root.setTop(buildTopBar());
        showHome();
    }

    public BorderPane getRoot() { return root; }

    private HBox buildTopBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));

        Label logoIcon = new Label("⬡");
        logoIcon.getStyleClass().add("logo-icon");
        Label logoText = new Label("MMA SYSTEM");
        logoText.getStyleClass().add("logo-text");

        Region spacerL = new Region();
        HBox.setHgrow(spacerL, Priority.ALWAYS);

        // Main navigation
        Button btnHome     = makeNavBtn("🏠 HOME");
        Button btnFighters = makeNavBtn("🥊 FIGHTERS");
        Button btnEvents   = makeNavBtn("📅 EVENTS");
        Button btnResults  = makeNavBtn("🏆 RESULTS");

        btnHome.setOnAction(e     -> { activate(btnHome);     showHome(); });
        btnFighters.setOnAction(e -> { activate(btnFighters); showFighters(); });
        btnEvents.setOnAction(e   -> { activate(btnEvents);   showEvents(); });
        btnResults.setOnAction(e  -> { activate(btnResults);  showResults(); });

        activate(btnHome);

        Region spacerR = new Region();
        HBox.setHgrow(spacerR, Priority.ALWAYS);

        // Admin‑only buttons
        if (loggedUser.hasRole("ADMIN")) {
            Button btnUsers = makeNavBtn("👤 USERS");
            btnUsers.setOnAction(e -> { activate(btnUsers); showUsers(); });
            bar.getChildren().addAll(btnHome, btnFighters, btnEvents, btnResults, btnUsers);
        } else {
            bar.getChildren().addAll(btnHome, btnFighters, btnEvents, btnResults);
        }

        // New module buttons (also admin only)
        if (loggedUser.hasRole("ADMIN")) {
            Button btnStats = makeNavBtn("📊 STATS");
            Button btnPerformance = makeNavBtn("⭐ PERFORMANCE");
            Button btnRankings = makeNavBtn("🏅 RANKINGS");

            btnStats.setOnAction(e -> { activate(btnStats); showStats(); });
            btnPerformance.setOnAction(e -> { activate(btnPerformance); showPerformance(); });
            btnRankings.setOnAction(e -> { activate(btnRankings); showRankings(); });

            bar.getChildren().addAll(btnStats, btnPerformance, btnRankings);
        }

        bar.getChildren().addAll(spacerR);

        Label userLabel = new Label("⬤  " + loggedUser.getUsername() +
                (loggedUser.hasRole("ADMIN") ? " (Admin)" : ""));
        userLabel.getStyleClass().add("user-label");
        bar.getChildren().add(userLabel);

        return bar;
    }

    private Button makeNavBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        return b;
    }

    private void activate(Button btn) {
        if (activeNavBtn != null) activeNavBtn.getStyleClass().remove("nav-active");
        activeNavBtn = btn;
        btn.getStyleClass().add("nav-active");
    }

    private void showHome() {
        if (homePage == null) homePage = new HomePage().getRoot();
        root.setCenter(homePage);
    }

    private void showFighters() {
        if (fightersPage == null) fightersPage = new FightersPage(loggedUser.hasRole("ADMIN")).getRoot();
        root.setCenter(fightersPage);
    }

    private void showEvents() {
        if (eventsPage == null) eventsPage = new EventsPage().getRoot();
        root.setCenter(eventsPage);
    }

    private void showResults() {
        if (resultsPage == null) resultsPage = new ResultsPage(loggedUser.hasRole("ADMIN")).getRoot();
        root.setCenter(resultsPage);
    }

    private void showUsers() {
        if (usersPage == null) usersPage = new UsersPage().getRoot();
        root.setCenter(usersPage);
    }

    // New page methods
    private void showStats() {
        if (statsPage == null) statsPage = new FightStatisticsPage().getRoot();
        root.setCenter(statsPage);
    }

    private void showPerformance() {
        if (performancePage == null) performancePage = new PerformanceScoresPage().getRoot();
        root.setCenter(performancePage);
    }

    private void showRankings() {
        if (rankingsPage == null) rankingsPage = new RankingsPage().getRoot();
        root.setCenter(rankingsPage);
    }
}