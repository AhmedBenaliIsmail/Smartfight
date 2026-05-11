package tn.smartfight.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.smartfight.config.DBConnection;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminUsersController {
    private static final Logger LOG = Logger.getLogger(AdminUsersController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private TableView<Object[]> usersTable;
    @FXML private TableColumn<Object[], Number> colId;
    @FXML private TableColumn<Object[], String> colUsername;
    @FXML private TableColumn<Object[], String> colEmail;
    @FXML private TableColumn<Object[], Number> colPoints;
    @FXML private TableColumn<Object[], String> colRole;
    @FXML private TableColumn<Object[], String> colCreated;
    @FXML private TableColumn<Object[], Void>   colActions;
    @FXML private Label statusLabel;

    private final DataSource ds = DBConnection.getDataSource();

    @FXML
    public void initialize() {
        roleFilter.getSelectionModel().selectFirst();
        setupColumns();
        load();
    }

    private void setupColumns() {
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty((int) cd.getValue()[0]));
        colUsername.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[1]));

        colUsername.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Object[] row = getTableView().getItems().get(getIndex());
                String role = (String) row[4];
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(v);
                name.setStyle("-fx-text-fill:#ffffff;-fx-font-weight:bold;");
                Label badge = new Label("ROLE_ADMIN".equals(role) ? "adm" : "usr");
                badge.getStyleClass().add("ROLE_ADMIN".equals(role) ? "badge-red" : "badge-gray");
                box.getChildren().addAll(name, badge);
                setGraphic(box); setText(null);
            }
        });

        colEmail.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[2]));
        colEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v);
                lbl.setStyle("-fx-text-fill:#9ca3af;");
                setGraphic(lbl); setText(null);
            }
        });

        colPoints.setCellValueFactory(cd -> new SimpleIntegerProperty((int) cd.getValue()[3]));

        colRole.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[4]));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(v.replace("ROLE_", ""));
                badge.getStyleClass().add("ROLE_ADMIN".equals(v) ? "badge-red" : "badge-green");
                setGraphic(badge); setText(null);
            }
        });

        colCreated.setCellValueFactory(cd -> new SimpleStringProperty((String) cd.getValue()[5]));
        colCreated.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                Label lbl = (empty || v == null) ? null : new Label(v);
                if (lbl != null) lbl.setStyle("-fx-text-fill:#9ca3af;");
                setGraphic(lbl); setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("🗑");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setOnAction(e -> {
                    Object[] row = getTableView().getItems().get(getIndex());
                    int uid = (int) row[0];
                    String uname = (String) row[1];
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete user \"" + uname + "\"?");
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn.getButtonData().isDefaultButton()) deleteUser(uid);
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : delBtn);
            }
        });
    }

    @FXML private void onSearch() { load(); }

    @FXML private void onRegisterAdmin() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Register Admin Account");
        dlg.setHeaderText("Create a new ROLE_ADMIN user");
        TextField tfUser = new TextField(); tfUser.setPromptText("Username");
        TextField tfEmail = new TextField(); tfEmail.setPromptText("Email");
        PasswordField tfPwd = new PasswordField(); tfPwd.setPromptText("Password");
        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(8, tfUser, tfEmail, tfPwd);
        box.setPadding(new javafx.geometry.Insets(12));
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String u = tfUser.getText().trim();
                String em = tfEmail.getText().trim();
                String pw = tfPwd.getText();
                if (u.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                    statusLabel.setText("All fields required.");
                    return;
                }
                Task<Void> t = new Task<>() {
                    @Override protected Void call() throws Exception {
                        createAdminUser(u, em, pw);
                        return null;
                    }
                };
                t.setOnSucceeded(ev -> { statusLabel.setText("Admin registered."); load(); });
                t.setOnFailed(ev -> statusLabel.setText("Error: " + t.getException().getMessage()));
                new Thread(t, "user-create").start();
            }
        });
    }

    private void load() {
        String search = searchField.getText() != null ? searchField.getText().trim() : "";
        String roleVal = roleFilter.getValue();
        Task<List<Object[]>> task = new Task<>() {
            @Override protected List<Object[]> call() throws Exception {
                return fetchUsers(search, roleVal);
            }
        };
        task.setOnSucceeded(e -> {
            usersTable.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText(task.getValue().size() + " users");
        });
        task.setOnFailed(e -> {
            LOG.log(Level.SEVERE, "AdminUsers load failed", task.getException());
            statusLabel.setText("Load failed.");
        });
        new Thread(task, "users-load").start();
    }

    private List<Object[]> fetchUsers(String search, String roleFilter) throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT u.userId, u.username, u.email, u.predictionPoints, " +
                "COALESCE(r.roleName,'ROLE_USER') AS roleName, " +
                "DATE_FORMAT(u.createdDate,'%Y-%m-%d') AS created " +
                "FROM users u " +
                "LEFT JOIN user_roles ur ON ur.userId = u.userId " +
                "LEFT JOIN roles r ON r.roleId = ur.roleId " +
                "WHERE 1=1 " +
                (search.isEmpty() ? "" : "AND (u.username LIKE ? OR u.email LIKE ?) ") +
                (roleFilter == null || "All Roles".equals(roleFilter) ? "" : "AND r.roleName = ? ") +
                "ORDER BY u.userId DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (!search.isEmpty()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            if (roleFilter != null && !"All Roles".equals(roleFilter)) {
                ps.setString(idx, roleFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getInt("userId"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getInt("predictionPoints"),
                            rs.getString("roleName"),
                            rs.getString("created")
                    });
                }
            }
        }
        return result;
    }

    private void deleteUser(int userId) {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                try (Connection c = ds.getConnection()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM user_roles WHERE userId=?")) {
                        ps.setInt(1, userId); ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "DELETE FROM users WHERE userId=?")) {
                        ps.setInt(1, userId); ps.executeUpdate();
                    }
                }
                return null;
            }
        };
        t.setOnSucceeded(e -> { statusLabel.setText("User deleted."); load(); });
        t.setOnFailed(e -> statusLabel.setText("Delete failed: " + t.getException().getMessage()));
        new Thread(t, "user-delete").start();
    }

    private void createAdminUser(String username, String email, String password) throws Exception {
        String bcrypt = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(13));
        try (Connection c = ds.getConnection()) {
            int newId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users (username,password,email,createdDate,predictionPoints,is_verified) " +
                    "VALUES (?,?,?,NOW(),0,1)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, bcrypt);
                ps.setString(3, email);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new Exception("User insert failed.");
                    newId = keys.getInt(1);
                }
            }
            // find or create ROLE_ADMIN
            int roleId;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT roleId FROM roles WHERE roleName='ROLE_ADMIN' LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    roleId = rs.getInt(1);
                } else {
                    try (PreparedStatement pi = c.prepareStatement(
                            "INSERT INTO roles (roleName) VALUES ('ROLE_ADMIN')",
                            Statement.RETURN_GENERATED_KEYS)) {
                        pi.executeUpdate();
                        try (ResultSet rk = pi.getGeneratedKeys()) {
                            rk.next(); roleId = rk.getInt(1);
                        }
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO user_roles (userId,roleId) VALUES (?,?)")) {
                ps.setInt(1, newId); ps.setInt(2, roleId); ps.executeUpdate();
            }
        }
    }
}
