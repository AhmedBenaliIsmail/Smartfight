package tn.smartfight.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.models.User;
import tn.smartfight.services.UserService;

import java.util.List;

public class UsersAdminController {

    @FXML private TextField          tfSearch;
    @FXML private TableView<User>    table;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colActive;
    @FXML private Label lblStatus;

    private final UserService service = new UserService();
    private final ObservableList<User> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRoleName() != null ? c.getValue().getRoleName() :
            String.valueOf(c.getValue().getRoleId())));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPhone() != null ? c.getValue().getPhone() : "—"));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));

        table.setItems(data);
        tfSearch.textProperty().addListener((obs, o, n) -> filter(n));
        loadData();
    }

    @FXML private void onAddClicked()  { showDialog(null); }

    @FXML
    private void onEditClicked() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a user to edit."); return; }
        showDialog(sel);
    }

    @FXML
    private void onDeleteClicked() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Select a user to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete user " + sel.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> {
            if (service.deleteUser(sel.getId())) { loadData(); setStatus("User deleted."); }
            else setStatus("Delete failed — user may have related records.");
        });
    }

    @FXML private void onRefreshClicked() { loadData(); }

    private void loadData() {
        List<User> users = service.getAllUsers();
        data.setAll(users);
        setStatus(users.size() + " users");
    }

    private void filter(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lower = q.toLowerCase();
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User u : data) {
            if (u.getFullName().toLowerCase().contains(lower) ||
                u.getEmail().toLowerCase().contains(lower) ||
                (u.getRoleName() != null && u.getRoleName().toLowerCase().contains(lower))) {
                filtered.add(u);
            }
        }
        table.setItems(filtered);
    }

    private void showDialog(User existing) {
        boolean isEdit = existing != null;
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit User" : "Add User");

        DialogPane pane = dlg.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color:#16161b;");

        TextField     tfFirst    = field("First Name *");
        TextField     tfLast     = field("Last Name *");
        TextField     tfEmail    = field("Email *");
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText(isEdit ? "New password (leave blank to keep)" : "Password *");
        pfPassword.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;-fx-prompt-text-fill:#888;-fx-background-radius:4;-fx-padding:6 10;");
        TextField     tfPhone    = field("Phone");
        ComboBox<String> cbRole  = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN","ORGANIZER","FIGHTER","COACH","FAN");
        cbRole.setValue("FAN");
        cbRole.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;");
        CheckBox cbActive = new CheckBox("Active");
        cbActive.setStyle("-fx-text-fill:#fff;");
        cbActive.setSelected(true);

        if (isEdit) {
            tfFirst.setText(existing.getFirstName());
            tfLast.setText(existing.getLastName());
            tfEmail.setText(existing.getEmail());
            tfPhone.setText(existing.getPhone() != null ? existing.getPhone() : "");
            if (existing.getRoleName() != null) cbRole.setValue(existing.getRoleName());
            cbActive.setSelected(existing.isActive());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:#16161b;");
        addRow(grid, 0, "First Name *", tfFirst);
        addRow(grid, 1, "Last Name *",  tfLast);
        addRow(grid, 2, "Email *",      tfEmail);
        addRow(grid, 3, "Password",     pfPassword);
        addRow(grid, 4, "Phone",        tfPhone);
        addRow(grid, 5, "Role",         cbRole);
        addRow(grid, 6, "Status",       cbActive);
        pane.setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            if (tfFirst.getText().isBlank() || tfLast.getText().isBlank() || tfEmail.getText().isBlank()) {
                setStatus("Name and email are required."); return null;
            }
            User u = isEdit ? existing : new User();
            u.setFirstName(tfFirst.getText().trim());
            u.setLastName(tfLast.getText().trim());
            u.setEmail(tfEmail.getText().trim());
            u.setPhone(tfPhone.getText().trim());
            u.setRoleName(cbRole.getValue());
            u.setActive(cbActive.isSelected());
            // Find roleId from name mapping (simple)
            u.setRoleId(roleIdFromName(cbRole.getValue()));
            return u;
        });

        dlg.showAndWait().ifPresent(u -> {
            boolean ok;
            if (isEdit) {
                ok = service.updateUser(u);
            } else {
                String pwd = pfPassword.getText();
                if (pwd.isBlank()) { setStatus("Password is required for new users."); return; }
                ok = service.createUser(u, pwd);
            }
            if (ok) { loadData(); setStatus(isEdit ? "User updated." : "User created."); }
            else      setStatus("Save failed.");
        });
    }

    private int roleIdFromName(String name) {
        switch (name) {
            case "ADMIN":     return 1;
            case "ORGANIZER": return 2;
            case "FIGHTER":   return 3;
            case "COACH":     return 4;
            case "FAN":       return 5;
            default:          return 5;
        }
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:#fff;-fx-prompt-text-fill:#888;-fx-background-radius:4;-fx-padding:6 10;");
        return tf;
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node ctrl) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#aaa;-fx-font-size:11px;");
        g.add(lbl, 0, row);
        g.add(ctrl, 1, row);
        GridPane.setHgrow(ctrl, Priority.ALWAYS);
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }
}
