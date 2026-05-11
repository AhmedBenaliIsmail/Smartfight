package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.smartfight.config.Session;
import tn.smartfight.dao.BlogDao;
import tn.smartfight.model.BlogArticle;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminBlogFormController {
    private static final Logger LOG = Logger.getLogger(AdminBlogFormController.class.getName());

    @FXML private TextField titleField;
    @FXML private TextArea summaryArea;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField imagePathField;
    @FXML private TextField videoPathField;
    @FXML private Label statusLabel;

    private final BlogDao dao = new BlogDao();
    private Integer articleId;
    private int pendingCategoryId = 0;
    private final Map<String, Integer> categoryNameToId = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("DRAFT", "PUBLISHED", "ARCHIVED"));
        statusCombo.setValue("DRAFT");
        loadCategories();
    }

    private void loadCategories() {
        Task<Map<Integer, String>> task = new Task<>() {
            @Override protected Map<Integer, String> call() { return dao.loadCategories(); }
        };
        task.setOnSucceeded(e -> {
            categoryNameToId.clear();
            task.getValue().forEach((id, name) -> categoryNameToId.put(name, id));
            categoryCombo.setItems(FXCollections.observableArrayList(categoryNameToId.keySet()));
            if (pendingCategoryId > 0) {
                for (Map.Entry<String, Integer> entry : categoryNameToId.entrySet()) {
                    if (entry.getValue() == pendingCategoryId) {
                        categoryCombo.setValue(entry.getKey());
                        break;
                    }
                }
            } else if (!categoryNameToId.isEmpty()) {
                categoryCombo.getSelectionModel().selectFirst();
            }
        });
        task.setOnFailed(e -> LOG.log(Level.WARNING, "Load categories failed", task.getException()));
        new Thread(task, "blog-cats").start();
    }

    public void setArticle(BlogArticle a) {
        if (a == null) return;
        articleId = a.getId();
        titleField.setText(a.getTitle() != null ? a.getTitle() : "");
        summaryArea.setText(a.getSummary() != null ? a.getSummary() : "");
        contentArea.setText(a.getContent() != null ? a.getContent() : "");
        statusCombo.setValue(a.getStatus() != null ? a.getStatus() : "DRAFT");
        imagePathField.setText(a.getImagePath() != null ? a.getImagePath() : "");
        videoPathField.setText(a.getVideoPath() != null ? a.getVideoPath() : "");
        pendingCategoryId = a.getCategoryId();
        if (!categoryNameToId.isEmpty()) {
            for (Map.Entry<String, Integer> entry : categoryNameToId.entrySet()) {
                if (entry.getValue() == pendingCategoryId) {
                    categoryCombo.setValue(entry.getKey());
                    break;
                }
            }
        }
    }

    @FXML
    private void onBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"));
        File f = chooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (f != null) imagePathField.setText(f.getName());
    }

    @FXML
    private void onBrowseVideo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Video");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.mov", "*.avi", "*.mkv", "*.webm"));
        File f = chooser.showOpenDialog(videoPathField.getScene().getWindow());
        if (f != null) videoPathField.setText(f.getName());
    }

    @FXML
    private void onSave() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) { statusLabel.setText("Title is required."); return; }
        String catName = categoryCombo.getValue();
        if (catName == null || !categoryNameToId.containsKey(catName)) {
            statusLabel.setText("Category is required.");
            return;
        }

        BlogArticle a = new BlogArticle();
        a.setTitle(title);
        a.setSummary(summaryArea.getText());
        a.setContent(contentArea.getText());
        a.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : "DRAFT");
        a.setCategoryId(categoryNameToId.get(catName));
        String img = imagePathField.getText();
        a.setImagePath(img != null && !img.isBlank() ? img.trim() : null);
        String vid = videoPathField.getText();
        a.setVideoPath(vid != null && !vid.isBlank() ? vid.trim() : null);
        if (Session.getUser() != null) a.setAuthorId(Session.getUser().getUserId());

        statusLabel.setText("Saving...");
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                if (articleId == null) dao.create(a);
                else { a.setId(articleId); dao.update(a); }
                return null;
            }
        };
        task.setOnSucceeded(e -> ((Stage) statusLabel.getScene().getWindow()).close());
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Save blog article failed", task.getException());
            statusLabel.setText("Save failed: " + task.getException().getMessage());
        });
        new Thread(task, "blog-save").start();
    }

    @FXML
    private void onCancel() { ((Stage) statusLabel.getScene().getWindow()).close(); }
}
