package mmadesktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import service.Userservice;
import java.util.Optional;

public class LoginView {

    private final Userservice userService = new Userservice();
    private Stage primaryStage;

    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("MMA Manager – Login");

        // UI elements
        Label title = new Label("MMA FIGHT MANAGER");
        title.getStyleClass().add("page-title"); // Using your CSS class
        title.setStyle("-fx-font-size: 26px; -fx-font-family: 'Arial Black'; -fx-text-fill: #fafafa;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field");

        Button loginBtn = new Button("LOGIN");
        loginBtn.getStyleClass().add("btn-red");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button signupBtn = new Button("SIGN UP");
        signupBtn.getStyleClass().add("btn-dark");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Label message = new Label();
        message.getStyleClass().add("red-label");

        // Layout
        VBox root = new VBox(20, title, usernameField, passwordField, loginBtn, signupBtn, message);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #0f0f11;"); // Matches your .root-pane

        Scene scene = new Scene(root, 400, 450);
        
        // Load CSS safely
        applyStyle(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Login action
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                message.setText("Please fill both fields.");
                return;
            }

            Optional<User> userOpt = userService.authenticateUser(username, password);
            if (userOpt.isPresent()) {
                // IMPORTANT: We fetch the user again or ensure roles are loaded
                User loggedUser = userOpt.get();
                openMainApp(loggedUser);
            } else {
                message.setText("Invalid username or password.");
            }
        });

        // Sign-up action
        signupBtn.setOnAction(e -> showSignupDialog());
        
        // Allow pressing 'ENTER' to login
        passwordField.setOnAction(e -> loginBtn.fire());
    }

    private void openMainApp(User loggedUser) {
        MainView mainView = new MainView(loggedUser);
        Scene mainScene = new Scene(mainView.getRoot(), 1200, 750);
        applyStyle(mainScene);
        
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("MMA Fight Manager – " + loggedUser.getUsername());
        primaryStage.centerOnScreen();
    }

    private void showSignupDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        DialogPane pane = dialog.getDialogPane();
        applyStyleToPane(pane);
        
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-red");
        okBtn.setText("REGISTER");

        TextField userF = sf("Username");
        PasswordField passF = new PasswordField(); 
        passF.setPromptText("Password");
        passF.getStyleClass().add("text-field");
        TextField emailF = sf("Email (optional)");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("Username *"), 0, 0); grid.add(userF, 1, 0);
        grid.add(new Label("Password *"), 0, 1); grid.add(passF, 1, 1);
        grid.add(new Label("Email"), 0, 2);      grid.add(emailF, 1, 2);
        
        GridPane.setHgrow(userF, Priority.ALWAYS);
        GridPane.setHgrow(passF, Priority.ALWAYS);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (userF.getText().isEmpty() || passF.getText().isEmpty()) return null;
                return new User(userF.getText().trim(), passF.getText(), emailF.getText().trim());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            try {
                userService.registerUser(user, "USER");
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Account created!", ButtonType.OK);
                applyStyleToPane(info.getDialogPane());
                info.showAndWait();
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    // Helper to apply CSS to Scenes
    private void applyStyle(Scene s) {
        String css = getClass().getResource("/css/style.css").toExternalForm();
        if (css != null) s.getStylesheets().add(css);
    }

    // Helper to apply CSS to Dialogs
    private void applyStyleToPane(DialogPane p) {
        String css = getClass().getResource("/css/style.css").toExternalForm();
        if (css != null) p.getStylesheets().add(css);
        p.getStyleClass().add("dialog-pane");
    }

    private TextField sf(String p) {
        TextField tf = new TextField(); tf.setPromptText(p);
        tf.getStyleClass().add("text-field"); return tf;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        applyStyleToPane(alert.getDialogPane());
        alert.showAndWait();
    }
}