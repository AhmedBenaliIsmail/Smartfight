package mmadesktop;

import javafx.application.Application;
import javafx.stage.Stage;
import service.Userservice;

public class AppLauncher extends Application {
    
    private final Userservice userService = new Userservice();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create default admin if none exists
        userService.createDefaultAdminIfNotExists();
        
        // Start with login view
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
    }
}