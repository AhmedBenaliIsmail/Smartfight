package tn.smartfight.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.smartfight.dao.BlogDao;
import tn.smartfight.model.BlogArticle;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminBlogListController {
    private static final Logger LOG = Logger.getLogger(AdminBlogListController.class.getName());

    @FXML private TableView<BlogArticle> blogTable;
    @FXML private TableColumn<BlogArticle, String> colTitle;
    @FXML private TableColumn<BlogArticle, String> colStatus;
    @FXML private TableColumn<BlogArticle, String> colCreated;
    @FXML private Label statusLabel;

    private final BlogDao dao = new BlogDao();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreated.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCreatedAt() != null
                        ? c.getValue().getCreatedAt().toLocalDate().toString()
                        : ""));
        loadData();
    }

    private void loadData() {
        statusLabel.setText("Loading...");
        Task<List<BlogArticle>> task = new Task<>() {
            @Override protected List<BlogArticle> call() { return dao.findAllAdmin(); }
        };
        task.setOnSucceeded(e -> {
            blogTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText(task.getValue().size() + " articles");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "Load blog failed", task.getException());
            statusLabel.setText("Failed to load");
        });
        new Thread(task, "blog-load").start();
    }

    @FXML
    private void onNew() { openForm(null); }

    @FXML
    private void onEdit() {
        BlogArticle selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select an article to edit."); return; }
        openForm(selected);
    }

    @FXML
    private void onDelete() {
        BlogArticle selected = blogTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select an article to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete article \"" + selected.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() { dao.deleteById(selected.getId()); return null; }
                };
                task.setOnSucceeded(e -> loadData());
                task.setOnFailed(e -> {
                    LOG.log(Level.SEVERE, "Delete blog failed", task.getException());
                    statusLabel.setText("Delete failed");
                });
                new Thread(task, "blog-delete").start();
            }
        });
    }

    private void openForm(BlogArticle article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/tn/smartfight/views/admin/AdminBlogForm.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(article == null ? "New Article" : "Edit Article");
            stage.setScene(new Scene(loader.load()));
            AdminBlogFormController ctrl = loader.getController();
            if (article != null) ctrl.setArticle(article);
            stage.showAndWait();
            loadData();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Open blog form failed", e);
        }
    }
}
