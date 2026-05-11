package tn.smartfight.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.config.AppConfig;
import tn.smartfight.config.DBConnection;
import tn.smartfight.dao.BlogDao;
import tn.smartfight.model.BlogArticle;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlogController {
    private static final Logger LOG = Logger.getLogger(BlogController.class.getName());

    @FXML private TextField  searchField;
    @FXML private VBox       articlesContainer;
    @FXML private VBox       categoryList;
    @FXML private Label      statusLabel;
    @FXML private StackPane  articleDetail;
    @FXML private StackPane  detailImagePane;
    @FXML private Label      detailImagePlaceholder;
    @FXML private Label      detailTitle;
    @FXML private Label      detailCategory;
    @FXML private Label      detailMeta;
    @FXML private Label      detailViews;
    @FXML private Label      detailSummary;
    @FXML private Label      detailBody;

    private final BlogDao dao = new BlogDao();
    private List<BlogArticle> allArticles = new ArrayList<>();
    private Map<Integer, String> categories;
    private Integer activeCategoryId = null;

    @FXML
    public void initialize() {
        statusLabel.setText("Loading…");
        Task<Object[]> task = new Task<>() {
            @Override protected Object[] call() {
                List<BlogArticle> arts = dao.findAll();
                Map<Integer, String> cats = dao.loadCategories();
                return new Object[]{ arts, cats };
            }
        };
        task.setOnSucceeded(e -> {
            Object[] res = task.getValue();
            allArticles = (List<BlogArticle>) res[0];
            categories  = (Map<Integer, String>) res[1];
            buildCategoryPanel();
            renderGrid(allArticles);
        });
        task.setOnFailed(e -> statusLabel.setText("Load failed."));
        new Thread(task, "blog-load").start();
    }

    @FXML private void onSearch() { renderGrid(filtered()); }

    @FXML private void onCloseDetail() {
        articleDetail.setVisible(false);
        articleDetail.setManaged(false);
    }

    /* ── Category sidebar ────────────────────────────────────── */

    private void buildCategoryPanel() {
        categoryList.getChildren().clear();

        Button allBtn = categoryChip("All", null);
        allBtn.getStyleClass().add("nav-btn-active");
        categoryList.getChildren().add(allBtn);

        if (categories != null) {
            categories.forEach((id, name) ->
                    categoryList.getChildren().add(categoryChip(name, id)));
        }
    }

    private Button categoryChip(String name, Integer catId) {
        Button btn = new Button(name);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:#18181b;-fx-text-fill:#d4d4d8;-fx-font-size:13px;" +
                "-fx-border-color:#27272a;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;" +
                "-fx-padding:8 12;-fx-alignment:CENTER_LEFT;");
        btn.setOnAction(e -> {
            categoryList.getChildren().forEach(n -> {
                if (n instanceof Button b) {
                    b.getStyleClass().remove("nav-btn-active");
                    b.setStyle("-fx-background-color:#18181b;-fx-text-fill:#d4d4d8;-fx-font-size:13px;" +
                            "-fx-border-color:#27272a;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;" +
                            "-fx-padding:8 12;-fx-alignment:CENTER_LEFT;");
                }
            });
            btn.setStyle("-fx-background-color:rgba(220,38,38,0.15);-fx-text-fill:#dc2626;-fx-font-size:13px;" +
                    "-fx-border-color:#dc2626;-fx-border-radius:6;-fx-background-radius:6;-fx-cursor:hand;" +
                    "-fx-padding:8 12;-fx-alignment:CENTER_LEFT;");
            activeCategoryId = catId;
            renderGrid(filtered());
        });
        return btn;
    }

    /* ── Article grid ────────────────────────────────────────── */

    private void renderGrid(List<BlogArticle> articles) {
        articlesContainer.getChildren().clear();

        if (articles.isEmpty()) {
            Label empty = new Label("No articles found.");
            empty.setStyle("-fx-text-fill:#52525b;-fx-font-size:14px;-fx-padding:32;");
            articlesContainer.getChildren().add(empty);
            statusLabel.setText("0 articles");
            return;
        }

        // 2-column TilePane
        javafx.scene.layout.TilePane tile = new javafx.scene.layout.TilePane();
        tile.setPrefColumns(2);
        tile.setHgap(16);
        tile.setVgap(16);
        tile.setPadding(new Insets(20));
        tile.setPrefTileWidth(300);

        for (BlogArticle a : articles) {
            tile.getChildren().add(buildArticleCard(a));
        }
        articlesContainer.getChildren().add(tile);
        statusLabel.setText(articles.size() + " articles");
    }

    private VBox buildArticleCard(BlogArticle a) {
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;");
        card.setOnMouseClicked(e -> showDetail(a));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#1f1f22;-fx-border-color:#3f3f46;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;" +
                "-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;"));

        // Cover image area
        StackPane coverArea = new StackPane();
        coverArea.setMinHeight(160);
        coverArea.setMaxHeight(160);
        String imgUri = resolveImage(a.getImagePath());
        if (imgUri != null) {
            coverArea.setStyle(
                "-fx-background-image:url('" + imgUri + "');" +
                "-fx-background-size:cover;" +
                "-fx-background-position:center center;" +
                "-fx-background-radius:10 10 0 0;"
            );
        } else {
            coverArea.setStyle("-fx-background-color:#111111;-fx-background-radius:10 10 0 0;");
            Label icon = new Label("📰");
            icon.setStyle("-fx-font-size:40px;-fx-text-fill:#374151;");
            coverArea.getChildren().add(icon);
        }

        // Category badge overlay
        String catName = (categories != null && a.getCategoryId() > 0)
                ? categories.get(a.getCategoryId()) : null;
        if (catName != null) {
            Label catBadge = new Label(catName.toUpperCase());
            catBadge.setStyle("-fx-background-color:#dc2626;-fx-text-fill:#ffffff;" +
                    "-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:3 8;-fx-background-radius:4;");
            StackPane.setAlignment(catBadge, Pos.TOP_LEFT);
            StackPane.setMargin(catBadge, new Insets(8));
            coverArea.getChildren().add(catBadge);
        }

        // Info area
        VBox info = new VBox(8);
        info.setPadding(new Insets(14));

        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#fafafa;");
        title.setWrapText(true);

        String summary = a.getSummary() != null ? a.getSummary() : "";
        if (summary.length() > 100) summary = summary.substring(0, 100) + "…";
        Label sumLbl = new Label(summary);
        sumLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#a1a1aa;");
        sumLbl.setWrapText(true);

        String dateStr = a.getCreatedAt() != null
                ? a.getCreatedAt().toLocalDate().toString() : "";
        Label meta = new Label(dateStr + "  ·  " + a.getViewCount() + " views");
        meta.setStyle("-fx-font-size:10px;-fx-text-fill:#52525b;");

        info.getChildren().addAll(title, sumLbl, meta);
        card.getChildren().addAll(coverArea, info);
        return card;
    }

    /* ── Detail overlay ──────────────────────────────────────── */

    private void showDetail(BlogArticle a) {
        detailTitle.setText(a.getTitle());

        String catName = (categories != null && a.getCategoryId() > 0)
                ? categories.get(a.getCategoryId()) : "General";
        detailCategory.setText(catName != null ? catName.toUpperCase() : "GENERAL");

        String dateStr = a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "";
        detailMeta.setText(dateStr);
        detailViews.setText(a.getViewCount() + " views");

        detailSummary.setText(a.getSummary() != null ? a.getSummary() : "");
        detailBody.setText(a.getContent() != null ? a.getContent() : "");

        // Cover image
        String imgUri = resolveImage(a.getImagePath());
        detailImagePane.getChildren().clear();
        if (imgUri != null) {
            detailImagePane.setStyle(
                "-fx-background-image:url('" + imgUri + "');" +
                "-fx-background-size:cover;" +
                "-fx-background-position:center center;"
            );
        } else {
            detailImagePane.setStyle("-fx-background-color:#111111;");
            Label icon = new Label("📰");
            icon.setStyle("-fx-font-size:48px;-fx-text-fill:#374151;");
            detailImagePane.getChildren().add(icon);
        }

        articleDetail.setVisible(true);
        articleDetail.setManaged(true);

        // Increment view count in background
        int id = a.getId();
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                DataSource ds = DBConnection.getDataSource();
                try (Connection conn = ds.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE blog_article SET view_count = view_count + 1 WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "view_count increment failed", e);
                }
                return null;
            }
        };
        new Thread(task, "blog-view").start();
    }

    /* ── Helpers ─────────────────────────────────────────────── */

    private List<BlogArticle> filtered() {
        String search = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        List<BlogArticle> result = new ArrayList<>();
        for (BlogArticle a : allArticles) {
            if (activeCategoryId != null && a.getCategoryId() != activeCategoryId) continue;
            if (!search.isEmpty()) {
                String hay = (a.getTitle() + " " + (a.getSummary() != null ? a.getSummary() : "")).toLowerCase();
                if (!hay.contains(search)) continue;
            }
            result.add(a);
        }
        return result;
    }

    private String resolveImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        String uploadDir = AppConfig.get().uploadDir;
        // Try absolute path first, then under blog/images/
        File f = new File(imagePath);
        if (!f.exists()) f = new File(uploadDir + "/blog/images/" + imagePath);
        if (!f.exists()) f = new File(uploadDir + "/" + imagePath);
        return f.exists() ? f.toURI().toString() : null;
    }
}
