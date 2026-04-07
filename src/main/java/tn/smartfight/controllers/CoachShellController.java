package tn.smartfight.controllers;

import tn.smartfight.models.Fighter;
import tn.smartfight.services.FighterService;
import tn.smartfight.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.List;

public class CoachShellController {

    @FXML private VBox      sidebar;
    @FXML private VBox      vboxUser;
    @FXML private Label     lblBrand;
    @FXML private Label     lblSubtitle;
    @FXML private Label     lblSignedIn;
    @FXML private Label     lblUserName;
    @FXML private Button    btnFighters;
    @FXML private Button    btnResults;
    @FXML private Button    btnLogout;
    @FXML private Button    btnCollapse;
    @FXML private StackPane contentArea;

    private final FighterService fighterService = new FighterService();

    private boolean collapsed = false;
    private static final double W_EXPANDED  = 220;
    private static final double W_COLLAPSED = 58;

    @FXML
    private void initialize() {
        lblUserName.setText(SessionManager.getCurrentUserName());
        showFighters();
    }

    @FXML private void onFightersClicked() { showFighters(); }
    @FXML private void onResultsClicked()  { navigateTo("FightResultsAdminView.fxml"); }

    @FXML
    private void onLogoutClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            ((Stage) contentArea.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showFighters() {
        List<Fighter> fighters = fighterService.getAllFighters();

        VBox pane = new VBox(16);
        pane.setStyle("-fx-padding:40;-fx-background-color:#09090b;");

        Label title = new Label("FIGHTERS ROSTER");
        title.setStyle("-fx-text-fill:#fafafa;-fx-font-size:22px;-fx-font-weight:bold;");

        TableView<Fighter> table = new TableView<>();
        table.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Fighter,String> cName    = col("NAME",        200, c -> new SimpleStringProperty(c.getValue().getFullName()));
        TableColumn<Fighter,String> cNick    = col("NICKNAME",    130, c -> new SimpleStringProperty(
            c.getValue().getNickname() != null ? "\"" + c.getValue().getNickname() + "\"" : "—"));
        TableColumn<Fighter,String> cWeight  = col("WEIGHT CLASS",130, c -> new SimpleStringProperty(
            c.getValue().getWeightClass() != null ? c.getValue().getWeightClass() : "—"));
        TableColumn<Fighter,String> cRecord  = col("W-L-D",       90, c -> new SimpleStringProperty(c.getValue().getRecord()));
        TableColumn<Fighter,String> cStatus  = col("STATUS",      90, c -> new SimpleStringProperty(
            c.getValue().getStatus() != null ? c.getValue().getStatus() : "ACTIVE"));

        table.getColumns().addAll(cName, cNick, cWeight, cRecord, cStatus);
        table.setItems(FXCollections.observableArrayList(fighters));

        pane.getChildren().addAll(title, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().setAll(pane);
    }

    private TableColumn<Fighter,String> col(String name, double w,
            javafx.util.Callback<TableColumn.CellDataFeatures<Fighter,String>,
            javafx.beans.value.ObservableValue<String>> fn) {
        TableColumn<Fighter,String> c = new TableColumn<>(name);
        c.setPrefWidth(w);
        c.setCellValueFactory(fn);
        return c;
    }

    private void navigateTo(String viewName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/" + viewName));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onToggleSidebar() {
        collapsed = !collapsed;
        double target = collapsed ? W_COLLAPSED : W_EXPANDED;
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(180),
            new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
            new KeyValue(sidebar.maxWidthProperty(),  target, Interpolator.EASE_BOTH)));
        if (collapsed) {
            setLabels(false);
            btnFighters.setText("F"); btnResults.setText("R"); btnLogout.setText("↩");
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabels(true);
                btnFighters.setText("FIGHTERS"); btnResults.setText("FIGHT RESULTS");
                btnLogout.setText("LOGOUT"); btnCollapse.setText("‹");
            });
        }
        tl.play();
    }

    private void setLabels(boolean v) {
        for (Node n : new Node[]{lblBrand, lblSubtitle, lblSignedIn, lblUserName}) {
            n.setVisible(v); n.setManaged(v);
        }
        vboxUser.setStyle(v
            ? "-fx-padding:4 14 18 18;-fx-border-color:transparent transparent #27272a transparent;-fx-border-width:0 0 1 0;"
            : "-fx-padding:0;-fx-border-width:0;");
    }
}
