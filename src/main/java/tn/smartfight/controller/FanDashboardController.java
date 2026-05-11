package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import tn.smartfight.config.AppConfig;
import tn.smartfight.config.DBConnection;
import tn.smartfight.config.Session;
import tn.smartfight.model.BlogArticle;
import tn.smartfight.model.Event;
import tn.smartfight.model.Fighter;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FanDashboardController {

    private static final Logger LOG = Logger.getLogger(FanDashboardController.class.getName());

    @FXML private Label statusLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label pointsSubtitle;
    @FXML private Label pointsBadge;
    @FXML private HBox  fightersRow;
    @FXML private VBox  votingContainer;
    @FXML private VBox  eventsContainer;
    @FXML private VBox  resultsContainer;
    @FXML private HBox  blogContainer;

    private List<Fighter>     topFighters;
    private List<Object[]>    proposals;
    private List<Event>       upcomingEvents;
    private List<Object[]>    recentResults;
    private List<BlogArticle> blogHighlights;

    @FXML
    public void initialize() {
        if (Session.getUser() != null) {
            welcomeLabel.setText("Welcome back, " + Session.getUser().getUsername() + "!");
            int pts = Session.getUser().getPredictionPoints();
            pointsBadge.setText(pts + " PTS");
            pointsSubtitle.setText("You have " + pts + " prediction point" + (pts == 1 ? "" : "s"));
        }
        loadDashboard();
    }

    private void loadDashboard() {
        statusLabel.setText("Loading dashboard...");
        statusLabel.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                DataSource ds = DBConnection.getDataSource();
                topFighters    = loadTopFighters(ds);
                proposals      = loadProposals(ds);
                upcomingEvents = loadUpcomingEvents(ds);
                recentResults  = loadRecentResults(ds);
                blogHighlights = loadBlogHighlights(ds);
                return null;
            }
        };
        task.setOnSucceeded(e -> { statusLabel.setVisible(false); populate(); });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Dashboard load failed", task.getException());
            statusLabel.setText("Failed to load dashboard.");
        });
        new Thread(task, "fan-dashboard-load").start();
    }

    // ── DB queries ────────────────────────────────────────────────────────────

    private List<Fighter> loadTopFighters(DataSource ds) {
        String sql =
            "SELECT fighterId, firstName, lastName, photo_filename, eloRating, wins, losses, draws, " +
            "COALESCE(performanceScore, eloRating, 0) AS ps " +
            "FROM fighters ORDER BY ps DESC LIMIT 3";
        List<Fighter> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Fighter f = new Fighter();
                f.setFighterId(rs.getInt("fighterId"));
                f.setFirstName(rs.getString("firstName"));
                f.setLastName(rs.getString("lastName"));
                f.setPhotoFilename(rs.getString("photo_filename"));
                f.setEloRating(rs.getDouble("eloRating"));
                f.setWins(rs.getInt("wins"));
                f.setLosses(rs.getInt("losses"));
                f.setDraws(rs.getInt("draws"));
                list.add(f);
            }
        } catch (Exception e) { LOG.log(Level.WARNING, "loadTopFighters", e); }
        return list;
    }

    private List<Object[]> loadProposals(DataSource ds) {
        String sql =
            "SELECT mp.id, CONCAT(f1.firstName,' ',f1.lastName), CONCAT(f2.firstName,' ',f2.lastName), " +
            "mp.vote_count, mp.notes " +
            "FROM match_proposal mp " +
            "JOIN fighters f1 ON f1.fighterId=mp.fighter1_id " +
            "JOIN fighters f2 ON f2.fighterId=mp.fighter2_id " +
            "WHERE mp.status='PENDING' ORDER BY mp.vote_count DESC LIMIT 3";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getString(5)});
        } catch (Exception e) { LOG.log(Level.WARNING, "loadProposals", e); }
        return list;
    }

    private List<Event> loadUpcomingEvents(DataSource ds) {
        String sql = "SELECT eventId,eventName,eventDate,venue,city,status " +
                     "FROM events WHERE status='SCHEDULED' ORDER BY eventDate ASC LIMIT 4";
        List<Event> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Event ev = new Event();
                ev.setEventId(rs.getInt("eventId"));
                ev.setEventName(rs.getString("eventName"));
                Date d = rs.getDate("eventDate");
                if (d != null) ev.setEventDate(d.toLocalDate());
                ev.setVenue(rs.getString("venue"));
                ev.setCity(rs.getString("city"));
                ev.setStatus(rs.getString("status"));
                list.add(ev);
            }
        } catch (Exception e) { LOG.log(Level.WARNING, "loadUpcomingEvents", e); }
        return list;
    }

    private List<Object[]> loadRecentResults(DataSource ds) {
        String sql =
            "SELECT fr.resultId, f1.lastName, f2.lastName, " +
            "COALESCE(fw.lastName,'DRAW') AS winnerLast, fr.methodOfVictory, fr.roundNumber " +
            "FROM fight_results fr " +
            "JOIN fighters f1 ON f1.fighterId=fr.fighter1Id " +
            "JOIN fighters f2 ON f2.fighterId=fr.fighter2Id " +
            "LEFT JOIN fighters fw ON fw.fighterId=fr.winnerId " +
            "JOIN events ev ON ev.eventId=fr.eventId " +
            "WHERE ev.status='COMPLETED' ORDER BY fr.resultId DESC LIMIT 5";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getInt(6)});
        } catch (Exception e) { LOG.log(Level.WARNING, "loadRecentResults", e); }
        return list;
    }

    private List<BlogArticle> loadBlogHighlights(DataSource ds) {
        String sql = "SELECT id,title,summary,view_count,created_at,image_path " +
                     "FROM blog_article WHERE status='PUBLISHED' ORDER BY created_at DESC LIMIT 4";
        List<BlogArticle> list = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BlogArticle a = new BlogArticle();
                a.setId(rs.getInt("id"));
                a.setTitle(rs.getString("title"));
                a.setSummary(rs.getString("summary"));
                a.setViewCount(rs.getInt("view_count"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
                a.setImagePath(rs.getString("image_path"));
                list.add(a);
            }
        } catch (Exception e) { LOG.log(Level.WARNING, "loadBlogHighlights", e); }
        return list;
    }

    // ── Populate (FX thread) ──────────────────────────────────────────────────

    private void populate() {
        populateFighters();
        populateVoting();
        populateEvents();
        populateResults();
        populateBlog();
    }

    private void populateFighters() {
        fightersRow.getChildren().clear();
        if (topFighters == null || topFighters.isEmpty()) {
            fightersRow.getChildren().add(muted("No fighters found.")); return;
        }
        for (Fighter f : topFighters) fightersRow.getChildren().add(buildFighterCard(f));
    }

    private void populateVoting() {
        var header = votingContainer.getChildren().get(0);
        votingContainer.getChildren().setAll(header);
        if (proposals == null || proposals.isEmpty()) {
            votingContainer.getChildren().add(muted("No pending proposals.")); return;
        }
        for (Object[] p : proposals) votingContainer.getChildren().add(buildProposalCard(p));
    }

    private void populateEvents() {
        var header = eventsContainer.getChildren().get(0);
        eventsContainer.getChildren().setAll(header);
        if (upcomingEvents == null || upcomingEvents.isEmpty()) {
            eventsContainer.getChildren().add(muted("No upcoming events.")); return;
        }
        for (Event ev : upcomingEvents) eventsContainer.getChildren().add(buildEventCard(ev));
    }

    private void populateResults() {
        resultsContainer.getChildren().clear();
        if (recentResults == null || recentResults.isEmpty()) {
            resultsContainer.getChildren().add(muted("No recent results.")); return;
        }
        for (Object[] r : recentResults) resultsContainer.getChildren().add(buildResultRow(r));
    }

    private void populateBlog() {
        blogContainer.getChildren().clear();
        if (blogHighlights == null || blogHighlights.isEmpty()) {
            blogContainer.getChildren().add(muted("No articles published.")); return;
        }
        for (BlogArticle a : blogHighlights) blogContainer.getChildren().add(buildBlogCard(a));
    }

    // ── Card builders ─────────────────────────────────────────────────────────

    private VBox buildFighterCard(Fighter f) {
        VBox card = new VBox(0);
        card.setMinWidth(180); card.setMaxWidth(220);
        card.getStyleClass().add("fighter-card");

        StackPane photo = new StackPane();
        photo.setMinHeight(120); photo.setMaxHeight(120);
        String uri = resolvePhoto(f.getPhotoFilename());
        if (uri != null) {
            photo.setStyle("-fx-background-radius:10 10 0 0;-fx-background-image:url('" + uri + "');" +
                           "-fx-background-size:cover;-fx-background-position:center;");
        } else {
            photo.setStyle("-fx-background-radius:10 10 0 0;-fx-background-color:#27272a;");
            Label ini = new Label(initials(f.getFirstName(), f.getLastName()));
            ini.setStyle("-fx-font-size:32px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
            photo.getChildren().add(ini);
        }
        Label elo = new Label(String.format("%.0f ELO", f.getEloRating()));
        elo.getStyleClass().add("badge-red");
        elo.setStyle(elo.getStyle() + "-fx-font-size:10px;-fx-padding:3 8;");
        StackPane.setAlignment(elo, Pos.TOP_RIGHT);
        StackPane.setMargin(elo, new Insets(8));
        photo.getChildren().add(elo);

        VBox info = new VBox(4); info.setPadding(new Insets(10, 12, 12, 12));
        Label name = new Label(f.getFirstName() + " " + f.getLastName());
        name.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13px;");
        name.setWrapText(true);
        Label rec = new Label(f.getWins() + "W - " + f.getLosses() + "L" +
                              (f.getDraws() > 0 ? " - " + f.getDraws() + "D" : ""));
        rec.getStyleClass().add("caption");
        info.getChildren().addAll(name, rec);
        card.getChildren().addAll(photo, info);
        return card;
    }

    private VBox buildProposalCard(Object[] p) {
        int id = (int) p[0]; String f1 = (String) p[1]; String f2 = (String) p[2];
        int votes = (int) p[3]; String notes = (String) p[4];

        VBox card = new VBox(10); card.getStyleClass().add("proposal-card"); card.setPadding(new Insets(14));
        Label matchup = new Label(f1 + "  vs  " + f2);
        matchup.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:14px;");
        matchup.setWrapText(true);

        Label badge = new Label(votes + " votes"); badge.getStyleClass().add("badge-gray");
        card.getChildren().addAll(matchup, badge);

        if (notes != null && !notes.isBlank()) {
            Label nl = new Label(notes); nl.getStyleClass().add("text-muted");
            nl.setWrapText(true); nl.setStyle("-fx-font-size:11px;"); card.getChildren().add(nl);
        }
        Button btn = new Button("VOTE"); btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> onVote(id, btn, badge));
        card.getChildren().add(btn);
        return card;
    }

    private HBox buildEventCard(Event ev) {
        HBox card = new HBox(14); card.getStyleClass().add("event-card");
        card.setAlignment(Pos.CENTER_LEFT); card.setPadding(new Insets(12));

        VBox dateBadge = new VBox(2); dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setMinWidth(48); dateBadge.setMaxWidth(48);
        dateBadge.setStyle("-fx-background-color:#dc2626;-fx-background-radius:8;-fx-padding:6 0;");
        LocalDate d = ev.getEventDate();
        if (d != null) {
            Label day = new Label(String.valueOf(d.getDayOfMonth()));
            day.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
            Label mon = new Label(d.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase());
            mon.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
            dateBadge.getChildren().addAll(day, mon);
        } else {
            Label tbd = new Label("TBD"); tbd.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#ffffff;");
            dateBadge.getChildren().add(tbd);
        }

        VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(ev.getEventName() != null ? ev.getEventName() : "Event");
        name.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13px;"); name.setWrapText(true);
        String venue = "";
        if (ev.getVenue() != null && !ev.getVenue().isBlank()) venue = ev.getVenue();
        if (ev.getCity()  != null && !ev.getCity().isBlank())
            venue = venue.isEmpty() ? ev.getCity() : venue + ", " + ev.getCity();
        Label vl = new Label(venue.isEmpty() ? "TBD" : venue); vl.getStyleClass().add("caption");
        Label sl = new Label(ev.getStatus() != null ? ev.getStatus() : "SCHEDULED");
        sl.getStyleClass().add("badge-gray");
        info.getChildren().addAll(name, vl, sl);
        card.getChildren().addAll(dateBadge, info);
        return card;
    }

    private HBox buildResultRow(Object[] r) {
        String f1 = (String) r[1]; String f2 = (String) r[2];
        String winner = (String) r[3]; String method = r[4] != null ? (String) r[4] : "—";
        int round = (int) r[5];

        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("result-card"); row.setPadding(new Insets(10, 14, 10, 14));
        Label fighters = new Label(f1 + "  vs  " + f2);
        fighters.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:13px;");
        HBox.setHgrow(fighters, Priority.ALWAYS);
        Label wb = new Label(winner); wb.getStyleClass().add("badge-gold");
        Label ml = new Label(method + (round > 0 ? "  Rd " + round : ""));
        ml.getStyleClass().add("text-muted"); ml.setStyle("-fx-font-size:11px;");
        row.getChildren().addAll(fighters, wb, ml);
        return row;
    }

    private VBox buildBlogCard(BlogArticle a) {
        VBox card = new VBox(0); card.setMinWidth(160); card.setMaxWidth(200);
        card.getStyleClass().add("card-dark"); card.setPadding(new Insets(0));

        StackPane img = new StackPane(); img.setMinHeight(90); img.setMaxHeight(90);
        String imgUri = resolveBlogImage(a.getImagePath());
        if (imgUri != null) {
            img.setStyle("-fx-background-radius:8 8 0 0;-fx-background-image:url('" + imgUri + "');" +
                         "-fx-background-size:cover;-fx-background-position:center;");
        } else {
            img.setStyle("-fx-background-radius:8 8 0 0;-fx-background-color:#27272a;");
            Label ph = new Label("NO IMAGE"); ph.setStyle("-fx-font-size:9px;-fx-text-fill:#52525b;-fx-font-weight:bold;");
            img.getChildren().add(ph);
        }
        VBox text = new VBox(6); text.setPadding(new Insets(10));
        Label title = new Label(a.getTitle() != null ? a.getTitle() : "Untitled");
        title.setStyle("-fx-font-weight:bold;-fx-text-fill:#ffffff;-fx-font-size:12px;");
        title.setWrapText(true); title.setMaxHeight(36);
        Label views = new Label(a.getViewCount() + " views"); views.getStyleClass().add("caption");
        text.getChildren().addAll(title, views);
        card.getChildren().addAll(img, text);
        return card;
    }

    // ── Vote ──────────────────────────────────────────────────────────────────

    private void onVote(int proposalId, Button btn, Label badge) {
        if (Session.getUser() == null) return;
        int userId = Session.getUser().getUserId();
        btn.setDisable(true); btn.setText("...");

        Task<Boolean> t = new Task<>() {
            @Override protected Boolean call() throws Exception {
                try (Connection c = DBConnection.getDataSource().getConnection()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM fan_vote WHERE user_id=? AND match_proposal_id=?")) {
                        ps.setInt(1, userId); ps.setInt(2, proposalId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) return true;
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO fan_vote(user_id,match_proposal_id,voted_at) VALUES(?,?,NOW())")) {
                        ps.setInt(1, userId); ps.setInt(2, proposalId); ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE match_proposal SET vote_count=vote_count+1 WHERE id=?")) {
                        ps.setInt(1, proposalId); ps.executeUpdate();
                    }
                    return false;
                }
            }
        };
        t.setOnSucceeded(e -> {
            if (t.getValue()) { btn.setText("Already Voted"); }
            else { reloadVoting(); }
        });
        t.setOnFailed(e -> { btn.setDisable(false); btn.setText("VOTE"); });
        new Thread(t, "fan-vote").start();
    }

    private void reloadVoting() {
        Task<List<Object[]>> t = new Task<>() {
            @Override protected List<Object[]> call() throws Exception {
                return loadProposals(DBConnection.getDataSource());
            }
        };
        t.setOnSucceeded(e -> { proposals = t.getValue(); populateVoting(); });
        new Thread(t, "fan-proposals-reload").start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolvePhoto(String filename) {
        if (filename == null || filename.isBlank()) return null;
        try {
            File f = new File(AppConfig.get().uploadDir + "/boxers/" + filename);
            return f.exists() ? f.toURI().toString() : null;
        } catch (Exception e) { return null; }
    }

    private String resolveBlogImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        try {
            File f = new File(imagePath);
            if (f.isAbsolute() && f.exists()) return f.toURI().toString();
            File rel = new File(AppConfig.get().uploadDir + "/" + imagePath);
            return rel.exists() ? rel.toURI().toString() : null;
        } catch (Exception e) { return null; }
    }

    private String initials(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isBlank()) sb.append(Character.toUpperCase(first.charAt(0)));
        if (last  != null && !last.isBlank())  sb.append(Character.toUpperCase(last.charAt(0)));
        return sb.length() > 0 ? sb.toString() : "?";
    }

    private Label muted(String text) {
        Label l = new Label(text); l.getStyleClass().add("text-muted"); return l;
    }
}
