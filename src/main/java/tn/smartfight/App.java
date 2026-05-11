package tn.smartfight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.smartfight.config.AppConfig;
import tn.smartfight.config.DBConnection;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppConfig.loadFromClasspath("config.properties");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/smartfight/views/Login.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        var css = getClass().getResource("/styles/smartfight.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("SmartFight");
        stage.setMinWidth(860);
        stage.setMinHeight(580);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DBConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
