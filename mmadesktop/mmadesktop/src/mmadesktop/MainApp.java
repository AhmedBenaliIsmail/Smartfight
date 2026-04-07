package mmadesktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import model.Role;
import java.util.HashSet;
import java.util.Set;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a dummy admin user for testing (replace with real login later)
        User dummyUser = new User("admin", "admin", "admin@example.com");
        Role adminRole = new Role(1, "ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        dummyUser.setRoles(roles);

        MainView mainView = new MainView(dummyUser);
        Scene scene = new Scene(mainView.getRoot(), 1280, 760);
        String css = getClass().getResource("/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("MMA System");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}