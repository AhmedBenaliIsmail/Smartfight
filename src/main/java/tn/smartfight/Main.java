package tn.smartfight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import tn.smartfight.database.DBMigration;
import tn.smartfight.services.MediaCache;
import tn.smartfight.services.MediaSeeder;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MediaCache.init();
        DBMigration.run();
        MediaSeeder.seed();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        stage.setTitle("SmartFight — Integrated Platform");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void styleAlertDialog(Alert alert) {
        if (alert != null && alert.getDialogPane() != null) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("/tn/smartfight/styles/smartfight.css")).toExternalForm()
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
