package mmadesktop;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Fighter;
import service.RankingService;

public class PerformanceSafePage {
    private final RankingService rankingService = new RankingService();
    private final BorderPane root = new BorderPane();
    private final TableView<Fighter> table = new TableView<>();

    public PerformanceSafePage() {
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> loadData());
        HBox top = new HBox(refresh);

        TableColumn<Fighter, String> name = new TableColumn<>("Fighter");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        TableColumn<Fighter, String> perf = new TableColumn<>("Performance");
        perf.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int) c.getValue().getPerformanceScore())));
        TableColumn<Fighter, String> elo = new TableColumn<>("ELO");
        elo.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf((int) c.getValue().getEloRating())));
        table.getColumns().addAll(name, perf, elo);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox box = new VBox(10, top, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        root.setCenter(box);
        loadData();
    }

    private void loadData() {
        table.setItems(FXCollections.observableArrayList(rankingService.getRankedFighters()));
    }

    public Node getRoot() {
        return root;
    }
}
