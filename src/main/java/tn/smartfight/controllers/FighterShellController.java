package tn.smartfight.controllers;

import tn.smartfight.models.Fighter;
import tn.smartfight.services.FighterService;
import tn.smartfight.services.FightResultService;
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

public class FighterShellController {

    @FXML private VBox      sidebar;
    @FXML private VBox      vboxUser;
    @FXML private Label     lblBrand;
    @FXML private Label     lblSubtitle;
    @FXML private Label     lblSignedIn;
    @FXML private Label     lblUserName;
    @FXML private Button    btnProfile;
    @FXML private Button    btnFights;
    @FXML private Button    btnLogout;
    @FXML private Button    btnCollapse;
    @FXML private StackPane contentArea;

    private final FighterService     fighterService = new FighterService();
    private final FightResultService resultService  = new FightResultService();

    private boolean collapsed = false;
    private static final double W_EXPANDED  = 220;
    private static final double W_COLLAPSED = 58;

    @FXML
    private void initialize() {
        lblUserName.setText(SessionManager.getCurrentUserName());
        showProfile();
    }

    @FXML private void onProfileClicked() { showProfile(); }
    @FXML private void onFightsClicked()  { showFightHistory(); }

    @FXML
    private void onLogoutClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            ((Stage) contentArea.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showProfile() {
        int userId = SessionManager.getCurrentUserId();
        Fighter f  = fighterService.getFighterByUserId(userId);

        VBox pane = new VBox(20);
        pane.setStyle("-fx-padding:40;-fx-background-color:#09090b;");

        Label title = new Label("MY PROFILE");
        title.setStyle("-fx-text-fill:#fafafa;-fx-font-size:22px;-fx-font-weight:bold;");

        if (f != null) {
            GridPane grid = new GridPane();
            grid.setHgap(24); grid.setVgap(12);
            grid.setStyle("-fx-background-color:#18181b;-fx-padding:24;-fx-background-radius:8;-fx-border-color:#27272a;-fx-border-radius:8;");
            addInfo(grid, 0, "Name",        f.getFullName());
            addInfo(grid, 1, "Nickname",    f.getNickname() != null ? "\"" + f.getNickname() + "\"" : "—");
            addInfo(grid, 2, "Nationality", f.getNationality() != null ? f.getNationality() : "—");
            addInfo(grid, 3, "Date of Birth", f.getDateOfBirth() != null ? f.getDateOfBirth() : "—");
            addInfo(grid, 4, "Weight Class",  f.getWeightClass() != null ? f.getWeightClass() : "—");
            addInfo(grid, 5, "Record (W-L-D)", f.getRecord());
            addInfo(grid, 6, "Status",       f.getStatus() != null ? f.getStatus() : "ACTIVE");
            pane.getChildren().addAll(title, grid);
        } else {
            Label msg = new Label("No fighter profile found for your account.");
            msg.setStyle("-fx-text-fill:#71717a;-fx-font-size:14px;");
            pane.getChildren().addAll(title, msg);
        }

        contentArea.getChildren().setAll(pane);
    }

    private void showFightHistory() {
        int userId = SessionManager.getCurrentUserId();
        Fighter f  = fighterService.getFighterByUserId(userId);

        VBox pane = new VBox(16);
        pane.setStyle("-fx-padding:40;-fx-background-color:#09090b;");
        Label title = new Label("MY FIGHT HISTORY");
        title.setStyle("-fx-text-fill:#fafafa;-fx-font-size:22px;-fx-font-weight:bold;");
        pane.getChildren().add(title);

        if (f != null) {
            var results = resultService.getFightResultsByFighter(f.getId());
            TableView<tn.smartfight.models.FightResult> table = new TableView<>();
            table.setStyle("-fx-background-color:#18181b;-fx-border-color:#27272a;");
            TableColumn<tn.smartfight.models.FightResult,String> cEvent =
                col("EVENT", 180, c -> new SimpleStringProperty(c.getValue().getEventName()));
            TableColumn<tn.smartfight.models.FightResult,String> cOpponent =
                col("OPPONENT", 160, c -> {
                    var fr = c.getValue();
                    String opp = fr.getFighterRedId() == f.getId()
                        ? fr.getFighterBlueName() : fr.getFighterRedName();
                    return new SimpleStringProperty(opp != null ? opp : "—");
                });
            TableColumn<tn.smartfight.models.FightResult,String> cResult =
                col("RESULT", 100, c -> {
                    var fr = c.getValue();
                    String r = fr.getWinnerId() == f.getId() ? "WIN"
                             : fr.getWinnerId() == 0        ? "DRAW" : "LOSS";
                    return new SimpleStringProperty(r);
                });
            TableColumn<tn.smartfight.models.FightResult,String> cMethod =
                col("METHOD", 110, c -> new SimpleStringProperty(c.getValue().getMethod()));
            TableColumn<tn.smartfight.models.FightResult,String> cDate =
                col("DATE", 110, c -> new SimpleStringProperty(c.getValue().getFightDate()));
            table.getColumns().addAll(cEvent, cOpponent, cResult, cMethod, cDate);
            table.setItems(FXCollections.observableArrayList(results));
            pane.getChildren().add(table);
            VBox.setVgrow(table, Priority.ALWAYS);
        } else {
            Label msg = new Label("No fighter profile linked to your account.");
            msg.setStyle("-fx-text-fill:#71717a;");
            pane.getChildren().add(msg);
        }

        contentArea.getChildren().setAll(pane);
    }

    private TableColumn<tn.smartfight.models.FightResult,String> col(
            String name, double w,
            javafx.util.Callback<TableColumn.CellDataFeatures<tn.smartfight.models.FightResult,String>,
            javafx.beans.value.ObservableValue<String>> fn) {
        TableColumn<tn.smartfight.models.FightResult,String> c = new TableColumn<>(name);
        c.setPrefWidth(w);
        c.setCellValueFactory(fn);
        return c;
    }

    private void addInfo(GridPane g, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#71717a;-fx-font-size:12px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#fafafa;-fx-font-size:14px;-fx-font-weight:bold;");
        g.add(lbl, 0, row);
        g.add(val, 1, row);
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
            btnProfile.setText("P"); btnFights.setText("F"); btnLogout.setText("↩");
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabels(true);
                btnProfile.setText("MY PROFILE"); btnFights.setText("MY FIGHTS");
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
