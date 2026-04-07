package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.services.MediaCache;
import tn.smartfight.session.SessionManager;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.scene.media.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class BlogAdminController {

    @FXML
    private ListView<BlogArticle> articleList;

    @FXML
    private ComboBox<String> cbStatusFilter;

    @FXML
    private TextField txtSearch;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblMeta;

    @FXML
    private Label lblStatus;

    @FXML
    private TextArea txtContentReader;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnPublish;

    @FXML
    private Button btnDelete;

    @FXML
    private VBox readerPanel;

    @FXML
    private VBox editorPanel;

    @FXML
    private TextField txtEditorTitle;

    @FXML
    private TextField txtEditorSummary;

    @FXML
    private ComboBox<BlogCategory> cbEditorCategory;

    @FXML
    private TextArea txtEditorContent;

    @FXML
    private ComboBox<String> cbEditorStatus;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Label lblImagePath;

    @FXML
    private Label lblVideoPath;

    @FXML
    private StackPane videoPreviewPane;

    private final BlogService blogService = new BlogService();
    private BlogArticle selectedArticle = null;
    private boolean isNewArticle = false;
    private List<BlogArticle> allAdminArticles;

    private File selectedImageFile = null;
    private File selectedVideoFile = null;

    @FXML
    private void initialize() {
        cbStatusFilter.getItems().addAll("All", "PUBLISHED", "DRAFT", "ARCHIVED");
        cbStatusFilter.setValue("All");

        cbEditorStatus.getItems().addAll("PUBLISHED", "DRAFT", "ARCHIVED");
        cbEditorStatus.setValue("DRAFT");

        List<BlogCategory> categories = blogService.getAllCategories();
        cbEditorCategory.getItems().addAll(categories);

        loadArticles();
        showReaderPanel();
        clearReaderPanel();

        articleList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onArticleSelected(newVal)
        );

        cbStatusFilter.setOnAction(e -> onStatusFilterChanged());
    }

    private void loadArticles() {
        allAdminArticles = blogService.getAllForAdmin();
        String filter = cbStatusFilter.getValue();
        List<BlogArticle> filtered;
        if (filter == null || "All".equals(filter)) {
            filtered = new ArrayList<>(allAdminArticles);
        } else {
            final String f = filter;
            filtered = allAdminArticles.stream()
                    .filter(a -> f.equals(a.getStatus()))
                    .collect(Collectors.toList());
        }
        articleList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void onArticleSelected(BlogArticle a) {
        selectedArticle = a;
        if (a == null) return;
        lblTitle.setText(a.getTitle());
        lblMeta.setText(a.getCategoryName() + " \u00b7 " + a.getAuthorName() + " \u00b7 " + a.getCreatedAt());
        lblStatus.setText(a.getStatus());
        txtContentReader.setText(a.getContent());
        btnPublish.setDisable("PUBLISHED".equals(a.getStatus()));
        btnEdit.setDisable(false);
        btnDelete.setDisable(false);
        showReaderPanel();
    }

    @FXML
    private void onNewArticle() {
        isNewArticle = true;
        selectedArticle = null;
        clearEditorFields();
        cbEditorStatus.setValue("DRAFT");
        showEditorPanel();
    }

    @FXML
    private void onEdit() {
        if (selectedArticle == null) return;
        isNewArticle = false;
        txtEditorTitle.setText(selectedArticle.getTitle());
        txtEditorContent.setText(selectedArticle.getContent());
        txtEditorSummary.setText(selectedArticle.getSummary() != null ? selectedArticle.getSummary() : "");
        cbEditorStatus.setValue(selectedArticle.getStatus());
        // Pre-select category in editor
        for (BlogCategory cat : cbEditorCategory.getItems()) {
            if (cat.getId() == selectedArticle.getCategoryId()) {
                cbEditorCategory.setValue(cat);
                break;
            }
        }
        // Show existing media
        selectedImageFile = null;
        selectedVideoFile = null;
        if (selectedArticle.hasImage()) {
            lblImagePath.setText(selectedArticle.getImagePath());
            try {
                java.io.InputStream s = MediaCache.getImageStream(selectedArticle);
                imgPreview.setImage(s != null ? new Image(s, 260, 160, true, true) : null);
            } catch (Exception ignored) { imgPreview.setImage(null); }
        } else {
            lblImagePath.setText("No image");
            imgPreview.setImage(null);
        }
        lblVideoPath.setText(selectedArticle.hasVideo() ? selectedArticle.getVideoPath() : "No video");
        showEditorPanel();
    }

    @FXML
    private void onChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        File file = fc.showOpenDialog(articleList.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            lblImagePath.setText(file.getName());
            try {
                Image img = new Image(file.toURI().toString(), 260, 160, true, true);
                imgPreview.setImage(img);
            } catch (Exception ignored) {}
        }
    }

    @FXML
    private void onChooseVideo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Video");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.webm"));
        File file = fc.showOpenDialog(articleList.getScene().getWindow());
        if (file != null) {
            selectedVideoFile = file;
            lblVideoPath.setText(file.getName());
        }
    }

    @FXML
    private void onSave() {
        String title = txtEditorTitle.getText().trim();
        if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Title cannot be empty.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        BlogArticle a = isNewArticle ? new BlogArticle() : selectedArticle;
        a.setTitle(title);
        a.setContent(txtEditorContent.getText());
        a.setSummary(txtEditorSummary.getText().trim());
        a.setStatus(cbEditorStatus.getValue() != null ? cbEditorStatus.getValue() : "DRAFT");
        a.setAuthorId(SessionManager.getCurrentUserId());
        if (cbEditorCategory.getValue() != null) {
            a.setCategoryId(cbEditorCategory.getValue().getId());
        }
        if (isNewArticle) {
            // Create row first to get the real ID, then save media with that ID
            int newId = blogService.create(a);
            if (newId > 0) {
                a.setId(newId);
                if (selectedImageFile != null) {
                    try {
                        a.setImagePath(MediaCache.saveImage(selectedImageFile, newId));
                    } catch (IOException e) {
                        System.err.println("[BlogAdmin] save image: " + e.getMessage());
                    }
                }
                if (selectedVideoFile != null) {
                    try {
                        a.setVideoPath(MediaCache.saveVideo(selectedVideoFile, newId));
                    } catch (IOException e) {
                        System.err.println("[BlogAdmin] save video: " + e.getMessage());
                    }
                }
                // Update media paths if any media was chosen
                if (selectedImageFile != null || selectedVideoFile != null) {
                    blogService.update(a);
                }
            }
        } else {
            if (selectedImageFile != null) {
                try {
                    a.setImagePath(MediaCache.saveImage(selectedImageFile, a.getId()));
                } catch (IOException e) {
                    System.err.println("[BlogAdmin] save image: " + e.getMessage());
                }
            }
            if (selectedVideoFile != null) {
                try {
                    a.setVideoPath(MediaCache.saveVideo(selectedVideoFile, a.getId()));
                } catch (IOException e) {
                    System.err.println("[BlogAdmin] save video: " + e.getMessage());
                }
            }
            blogService.update(a);
        }
        selectedImageFile = null;
        selectedVideoFile = null;
        loadArticles();
        showReaderPanel();
    }

    @FXML
    private void onCancel() {
        showReaderPanel();
    }

    @FXML
    private void onPublish() {
        if (selectedArticle == null) return;
        blogService.setStatus(selectedArticle.getId(), "PUBLISHED");
        loadArticles();
    }

    @FXML
    private void onDelete() {
        if (selectedArticle == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selectedArticle.getTitle() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                blogService.delete(selectedArticle.getId());
                selectedArticle = null;
                loadArticles();
                clearReaderPanel();
            }
        });
    }

    @FXML
    private void onStatusFilterChanged() {
        loadArticles();
    }

    @FXML
    private void onSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadArticles();
            return;
        }
        if (allAdminArticles == null) return;
        List<BlogArticle> filtered = allAdminArticles.stream()
                .filter(a -> a.getTitle() != null && a.getTitle().toLowerCase().contains(kw))
                .collect(Collectors.toList());
        articleList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showReaderPanel() {
        readerPanel.setVisible(true);
        readerPanel.setManaged(true);
        editorPanel.setVisible(false);
        editorPanel.setManaged(false);
    }

    private void showEditorPanel() {
        readerPanel.setVisible(false);
        readerPanel.setManaged(false);
        editorPanel.setVisible(true);
        editorPanel.setManaged(true);
    }

    private void clearReaderPanel() {
        lblTitle.setText("");
        lblMeta.setText("");
        lblStatus.setText("");
        txtContentReader.setText("");
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnPublish.setDisable(true);
    }

    private void clearEditorFields() {
        txtEditorTitle.clear();
        txtEditorContent.clear();
        txtEditorSummary.clear();
        cbEditorCategory.setValue(null);
        cbEditorStatus.setValue("DRAFT");
        selectedImageFile = null;
        selectedVideoFile = null;
        imgPreview.setImage(null);
        lblImagePath.setText("No image");
        lblVideoPath.setText("No video");
    }
}
