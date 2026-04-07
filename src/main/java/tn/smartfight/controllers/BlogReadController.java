package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.shape.Rectangle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;
import tn.smartfight.services.MediaCache;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BlogReadController {

    @FXML private ComboBox<Object> cbCategory;
    @FXML private TextField txtSearch;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox newsGrid;

    private final BlogService blogService = new BlogService();
    private final javafx.scene.control.ContextMenu suggestionPopup = new javafx.scene.control.ContextMenu();
    { suggestionPopup.getStyleClass().add("search-suggestions"); }

    private int lastBreakpoint = -1;
    private javafx.animation.Timeline resizeDebounce;

    @FXML
    private void initialize() {
        cbCategory.getItems().add("All Categories");
        List<BlogCategory> categories = blogService.getAllCategories();
        cbCategory.getItems().addAll(categories);
        cbCategory.getSelectionModel().selectFirst();

        scrollPane.setFitToWidth(true);

        // Autocomplete: show matching article titles as user types
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> updateSuggestions(newVal.trim()));
        // Hide popup when focus leaves the field
        txtSearch.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) suggestionPopup.hide();
        });

        newsGrid.widthProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number old, Number newVal) {
                if (newVal.doubleValue() > 50) {
                    newsGrid.widthProperty().removeListener(this);
                    loadNews();
                    // Listen to scrollPane width (not newsGrid) to avoid scrollbar-appear/disappear
                    // feedback loops: when content height changes, the scrollbar toggles, which
                    // shrinks newsGrid width and can flip the breakpoint, causing infinite reloads.
                    // The scrollPane's own width only changes on window resize, not on content change.
                    scrollPane.widthProperty().addListener((o, ov, nv) -> scheduleReload(nv.doubleValue()));
                }
            }
        });
    }

    @FXML
    private void onCategoryChanged() { loadNews(); }

    @FXML
    private void onSearch() { loadNews(); }

    private void scheduleReload(double newWidth) {
        int bp = newWidth >= 1100 ? 2 : newWidth >= 700 ? 1 : 0;
        if (bp == lastBreakpoint) return;
        if (resizeDebounce != null) resizeDebounce.stop();
        resizeDebounce = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(150), e -> loadNews()));
        resizeDebounce.play();
    }

    private void loadNews() {
        double w = newsGrid.getWidth();
        if (w < 50) return;
        // Track lastBreakpoint from scrollPane width (consistent with the resize listener source)
        // so breakpoint comparisons in scheduleReload never produce a mismatch.
        double scrollW = scrollPane.getWidth();
        lastBreakpoint = scrollW >= 1100 ? 2 : scrollW >= 700 ? 1 : 0;

        List<BlogArticle> articles = fetchArticles();
        newsGrid.getChildren().clear();

        if (articles.isEmpty()) {
            Label empty = new Label("NO STORIES AVAILABLE");
            empty.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #52525b; -fx-padding: 60;");
            newsGrid.getChildren().add(empty);
            return;
        }

        VBox container = new VBox(20);
        container.setStyle("-fx-padding: 0;");
        container.setMaxWidth(Double.MAX_VALUE);

        if (w >= 1100) {
            buildLayoutWide(articles, container, w);
        } else if (w >= 700) {
            buildLayoutMedium(articles, container, w);
        } else {
            buildLayoutNarrow(articles, container);
        }

        newsGrid.getChildren().add(container);
    }

    // >= 1100px: Featured + 2 side | Video + 2 side | 3-col grid
    private void buildLayoutWide(List<BlogArticle> articles, VBox container, double w) {
        double sideW = Math.min(340, w * 0.30);
        int idx = 0;

        if (idx < articles.size()) {
            int featuredH = (int)(w * 0.27);
            HBox row = new HBox(20);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setMinHeight(featuredH);
            row.setMaxHeight(featuredH);
            StackPane featured = buildFeaturedCard(articles.get(idx++), featuredH);
            HBox.setHgrow(featured, Priority.ALWAYS);
            row.getChildren().add(featured);

            VBox side = new VBox(16);
            side.setMinWidth(sideW); side.setMaxWidth(sideW);
            for (int i = 0; i < 2 && idx < articles.size(); i++)
                side.getChildren().add(buildSmallCard(articles.get(idx++)));
            row.getChildren().add(side);
            container.getChildren().add(row);
        }

        if (idx < articles.size()) {
            int videoH = (int)(w * 0.24);
            HBox row = new HBox(20);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setMinHeight(videoH);
            row.setMaxHeight(videoH);
            StackPane videoCard = buildVideoCard(articles.get(idx++), videoH);
            HBox.setHgrow(videoCard, Priority.ALWAYS);
            row.getChildren().add(videoCard);

            VBox side = new VBox(16);
            side.setMinWidth(sideW); side.setMaxWidth(sideW);
            for (int i = 0; i < 2 && idx < articles.size(); i++)
                side.getChildren().add(buildSmallCard(articles.get(idx++)));
            row.getChildren().add(side);
            container.getChildren().add(row);
        }

        // 3-col remaining
        HBox currentRow = null;
        int col = 0;
        while (idx < articles.size()) {
            if (col % 3 == 0) {
                currentRow = new HBox(20);
                currentRow.setMaxWidth(Double.MAX_VALUE);
                container.getChildren().add(currentRow);
            }
            VBox card = buildSmallCard(articles.get(idx++));
            HBox.setHgrow(card, Priority.ALWAYS);
            currentRow.getChildren().add(card);
            col++;
        }
    }

    // 700–1099px: Featured full-width | 2-col grid
    private void buildLayoutMedium(List<BlogArticle> articles, VBox container, double w) {
        int idx = 0;

        if (idx < articles.size()) {
            int featuredH = (int)(w * 0.30);
            StackPane featured = buildFeaturedCard(articles.get(idx++), featuredH);
            featured.setMaxWidth(Double.MAX_VALUE);
            container.getChildren().add(featured);
        }

        if (idx < articles.size()) {
            int videoH = (int)(w * 0.25);
            StackPane videoCard = buildVideoCard(articles.get(idx++), videoH);
            videoCard.setMaxWidth(Double.MAX_VALUE);
            container.getChildren().add(videoCard);
        }

        // 2-col remaining
        HBox currentRow = null;
        int col = 0;
        while (idx < articles.size()) {
            if (col % 2 == 0) {
                currentRow = new HBox(20);
                currentRow.setMaxWidth(Double.MAX_VALUE);
                container.getChildren().add(currentRow);
            }
            VBox card = buildSmallCard(articles.get(idx++));
            HBox.setHgrow(card, Priority.ALWAYS);
            currentRow.getChildren().add(card);
            col++;
        }
    }

    // < 700px: Everything stacked 1-col
    private void buildLayoutNarrow(List<BlogArticle> articles, VBox container) {
        for (BlogArticle a : articles) {
            VBox card = buildSmallCard(a);
            card.setMaxWidth(Double.MAX_VALUE);
            container.getChildren().add(card);
        }
    }

    // ---------------------------------------------------------------
    // Featured card (large, image overlay)
    // ---------------------------------------------------------------
    private StackPane buildFeaturedCard(BlogArticle article, int height) {
        StackPane card = new StackPane();
        card.setMinHeight(height);
        card.setMaxHeight(height);
        card.setStyle(
            "-fx-border-color: #27272a; -fx-border-width: 1; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #18181b;"
        );
        card.setCursor(javafx.scene.Cursor.HAND);

        addCoverImage(card, article, height);
        addGradientOverlay(card);

        VBox content = new VBox(8);
        content.setAlignment(Pos.BOTTOM_LEFT);
        content.setStyle("-fx-padding: 28;");

        HBox badges = buildBadges(article);

        Label title = new Label(article.getTitle() != null ? article.getTitle().toUpperCase() : "");
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: #fafafa; -fx-font-size: 26px; -fx-font-weight: 900; -fx-font-style: italic;");

        Label summary = new Label(article.getSummary() != null ? article.getSummary() : "");
        summary.setWrapText(false);
        summary.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");

        Label readMore = new Label("READ THE STORY \u2192");
        readMore.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold;");

        content.getChildren().addAll(badges, title, summary, readMore);
        card.getChildren().add(content);

        final String styleNormal =
            "-fx-border-color: #27272a; -fx-border-width: 1; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #18181b;";
        final String stylePressed =
            "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #18181b;" +
            " -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.6), 15, 0.4, 0, 0);";

        card.setOnMousePressed(e -> card.setStyle(stylePressed));
        card.setOnMouseReleased(e -> card.setStyle(styleNormal));
        card.setOnMouseClicked(e -> openArticleDetail(article));
        return card;
    }

    // ---------------------------------------------------------------
    // Video card (large, dominant play button, VIDEO badge, no-image fallback)
    // ---------------------------------------------------------------
    private StackPane buildVideoCard(BlogArticle article, int height) {
        StackPane card = new StackPane();
        card.setMinHeight(height);
        card.setMaxHeight(height);
        card.setStyle(
            "-fx-border-color: #27272a; -fx-border-width: 1; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #09090b;"
        );
        card.setCursor(javafx.scene.Cursor.HAND);

        // Cover image (only when present)
        addCoverImage(card, article, height);

        // When there is no image the card is a plain dark surface; add a subtle
        // cinematic letterbox pattern so it doesn't look broken.
        if (!article.hasImage()) {
            Region topBar = new Region();
            topBar.setMinHeight(48);
            topBar.setMaxHeight(48);
            topBar.setStyle("-fx-background-color: #000000;");
            StackPane.setAlignment(topBar, Pos.TOP_CENTER);
            card.getChildren().add(topBar);

            Region bottomBar = new Region();
            bottomBar.setMinHeight(48);
            bottomBar.setMaxHeight(48);
            bottomBar.setStyle("-fx-background-color: #000000;");
            StackPane.setAlignment(bottomBar, Pos.BOTTOM_CENTER);
            card.getChildren().add(bottomBar);
        }

        // Full gradient overlay — deeper when there is no image so text stays readable
        Region gradient = new Region();
        if (article.hasImage()) {
            gradient.setStyle(
                "-fx-background-color: linear-gradient(to top, #000000f8 0%, #000000aa 40%, #00000044 75%, transparent 100%);" +
                " -fx-background-radius: 14;"
            );
        } else {
            gradient.setStyle(
                "-fx-background-color: linear-gradient(to top, #000000ff 0%, #000000cc 60%, #000000aa 100%);" +
                " -fx-background-radius: 14;"
            );
        }
        card.getChildren().add(gradient);

        // ---- Play button cluster (always centered) ----
        if (article.hasVideo()) {
            // Outer glow ring
            StackPane glowRing = new StackPane();
            glowRing.setMaxSize(92, 92);
            glowRing.setMinSize(92, 92);
            glowRing.setStyle(
                "-fx-background-color: rgba(220,38,38,0.18);" +
                " -fx-background-radius: 46;" +
                " -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.55), 28, 0.3, 0, 0);"
            );

            // Middle ring
            StackPane midRing = new StackPane();
            midRing.setMaxSize(72, 72);
            midRing.setMinSize(72, 72);
            midRing.setStyle(
                "-fx-background-color: rgba(220,38,38,0.35);" +
                " -fx-background-radius: 36;"
            );

            // Inner solid play button
            StackPane playBtn = new StackPane();
            playBtn.setMaxSize(54, 54);
            playBtn.setMinSize(54, 54);
            playBtn.setStyle(
                "-fx-background-color: #dc2626;" +
                " -fx-background-radius: 27;" +
                " -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.8), 16, 0.5, 0, 0);"
            );
            Label playIcon = new Label("\u25B6");
            playIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-padding: 0 0 0 4;");
            playBtn.getChildren().add(playIcon);

            // Stack all rings together
            StackPane playCluster = new StackPane(glowRing, midRing, playBtn);
            StackPane.setAlignment(playCluster, Pos.CENTER);
            card.getChildren().add(playCluster);
        }

        // ---- Bottom info bar ----
        VBox info = new VBox(6);
        info.setAlignment(Pos.BOTTOM_LEFT);
        info.setStyle("-fx-padding: 24;");

        HBox badges = buildVideoBadges(article);

        Label title = new Label(article.getTitle() != null ? article.getTitle().toUpperCase() : "");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-font-style: italic; -fx-text-fill: #fafafa;");

        // "Click to watch" call-to-action only for video articles
        if (article.hasVideo()) {
            Label watchCta = new Label("\u25B6  CLICK TO WATCH");
            watchCta.setStyle(
                "-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;" +
                " -fx-padding: 5 12; -fx-background-color: rgba(0,0,0,0.55);" +
                " -fx-background-radius: 4;"
            );
            info.getChildren().addAll(badges, title, watchCta);
        } else {
            info.getChildren().addAll(badges, title);
        }

        card.getChildren().add(info);

        final String styleNormal =
            "-fx-border-color: #27272a; -fx-border-width: 1; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #09090b;";
        final String stylePressed =
            "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 14;" +
            " -fx-background-radius: 14; -fx-background-color: #09090b;" +
            " -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.6), 15, 0.4, 0, 0);";

        card.setOnMousePressed(e -> card.setStyle(stylePressed));
        card.setOnMouseReleased(e -> card.setStyle(styleNormal));
        card.setOnMouseClicked(e -> {
            if (article.hasVideo()) { playVideo(article); } else { openArticleDetail(article); }
        });

        return card;
    }

    // ---------------------------------------------------------------
    // Small card (thumbnail + text) — video-aware layout
    // ---------------------------------------------------------------
    private VBox buildSmallCard(BlogArticle article) {
        final boolean isVideo = article.hasVideo();

        final String styleNormal =
            "-fx-border-color: #3f3f46; -fx-border-width: 1; -fx-border-radius: 10;" +
            " -fx-background-radius: 10; -fx-background-color: #18181b;";
        final String styleHover =
            "-fx-border-color: #dc2626; -fx-border-width: 1; -fx-border-radius: 10;" +
            " -fx-background-radius: 10; -fx-background-color: #18181b;";
        final String stylePressed =
            "-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 10;" +
            " -fx-background-radius: 10; -fx-background-color: #1a1a20;" +
            " -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.5), 12, 0.3, 0, 0);";

        // Use min-height only so content can expand if needed (no max-height cap)
        VBox card = new VBox(0);
        card.setMinHeight(140);
        card.setStyle(styleNormal);
        card.setCursor(javafx.scene.Cursor.HAND);

        HBox row = new HBox(12);
        row.setStyle("-fx-padding: 12;");
        row.setAlignment(Pos.CENTER_LEFT);

        // ---- LEFT: thumbnail ----
        // Video cards get a wider thumbnail to give the play button more room
        int thumbW = isVideo ? 140 : 110;
        int thumbH = 90;

        StackPane thumbPane = new StackPane();
        thumbPane.setMinSize(thumbW, thumbH);
        thumbPane.setMaxSize(thumbW, thumbH);
        thumbPane.setStyle("-fx-background-color: #09090b; -fx-background-radius: 8;");

        if (article.hasImage()) {
            try {
                InputStream thumbStream = MediaCache.getImageStream(article);
                if (thumbStream == null) throw new Exception("no image");
                ImageView thumb = new ImageView(new Image(thumbStream, thumbW, thumbH, true, true));
                thumb.setFitWidth(thumbW);
                thumb.setFitHeight(thumbH);
                thumb.setPreserveRatio(true);

                // Clip image to rounded rectangle
                Rectangle thumbClip = new Rectangle(thumbW, thumbH);
                thumbClip.setArcWidth(16);
                thumbClip.setArcHeight(16);
                thumb.setClip(thumbClip);

                thumbPane.getChildren().add(thumb);
            } catch (Exception e) {
                System.err.println("[BlogRead] thumb: " + e.getMessage());
            }
        } else {
            Label placeholder = new Label("FS");
            placeholder.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3f3f46;");
            thumbPane.getChildren().add(placeholder);
        }

        // Video indicator — centered play button for video cards, corner badge for text cards
        if (isVideo) {
            // Dim overlay so the play button pops
            Region dimOverlay = new Region();
            dimOverlay.setMinSize(thumbW, thumbH);
            dimOverlay.setMaxSize(thumbW, thumbH);
            dimOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.42); -fx-background-radius: 8;");
            thumbPane.getChildren().add(dimOverlay);

            // Centered play button
            StackPane miniPlay = new StackPane();
            miniPlay.setMaxSize(38, 38);
            miniPlay.setMinSize(38, 38);
            miniPlay.setStyle(
                "-fx-background-color: #dc2626;" +
                " -fx-background-radius: 19;" +
                " -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.7), 10, 0.4, 0, 0);"
            );
            Label icon = new Label("\u25B6");
            icon.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-padding: 0 0 0 3;");
            miniPlay.getChildren().add(icon);
            StackPane.setAlignment(miniPlay, Pos.CENTER);
            thumbPane.getChildren().add(miniPlay);
        }

        // ---- RIGHT: text column ----
        VBox textBox = new VBox(6);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        textBox.setStyle("-fx-padding: 2 0 0 0;");

        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label catLabel = new Label(article.getCategoryName() != null ? article.getCategoryName().toUpperCase() : "NEWS");
        catLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label sep = new Label("\u00B7");
        sep.setStyle("-fx-text-fill: #52525b; -fx-font-size: 10px;");
        Label timeLabel = new Label(timeAgo(article.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px; -fx-font-weight: bold;");
        meta.getChildren().addAll(catLabel, sep, timeLabel);

        Label title = new Label(article.getTitle() != null ? article.getTitle().toUpperCase() : "");
        title.setWrapText(true);
        title.setMaxHeight(54);
        title.setStyle("-fx-text-fill: #fafafa; -fx-font-size: 13px; -fx-font-weight: 900; -fx-font-style: italic;");

        textBox.getChildren().addAll(meta, title);

        // "Watch" call-to-action label for video small cards
        if (isVideo) {
            Label watchLabel = new Label("\u25B6  WATCH VIDEO");
            watchLabel.setStyle(
                "-fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-font-weight: bold;" +
                " -fx-padding: 4 10; -fx-background-color: rgba(220,38,38,0.12);" +
                " -fx-background-radius: 4; -fx-border-color: rgba(220,38,38,0.3);" +
                " -fx-border-width: 1; -fx-border-radius: 4;"
            );
            textBox.getChildren().add(watchLabel);
        }

        row.getChildren().addAll(thumbPane, textBox);
        card.getChildren().add(row);

        card.setOnMouseClicked(e -> {
            if (isVideo) { playVideo(article); } else { openArticleDetail(article); }
        });
        card.setOnMousePressed(e -> card.setStyle(stylePressed));
        card.setOnMouseReleased(e -> card.setStyle(styleNormal));
        card.setOnMouseEntered(e -> card.setStyle(styleHover));
        card.setOnMouseExited(e -> card.setStyle(styleNormal));

        return card;
    }

    // ---------------------------------------------------------------
    // Shared helpers for card building
    // ---------------------------------------------------------------
    private void addCoverImage(StackPane card, BlogArticle article, int height) {
        if (!article.hasImage()) return;
        try {
            StackPane imgPane = new StackPane();
            // height is now always the card's actual min=max height (passed from the layout
            // method), so imgPane.minHeight=height never conflicts with the card's maxHeight.
            imgPane.setMinHeight(height);
            imgPane.setMaxHeight(height);
            imgPane.setMaxWidth(Double.MAX_VALUE);

            InputStream imgStream = MediaCache.getImageStream(article);
            if (imgStream == null) return;
            ImageView iv = new ImageView(new Image(imgStream, 0, height, true, true));
            iv.setPreserveRatio(true);
            iv.setFitHeight(height);
            StackPane.setAlignment(iv, Pos.CENTER);
            imgPane.getChildren().add(iv);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(card.widthProperty());
            clip.setHeight(height);
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            imgPane.setClip(clip);

            card.getChildren().add(imgPane);
        } catch (Exception e) {
            System.err.println("[BlogRead] coverImage: " + e.getMessage());
        }
    }

    private void addGradientOverlay(StackPane card) {
        Region gradient = new Region();
        gradient.setStyle(
            "-fx-background-color: linear-gradient(to top, #000000f0 0%, #00000088 50%, transparent 100%);" +
            " -fx-background-radius: 14;"
        );
        card.getChildren().add(gradient);
    }

    private HBox buildBadges(BlogArticle article) {
        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label catBadge = new Label(article.getCategoryName() != null ? article.getCategoryName().toUpperCase() : "NEWS");
        catBadge.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white;" +
            " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 4;"
        );
        Label timeLbl = new Label(timeAgo(article.getCreatedAt()));
        timeLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");
        badges.getChildren().addAll(catBadge, timeLbl);
        return badges;
    }

    // Badges row for video card — includes a "VIDEO" pill badge
    private HBox buildVideoBadges(BlogArticle article) {
        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(article.getCategoryName() != null ? article.getCategoryName().toUpperCase() : "NEWS");
        catBadge.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white;" +
            " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 4;"
        );

        if (article.hasVideo()) {
            Label videoBadge = new Label("\u25B6  VIDEO");
            videoBadge.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ef4444;" +
                " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10;" +
                " -fx-border-color: #ef4444; -fx-border-width: 1; -fx-border-radius: 4;" +
                " -fx-background-radius: 4;"
            );
            badges.getChildren().add(videoBadge);
        }

        badges.getChildren().add(catBadge);

        Label timeLbl = new Label(timeAgo(article.getCreatedAt()));
        timeLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");
        badges.getChildren().add(timeLbl);

        return badges;
    }

    private void applyCss(javafx.scene.Scene scene) {
        URL css = getClass().getResource("/tn/smartfight/styles/smartfight.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    // ---------------------------------------------------------------
    // Open article detail popup
    // ---------------------------------------------------------------
    private void openArticleDetail(BlogArticle article) {
        blogService.incrementViewCount(article.getId());

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle(article.getTitle());
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        VBox root = new VBox(0);
        root.setPrefSize(740, 640);
        root.setStyle(
            "-fx-background-color: #0a0a0a;" +
            " -fx-border-color: #27272a;" +
            " -fx-border-width: 1;" +
            " -fx-border-radius: 16;" +
            " -fx-background-radius: 16;" +
            " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 30, 0.5, 0, 10);"
        );

        HBox topBar = new HBox();
        topBar.setMinHeight(48);
        topBar.setMaxHeight(48);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
            "-fx-background-color: #18181b;" +
            " -fx-border-color: transparent transparent #27272a transparent;" +
            " -fx-border-width: 0 0 1 0;" +
            " -fx-padding: 0 16;" +
            " -fx-background-radius: 16 16 0 0;"
        );

        Label topLabel = new Label("ARTICLE");
        topLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region barSpacer = new Region();
        HBox.setHgrow(barSpacer, Priority.ALWAYS);

        Button closeX = new Button("\u2715");
        closeX.setMinSize(28, 28);
        closeX.setMaxSize(28, 28);
        closeX.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 13px;" +
            " -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0; -fx-cursor: hand;"
        );
        closeX.setOnAction(e -> stage.close());
        topBar.getChildren().addAll(topLabel, barSpacer, closeX);

        topBar.setOnMousePressed(ev -> { xOffset[0] = ev.getSceneX(); yOffset[0] = ev.getSceneY(); });
        topBar.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX() - xOffset[0]); stage.setY(ev.getScreenY() - yOffset[0]); });

        VBox bodyContent = new VBox(16);
        bodyContent.setStyle("-fx-padding: 20 24;");

        if (article.hasImage()) {
            try {
                StackPane headerPane = new StackPane();
                headerPane.setMinHeight(280);
                headerPane.setMaxHeight(280);
                headerPane.setStyle("-fx-background-color: #18181b; -fx-background-radius: 12;");

                InputStream headerStream = MediaCache.getImageStream(article);
                if (headerStream == null) throw new Exception("no image");
                ImageView headerImg = new ImageView(new Image(headerStream, 0, 280, true, true));
                headerImg.setPreserveRatio(true);
                headerImg.setFitHeight(280);
                StackPane.setAlignment(headerImg, Pos.CENTER);
                headerPane.getChildren().add(headerImg);

                Rectangle headerClip = new Rectangle();
                headerClip.widthProperty().bind(headerPane.widthProperty());
                headerClip.setHeight(280);
                headerClip.setArcWidth(24);
                headerClip.setArcHeight(24);
                headerPane.setClip(headerClip);

                Region imgGradient = new Region();
                imgGradient.setStyle(
                    "-fx-background-color: linear-gradient(to top, #0a0a0a 0%, transparent 60%);" +
                    " -fx-background-radius: 12;"
                );
                headerPane.getChildren().add(imgGradient);
                bodyContent.getChildren().add(headerPane);
            } catch (Exception e) {
                System.err.println("[BlogRead] detailImage: " + e.getMessage());
            }
        }

        VBox metaCard = new VBox(14);
        metaCard.setStyle(
            "-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1;" +
            " -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;"
        );

        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label catBadge = new Label(article.getCategoryName() != null ? article.getCategoryName().toUpperCase() : "NEWS");
        catBadge.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white;" +
            " -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 4;"
        );
        Label authorLbl = new Label(article.getAuthorName() != null ? article.getAuthorName() : "");
        authorLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label dotSep1 = new Label("\u00B7");
        dotSep1.setStyle("-fx-text-fill: #52525b; -fx-font-size: 11px;");
        Label timeLbl = new Label(timeAgo(article.getCreatedAt()));
        timeLbl.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label dotSep2 = new Label("\u00B7");
        dotSep2.setStyle("-fx-text-fill: #52525b; -fx-font-size: 11px;");
        Label viewsLbl = new Label(article.getViewCount() + " views");
        viewsLbl.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        badges.getChildren().addAll(catBadge, authorLbl, dotSep1, timeLbl, dotSep2, viewsLbl);

        Region separator = new Region();
        separator.setMinHeight(1);
        separator.setMaxHeight(1);
        separator.setStyle("-fx-background-color: #27272a;");

        Label title = new Label(article.getTitle() != null ? article.getTitle().toUpperCase() : "");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-font-style: italic; -fx-text-fill: #fafafa;");

        metaCard.getChildren().addAll(badges, separator, title);
        bodyContent.getChildren().add(metaCard);

        VBox contentCard = new VBox(0);
        contentCard.setStyle(
            "-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1;" +
            " -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;"
        );

        Label contentLabel = new Label(article.getContent() != null ? article.getContent() : "");
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #d4d4d8; -fx-font-size: 14px; -fx-line-spacing: 4;");

        ScrollPane contentScroll = new ScrollPane(contentLabel);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        contentCard.getChildren().add(contentScroll);
        VBox.setVgrow(contentCard, Priority.ALWAYS);
        bodyContent.getChildren().add(contentCard);

        ScrollPane bodyScroll = new ScrollPane(bodyContent);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        bodyScroll.setStyle("-fx-background: #0a0a0a; -fx-background-color: #0a0a0a; -fx-border-color: transparent;");
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);

        HBox bottomBar = new HBox(12);
        bottomBar.setMinHeight(56);
        bottomBar.setMaxHeight(56);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setStyle(
            "-fx-padding: 0 20; -fx-background-color: #0a0a0a;" +
            " -fx-border-color: #27272a transparent transparent transparent;" +
            " -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 16 16;"
        );

        Label bottomViews = new Label(article.getViewCount() + " views");
        bottomViews.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(bottomViews, Priority.ALWAYS);

        Button shareBtn = new Button("SHARE");
        shareBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-border-color: #3f3f46;" +
            " -fx-border-width: 1; -fx-border-radius: 20; -fx-background-radius: 20;" +
            " -fx-padding: 8 24; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"
        );

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-border-color: transparent;" +
            " -fx-border-radius: 20; -fx-background-radius: 20;" +
            " -fx-padding: 8 24; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> stage.close());

        bottomBar.getChildren().addAll(bottomViews, shareBtn, closeBtn);
        root.getChildren().addAll(topBar, bodyScroll, bottomBar);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 740, 640);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        applyCss(scene);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    // ---------------------------------------------------------------
    // Play video in a styled popup (transparent, custom title bar)
    // ---------------------------------------------------------------
    private void playVideo(BlogArticle article) {
        blogService.incrementViewCount(article.getId());
        if (!article.hasVideo()) { openArticleDetail(article); return; }

        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle(article.getTitle());
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        stage.setMinWidth(820);
        stage.setMinHeight(520);
        stage.setResizable(true);

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #09090b;" +
            " -fx-border-color: #27272a;" +
            " -fx-border-width: 1;" +
            " -fx-border-radius: 16;" +
            " -fx-background-radius: 16;" +
            " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 36, 0.6, 0, 12);"
        );

        // === CUSTOM TITLE BAR ===
        HBox titleBar = new HBox(12);
        titleBar.setMinHeight(48);
        titleBar.setMaxHeight(48);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle(
            "-fx-background-color: #18181b;" +
            " -fx-border-color: transparent transparent #27272a transparent;" +
            " -fx-border-width: 0 0 1 0;" +
            " -fx-padding: 0 16;" +
            " -fx-background-radius: 16 16 0 0;"
        );

        // Red dot accent
        Region redDot = new Region();
        redDot.setMinSize(10, 10);
        redDot.setMaxSize(10, 10);
        redDot.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 5;");

        Label titleLbl = new Label(article.getTitle() != null ? article.getTitle().toUpperCase() : "VIDEO");
        titleLbl.setStyle("-fx-text-fill: #fafafa; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Button closeTitleBtn = new Button("\u2715");
        closeTitleBtn.setMinSize(28, 28);
        closeTitleBtn.setMaxSize(28, 28);
        closeTitleBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 13px;" +
            " -fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 0; -fx-cursor: hand;"
        );

        titleBar.getChildren().addAll(redDot, titleLbl, closeTitleBtn);
        titleBar.setOnMousePressed(ev -> { xOffset[0] = ev.getSceneX(); yOffset[0] = ev.getSceneY(); });
        titleBar.setOnMouseDragged(ev -> { stage.setX(ev.getScreenX() - xOffset[0]); stage.setY(ev.getScreenY() - yOffset[0]); });

        try {
            String videoUri = MediaCache.getVideoUri(article.getVideoPath());
            if (videoUri == null) throw new Exception("No video data");

            Media media = new Media(videoUri);
            MediaPlayer player = new MediaPlayer(media);
            MediaView mediaView = new MediaView(player);
            mediaView.setPreserveRatio(true);

            // Constrain both dimensions so the video never pushes controls off-screen.
            // Title bar = 48px, controls panel ≈ 110px, buffer = 12px → reserve 170px.
            mediaView.fitWidthProperty().bind(stage.widthProperty().subtract(2));
            mediaView.fitHeightProperty().bind(stage.heightProperty().subtract(170));

            StackPane videoPane = new StackPane(mediaView);
            videoPane.setStyle("-fx-background-color: #000000;");
            VBox.setVgrow(videoPane, Priority.ALWAYS);

            // === CONTROLS PANEL ===
            VBox controlsPanel = buildVideoControlsPanel(player, stage);

            root.getChildren().addAll(titleBar, videoPane, controlsPanel);

            closeTitleBtn.setOnAction(e -> { player.stop(); stage.close(); });
            stage.setOnCloseRequest(e -> player.stop());
            player.play();

        } catch (Exception e) {
            // Error state — styled consistently
            VBox errBox = new VBox(16);
            errBox.setAlignment(Pos.CENTER);
            errBox.setStyle("-fx-padding: 48;");
            VBox.setVgrow(errBox, Priority.ALWAYS);

            Label errIcon = new Label("\u26A0");
            errIcon.setStyle("-fx-font-size: 40px; -fx-text-fill: #ef4444;");

            Label errMsg = new Label("Could not play video");
            errMsg.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

            Label errDetail = new Label(e.getMessage() != null ? e.getMessage() : "Unknown error");
            errDetail.setStyle("-fx-font-size: 12px; -fx-text-fill: #71717a;");
            errDetail.setWrapText(true);

            errBox.getChildren().addAll(errIcon, errMsg, errDetail);

            closeTitleBtn.setOnAction(ev -> stage.close());
            root.getChildren().addAll(titleBar, errBox);
        }

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 820, 560);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        applyCss(scene);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    // Builds the full controls panel: play/pause, seek slider, time labels, volume
    private VBox buildVideoControlsPanel(MediaPlayer player, javafx.stage.Stage stage) {
        VBox panel = new VBox(10);
        panel.setStyle(
            "-fx-background-color: #18181b;" +
            " -fx-border-color: #27272a transparent transparent transparent;" +
            " -fx-border-width: 1 0 0 0;" +
            " -fx-padding: 12 20 14 20;" +
            " -fx-background-radius: 0 0 16 16;"
        );

        // ---- Seek / progress row ----
        HBox seekRow = new HBox(10);
        seekRow.setAlignment(Pos.CENTER_LEFT);

        Label currentTimeLbl = new Label("0:00");
        currentTimeLbl.setMinWidth(40);
        currentTimeLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");

        Slider seekSlider = new Slider(0, 100, 0);
        seekSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(seekSlider, Priority.ALWAYS);
        seekSlider.setStyle(
            "-fx-control-inner-background: #3f3f46;" +
            " -fx-accent: #dc2626;"
        );

        Label totalTimeLbl = new Label("0:00");
        totalTimeLbl.setMinWidth(40);
        totalTimeLbl.setAlignment(Pos.CENTER_RIGHT);
        totalTimeLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");

        seekRow.getChildren().addAll(currentTimeLbl, seekSlider, totalTimeLbl);

        // Bind total duration once media is ready
        player.totalDurationProperty().addListener((obs, oldDur, newDur) -> {
            if (newDur != null && !newDur.isUnknown()) {
                seekSlider.setMax(newDur.toSeconds());
                totalTimeLbl.setText(formatDuration(newDur));
            }
        });

        // Update seek slider and time label as playback progresses
        final boolean[] userSeeking = {false};
        player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!userSeeking[0] && newTime != null) {
                seekSlider.setValue(newTime.toSeconds());
                currentTimeLbl.setText(formatDuration(newTime));
            }
        });

        // Seek when user drags the slider
        seekSlider.setOnMousePressed(e -> userSeeking[0] = true);
        seekSlider.setOnMouseReleased(e -> {
            player.seek(Duration.seconds(seekSlider.getValue()));
            userSeeking[0] = false;
        });
        seekSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (userSeeking[0]) {
                currentTimeLbl.setText(formatDuration(Duration.seconds(newVal.doubleValue())));
            }
        });

        // ---- Buttons + volume row ----
        HBox controlsRow = new HBox(12);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        Button playPause = new Button("\u25B6");
        playPause.setMinSize(40, 32);
        playPause.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-size: 13px;" +
            " -fx-padding: 6 14; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        playPause.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPause.setText("\u25B6");
            } else {
                player.play();
                playPause.setText("\u23F8");
            }
        });

        // Update play/pause icon based on player status changes
        player.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.PLAYING) {
                playPause.setText("\u23F8");
            } else {
                playPause.setText("\u25B6");
            }
        });

        // Volume icon label
        Label volIcon = new Label("\uD83D\uDD0A");
        volIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");

        Slider volumeSlider = new Slider(0, 1, player.getVolume());
        volumeSlider.setMaxWidth(110);
        volumeSlider.setMinWidth(80);
        volumeSlider.setStyle(
            "-fx-control-inner-background: #3f3f46;" +
            " -fx-accent: #dc2626;"
        );
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> player.setVolume(newVal.doubleValue()));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #a1a1aa;" +
            " -fx-border-color: #3f3f46; -fx-border-width: 1;" +
            " -fx-padding: 6 20; -fx-background-radius: 6; -fx-border-radius: 6;" +
            " -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> { player.stop(); stage.close(); });

        controlsRow.getChildren().addAll(playPause, volIcon, volumeSlider, spacer, closeBtn);

        panel.getChildren().addAll(seekRow, controlsRow);
        return panel;
    }

    // Format a JavaFX Duration as M:SS
    private String formatDuration(Duration d) {
        if (d == null || d.isUnknown() || d.isIndefinite()) return "0:00";
        int totalSecs = (int) d.toSeconds();
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        return mins + ":" + String.format("%02d", secs);
    }

    // ---------------------------------------------------------------
    // Data + time helpers
    // ---------------------------------------------------------------
    private void updateSuggestions(String text) {
        suggestionPopup.getItems().clear();
        if (text.length() < 1) {
            suggestionPopup.hide();
            return;
        }
        List<BlogArticle> matches = blogService.search(text);
        if (matches.isEmpty()) {
            suggestionPopup.hide();
            return;
        }
        for (BlogArticle a : matches) {
            javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem("\u2315  " + a.getTitle());
            item.getStyleClass().add("suggestion-item");
            item.setOnAction(e -> {
                txtSearch.setText(a.getTitle());
                suggestionPopup.hide();
                loadNews();
            });
            suggestionPopup.getItems().add(item);
        }
        if (!suggestionPopup.isShowing()) {
            suggestionPopup.show(txtSearch, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    private List<BlogArticle> fetchArticles() {
        String kw = txtSearch.getText().trim();
        Object sel = cbCategory.getValue();
        if (!kw.isEmpty()) return blogService.search(kw);
        if (sel instanceof BlogCategory) return blogService.getByCategory(((BlogCategory) sel).getId());
        return blogService.getAllPublished();
    }

    private String timeAgo(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) return "";
        try {
            LocalDateTime created = LocalDateTime.parse(timestamp.replace(" ", "T").substring(0, 19));
            LocalDateTime now = LocalDateTime.now();
            long hours = ChronoUnit.HOURS.between(created, now);
            if (hours < 1) return "JUST NOW";
            if (hours < 24) return hours + " HOURS AGO";
            long days = ChronoUnit.DAYS.between(created, now);
            if (days == 1) return "YESTERDAY";
            if (days < 7) return days + " DAYS AGO";
            long weeks = days / 7;
            if (weeks < 5) return weeks + " WEEKS AGO";
            return created.format(DateTimeFormatter.ofPattern("MMM d, yyyy")).toUpperCase();
        } catch (Exception e) { return ""; }
    }
}
