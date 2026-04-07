package mmadesktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import service.Userservice;
import model.User;
import java.util.List;
import java.util.Optional;

/**
 * USERS PAGE — Fully working implementation with validation and safety checks.
 */
public class UsersPage {

    private final Userservice service = new Userservice();
    private final BorderPane root = new BorderPane();
    private TableView<User> table;

    public UsersPage() {
        root.setStyle("-fx-background-color: #0a0a0b;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color: #0a0a0b;");

        // Header Section
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("USER MANAGEMENT");
        title.getStyleClass().add("page-title");
        
        Region sp = new Region(); 
        HBox.setHgrow(sp, Priority.ALWAYS);
        
        Button addBtn = new Button("+ REGISTER USER");
        addBtn.getStyleClass().add("btn-red");
        addBtn.setOnAction(e -> showRegisterDialog());
        
        header.getChildren().addAll(title, sp, addBtn);

        // Table Section
        table = buildTable();
        VBox tableCard = new VBox(14, table);
        tableCard.setStyle("-fx-background-color: #16161b; -fx-border-color: #222228;" +
                           "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;" +
                           "-fx-padding: 20;");

        content.getChildren().addAll(header, tableCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: #0a0a0b; -fx-background-color: #0a0a0b;");
        root.setCenter(scroll);

        loadData();
    }

    public Node getRoot() { return root; }

    private TableView<User> buildTable() {
        TableView<User> t = new TableView<>();
        t.getStyleClass().add("table-view");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(600);

        // ID Column
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setMinWidth(50);
        idCol.setMaxWidth(80);
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));

        // Username Column
        TableColumn<User, String> userCol = new TableColumn<>("USERNAME");
        userCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));

        // Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("EMAIL");
        emailCol.setCellValueFactory(d -> new SimpleStringProperty(
            (d.getValue().getEmail() == null || d.getValue().getEmail().isEmpty()) ? "—" : d.getValue().getEmail()
        ));

        // Roles Column (Fixed null-pointer risk)
        TableColumn<User, String> rolesCol = new TableColumn<>("ROLES");
        rolesCol.setCellValueFactory(d -> {
            if (d.getValue().getRoles() == null || d.getValue().getRoles().isEmpty()) {
                return new SimpleStringProperty("USER");
            }
            String roles = d.getValue().getRoles().stream()
                .map(r -> r.getRoleName())
                .reduce((a, b) -> a + ", " + b).orElse("USER");
            return new SimpleStringProperty(roles);
        });

        // Date Column
        TableColumn<User, String> dateCol = new TableColumn<>("JOINED DATE");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getCreatedDate() != null ? d.getValue().getCreatedDate().toLocalDate().toString() : "—"
        ));

        // Actions Column
        TableColumn<User, Void> actCol = new TableColumn<>("ACTIONS");
        actCol.setMinWidth(180);
        actCol.setCellFactory(c -> new TableCell<>() {
            private final Button pw = new Button("Password");
            private final Button del = new Button("Delete");
            private final HBox box = new HBox(8, pw, del);
            {
                pw.getStyleClass().add("btn-dark");
                pw.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                
                del.setStyle("-fx-background-color: rgba(232,0,28,0.1); -fx-text-fill: #e8001c; " +
                             "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 10; " +
                             "-fx-border-color: rgba(232,0,28,0.2); -fx-border-radius: 4; -fx-cursor: hand;");
                
                pw.setOnAction(e -> showChangePasswordDialog(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) setGraphic(null);
                else setGraphic(box);
            }
        });

        t.getColumns().addAll(idCol, userCol, emailCol, rolesCol, dateCol, actCol);
        return t;
    }

    private void loadData() {
        try {
            List<User> users = service.getAllUsers();
            table.getItems().setAll(users);
        } catch (Exception e) {
            System.err.println("Database error loading users: " + e.getMessage());
        }
    }

    private void showRegisterDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create System User");
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("dialog-pane");
        pane.setPrefWidth(420);
        
        // Add CSS to the dialog window
        if (root.getScene() != null && root.getScene().getStylesheets().size() > 0) {
            pane.getStylesheets().addAll(root.getScene().getStylesheets());
        }

        ButtonType regType = new ButtonType("REGISTER", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(regType, ButtonType.CANCEL);
        
        Button regBtn = (Button) pane.lookupButton(regType);
        regBtn.getStyleClass().add("btn-red");

        TextField username = sf("Enter username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Enter password");
        pass.getStyleClass().add("text-field");
        TextField email = sf("Enter email (optional)");

        // Basic Validation
        regBtn.setDisable(true);
        username.textProperty().addListener((o, old, val) -> regBtn.setDisable(val.trim().isEmpty() || pass.getText().isEmpty()));
        pass.textProperty().addListener((o, old, val) -> regBtn.setDisable(val.isEmpty() || username.getText().trim().isEmpty()));

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(25));
        
        addRow(grid, 0, "Username *", username);
        Label pl = new Label("Password *"); pl.getStyleClass().add("muted-label");
        grid.add(pl, 0, 1); grid.add(pass, 1, 1);
        addRow(grid, 2, "Email", email);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == regType) return new User(username.getText().trim(), pass.getText(), email.getText().trim());
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                service.registerUser(u);
                loadData();
            } catch (Exception e) {
                showAlert("Registration Error", e.getMessage());
            }
        });
    }

    private void showChangePasswordDialog(User user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password: " + user.getUsername());
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("dialog-pane");
        
        if (root.getScene() != null) pane.getStylesheets().addAll(root.getScene().getStylesheets());

        ButtonType changeType = new ButtonType("UPDATE PASSWORD", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(changeType, ButtonType.CANCEL);
        ((Button) pane.lookupButton(changeType)).getStyleClass().add("btn-red");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Enter new password");
        newPass.getStyleClass().add("text-field");

        VBox box = new VBox(10, new Label("New Password for " + user.getUsername()), newPass);
        box.setPadding(new Insets(20));
        pane.setContent(box);

        dialog.setResultConverter(btn -> btn == changeType ? newPass.getText() : null);

        dialog.showAndWait().ifPresent(pass -> {
            if (pass.isEmpty()) return;
            try {
                // Assuming Admin reset doesn't require the old password
                service.changePassword(user.getUserId(), null, pass);
                showAlert("Success", "Password updated for " + user.getUsername());
            } catch (Exception e) {
                showAlert("Error", "Could not change password.");
            }
        });
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete user: " + user.getUsername() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        DialogPane dp = confirm.getDialogPane();
        dp.getStyleClass().add("dialog-pane");
        if (root.getScene() != null) dp.getStylesheets().addAll(root.getScene().getStylesheets());

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.deleteUser(user.getUserId());
                loadData();
            }
        });
    }

    private TextField sf(String p) {
        TextField tf = new TextField(); 
        tf.setPromptText(p);
        tf.getStyleClass().add("text-field"); 
        return tf;
    }

    private void addRow(GridPane g, int row, String label, TextField field) {
        Label l = new Label(label); 
        l.getStyleClass().add("muted-label");
        g.add(l, 0, row); 
        g.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.getDialogPane().getStyleClass().add("dialog-pane");
        if (root.getScene() != null) a.getDialogPane().getStylesheets().addAll(root.getScene().getStylesheets());
        a.showAndWait();
    }
}