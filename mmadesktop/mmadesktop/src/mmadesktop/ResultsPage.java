package mmadesktop;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import service.FightResultservice;
import service.Fighterservice;
import service.Eventservice;
import service.RankingService;
import model.FightResult;
import model.Fighter;
import model.Event;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ResultsPage {

    private final FightResultservice resultService  = new FightResultservice();
    private final Fighterservice     fighterService = new Fighterservice();
    private final Eventservice       eventService   = new Eventservice();
    private final RankingService     rankingService = new RankingService();
    private final BorderPane         root           = new BorderPane();
    private TableView<DisplayResult> table;
    private final ObservableList<DisplayResult> masterList = FXCollections.observableArrayList();
    private TextField     searchField;
    private ComboBox<String> filterStatus, filterMethod, filterSort;
    private Label notifLabel;
    
    private final boolean isAdmin;

    static class DisplayResult {
        int resultId, eventId, fightNumber, round, fighter1Id, fighter2Id;
        String eventName, fighter1Name, fighter2Name, winnerName, method, status;
        LocalDateTime fightDate;
    }

    public ResultsPage(boolean isAdmin) {
        this.isAdmin = isAdmin;
        
        root.setStyle("-fx-background-color:#0a0a0b;");
        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 32, 32, 32));
        content.setStyle("-fx-background-color:#0a0a0b;");
        content.getChildren().addAll(buildNotifBar(), buildHeader(), buildToolbar(), buildTableCard());
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0a0a0b;-fx-background-color:#0a0a0b;");
        root.setCenter(scroll);
        loadData();
    }

    public Node getRoot() { return root; }

    private HBox buildNotifBar() {
        notifLabel = new Label("");
        notifLabel.setStyle("-fx-text-fill:#22c55e;-fx-font-size:12px;-fx-font-weight:bold;");
        notifLabel.setVisible(false);
        HBox bar = new HBox(notifLabel);
        bar.setPadding(new Insets(0, 0, 4, 0));
        return bar;
    }

    // RENAMED from notify() to showNotif() to avoid conflict with Object.notify()
    private void showNotif(String msg, boolean error) {
        javafx.application.Platform.runLater(() -> {
            notifLabel.setText((error ? "⚠  " : "✔  ") + msg);
            notifLabel.setStyle("-fx-text-fill:" + (error ? "#e8001c" : "#22c55e") +
                ";-fx-font-size:12px;-fx-font-weight:bold;");
            notifLabel.setVisible(true);
        });
        new Timer().schedule(new TimerTask() {
            public void run() { javafx.application.Platform.runLater(() -> notifLabel.setVisible(false)); }
        }, 4000);
    }

    private HBox buildHeader() {
        Label title = new Label("FIGHT RESULTS");
        title.getStyleClass().add("page-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        
        HBox h = new HBox(10, title, sp);
        h.setAlignment(Pos.CENTER_LEFT);

        if (isAdmin) {
            Button schedBtn = new Button("+ SCHEDULE FIGHT");
            schedBtn.getStyleClass().add("btn-red");
            schedBtn.setOnAction(e -> showScheduleDialog());
            h.getChildren().add(schedBtn);
        }

        Button excelBtn = new Button("EXCEL");
        excelBtn.getStyleClass().add("btn-dark");
        excelBtn.setOnAction(e -> exportExcel());
        
        Button ref = new Button("↻");
        ref.getStyleClass().add("btn-dark");
        ref.setOnAction(e -> { loadData(); showNotif("Refreshed", false); });
        
        h.getChildren().addAll(excelBtn, ref);
        return h;
    }

    private HBox buildToolbar() {
        searchField = new TextField();
        searchField.setPromptText("🔍  Search fighter, event, method...");
        searchField.getStyleClass().add("text-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((o, ov, nv) -> applyFilters());

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("All Status","SCHEDULED","COMPLETED","CANCELLED");
        filterStatus.setValue("All Status"); filterStatus.getStyleClass().add("combo-box");
        filterStatus.setOnAction(e -> applyFilters());

        filterMethod = new ComboBox<>();
        filterMethod.getItems().addAll("All Methods","KO","TKO","SUBMISSION","DECISION","DRAW");
        filterMethod.setValue("All Methods"); filterMethod.getStyleClass().add("combo-box");
        filterMethod.setOnAction(e -> applyFilters());

        filterSort = new ComboBox<>();
        filterSort.getItems().addAll("Date ↓","Date ↑","Event","Fight #","Method");
        filterSort.setValue("Date ↓"); filterSort.getStyleClass().add("combo-box");
        filterSort.setOnAction(e -> applyFilters());

        HBox bar = new HBox(10, searchField, filterStatus, filterMethod, filterSort);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox buildTableCard() {
        table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(500);

        TableColumn<DisplayResult,String> idCol     = col("ID",    45, d -> new SimpleStringProperty(String.valueOf(d.getValue().resultId)));
        TableColumn<DisplayResult,String> evCol     = col("EVENT", 140, d -> new SimpleStringProperty(d.getValue().eventName));
        TableColumn<DisplayResult,String> fnCol     = col("#",     38,  d -> new SimpleStringProperty(String.valueOf(d.getValue().fightNumber)));
        TableColumn<DisplayResult,String> muCol     = col("MATCHUP",200,d -> new SimpleStringProperty(d.getValue().fighter1Name+" vs "+d.getValue().fighter2Name));
        TableColumn<DisplayResult,String> winCol    = col("WINNER",130, d -> new SimpleStringProperty(d.getValue().winnerName!=null?d.getValue().winnerName:"—"));
        TableColumn<DisplayResult,String> metCol    = col("METHOD",100, d -> new SimpleStringProperty(d.getValue().method!=null?d.getValue().method:"—"));
        TableColumn<DisplayResult,String> rdCol     = col("RD",    38,  d -> new SimpleStringProperty(d.getValue().round>0?String.valueOf(d.getValue().round):"—"));
        TableColumn<DisplayResult,String> dateCol   = col("DATE",  130, d -> new SimpleStringProperty(
            d.getValue().fightDate!=null?d.getValue().fightDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")):"—"));

        TableColumn<DisplayResult,String> stCol = col("STATUS", 90, d -> new SimpleStringProperty(d.getValue().status));
        stCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty||s==null){setText(null);setStyle("");return;}
                setText(s);
                setStyle(s.equals("COMPLETED")?"-fx-text-fill:#22c55e;-fx-font-weight:bold;":
                         s.equals("SCHEDULED") ?"-fx-text-fill:#f5a623;-fx-font-weight:bold;":
                                                 "-fx-text-fill:#e8001c;-fx-font-weight:bold;");
            }
        });

        table.getColumns().addAll(idCol,evCol,fnCol,muCol,winCol,metCol,rdCol,dateCol,stCol);

        if (isAdmin) {
            TableColumn<DisplayResult,Void> actCol = new TableColumn<>("ACTIONS");
            actCol.setPrefWidth(210);
            actCol.setCellFactory(c -> new TableCell<>() {
                final Button enterBtn  = new Button("Enter Result");
                final Button cancelBtn = new Button("Cancel");
                final Button delBtn    = new Button("Delete");
                final Button qrBtn     = new Button("QR");
                final HBox box = new HBox(5, enterBtn, cancelBtn, delBtn, qrBtn);
                {
                    enterBtn.getStyleClass().add("btn-red");
                    cancelBtn.getStyleClass().add("btn-dark");
                    qrBtn.getStyleClass().add("btn-dark");
                    delBtn.setStyle("-fx-background-color:rgba(232,0,28,.15);-fx-text-fill:#e8001c;" +
                        "-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:5 8;-fx-background-radius:4;-fx-cursor:hand;");
                    box.setAlignment(Pos.CENTER_LEFT);
                    enterBtn.setOnAction(e  -> showEnterResultDialog(getTableView().getItems().get(getIndex())));
                    cancelBtn.setOnAction(e -> {
                        DisplayResult dr = getTableView().getItems().get(getIndex());
                        if (confirm("Cancel fight #"+dr.fightNumber+"?")) {
                            resultService.cancelFight(dr.resultId);
                            loadData(); 
                            showNotif("Fight cancelled", false);
                        }
                    });
                    delBtn.setOnAction(e -> {
                        DisplayResult dr = getTableView().getItems().get(getIndex());
                        if (confirm("Delete result #"+dr.resultId+"? Cannot be undone.")) {
                            resultService.deleteFightResult(dr.resultId);
                            loadData(); 
                            showNotif("Deleted", false);
                        }
                    });
                    qrBtn.setOnAction(e -> showQRDialog(getTableView().getItems().get(getIndex())));
                }
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty){setGraphic(null);return;}
                    DisplayResult dr = getTableView().getItems().get(getIndex());
                    enterBtn.setVisible("SCHEDULED".equals(dr.status));
                    cancelBtn.setVisible("SCHEDULED".equals(dr.status));
                    setGraphic(box);
                }
            });
            table.getColumns().add(actCol);
        }

        VBox card = new VBox(table);
        card.setStyle("-fx-background-color:#16161b;-fx-border-color:#222228;-fx-border-width:1;" +
                      "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:20;");
        return card;
    }

    private void loadData() {
        masterList.clear();
        Map<Integer,String> fn = new HashMap<>(), en = new HashMap<>();
        try {
            fighterService.getAllFighters().forEach(f -> fn.put(f.getFighterId(), f.getFullName()));
            eventService.getAllEvents().forEach(e -> en.put(e.getEventId(), e.getEventName()));
            for (FightResult r : resultService.getAllFightResults()) {
                DisplayResult dr = new DisplayResult();
                dr.resultId=r.getResultId(); dr.eventId=r.getEventId();
                dr.fightNumber=r.getFightNumber(); dr.round=r.getRoundNumber();
                dr.fighter1Id=r.getFighter1Id(); dr.fighter2Id=r.getFighter2Id();
                dr.eventName   = en.getOrDefault(r.getEventId(),"Event#"+r.getEventId());
                dr.fighter1Name= fn.getOrDefault(r.getFighter1Id(),"Fighter "+r.getFighter1Id());
                dr.fighter2Name= fn.getOrDefault(r.getFighter2Id(),"Fighter "+r.getFighter2Id());
                dr.winnerName  = r.getWinnerId()!=null?fn.get(r.getWinnerId()):null;
                dr.method=r.getMethodOfVictory(); dr.status=r.getStatus();
                dr.fightDate=r.getFightDate();
                masterList.add(dr);
            }
        } catch (Exception e) { e.printStackTrace(); }
        applyFilters();
    }

    private void applyFilters() {
        String q  = searchField!=null?searchField.getText().toLowerCase().trim():"";
        String st = filterStatus!=null?filterStatus.getValue():"All Status";
        String mt = filterMethod!=null?filterMethod.getValue():"All Methods";
        String so = filterSort  !=null?filterSort.getValue()  :"Date ↓";

        List<DisplayResult> list = masterList.stream()
            .filter(d -> q.isEmpty()||d.fighter1Name.toLowerCase().contains(q)||
                d.fighter2Name.toLowerCase().contains(q)||d.eventName.toLowerCase().contains(q)||
                (d.method!=null&&d.method.toLowerCase().contains(q))||
                (d.winnerName!=null&&d.winnerName.toLowerCase().contains(q)))
            .filter(d -> "All Status".equals(st)||st.equals(d.status))
            .filter(d -> "All Methods".equals(mt)||mt.equals(d.method))
            .collect(Collectors.toList());

        switch(so){
            case "Date ↑":  list.sort(Comparator.comparing(d->d.fightDate,Comparator.nullsLast(Comparator.naturalOrder()))); break;
            case "Date ↓":  list.sort(Comparator.comparing((DisplayResult d)->d.fightDate,Comparator.nullsLast(Comparator.reverseOrder()))); break;
            case "Event":   list.sort(Comparator.comparing(d->d.eventName)); break;
            case "Fight #": list.sort(Comparator.comparingInt(d->d.fightNumber)); break;
            case "Method":  list.sort(Comparator.comparing(d->d.method==null?"":d.method)); break;
        }
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void showScheduleDialog() {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("Schedule Fight");
        DialogPane p = d.getDialogPane(); p.setStyle("-fx-background-color:#16161b;");
        p.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        ((Button)p.lookupButton(ButtonType.OK)).getStyleClass().add("btn-red");
        ((Button)p.lookupButton(ButtonType.OK)).setText("SCHEDULE");

        ComboBox<Event> evCombo = new ComboBox<>();
        try{evCombo.getItems().addAll(eventService.getAllEvents());}catch(Exception ex){}
        evCombo.setPromptText("Event"); evCombo.getStyleClass().add("combo-box");
        evCombo.setConverter(new javafx.util.StringConverter<>(){
            public String toString(Event e){return e==null?"":e.getEventName();}
            public Event fromString(String s){return null;}});

        ComboBox<Fighter> f1=new ComboBox<>(), f2=new ComboBox<>();
        try{List<Fighter> all=fighterService.getAllFighters();f1.getItems().addAll(all);f2.getItems().addAll(all);}catch(Exception ex){}
        f1.setPromptText("Fighter 1"); f1.getStyleClass().add("combo-box");
        f2.setPromptText("Fighter 2"); f2.getStyleClass().add("combo-box");

        TextField fnField=new TextField("1"); fnField.getStyleClass().add("text-field");
        GridPane g=grid(); addRow(g,0,"Event *",evCombo); addRow(g,1,"Fighter 1 *",f1);
        addRow(g,2,"Fighter 2 *",f2); addRow(g,3,"Fight # *",fnField);
        p.setContent(g);

        d.showAndWait().ifPresent(btn->{
            if(btn!=ButtonType.OK)return;
            if(evCombo.getValue()==null||f1.getValue()==null||f2.getValue()==null){showAlert("Validation","All fields required.");return;}
            try{
                resultService.addScheduledFight(evCombo.getValue().getEventId(),
                    Integer.parseInt(fnField.getText().trim()),
                    f1.getValue().getFighterId(),f2.getValue().getFighterId());
                loadData(); showNotif("Fight scheduled!",false);
            }catch(Exception ex){showAlert("Error",ex.getMessage());}
        });
    }

    private void showEnterResultDialog(DisplayResult dr) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Result: "+dr.fighter1Name+" vs "+dr.fighter2Name);
        DialogPane p = d.getDialogPane(); p.setStyle("-fx-background-color:#16161b;");
        p.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
        ((Button)p.lookupButton(ButtonType.OK)).getStyleClass().add("btn-red");
        ((Button)p.lookupButton(ButtonType.OK)).setText("SUBMIT RESULT");

        ComboBox<String> winnerCb=new ComboBox<>();
        winnerCb.getItems().addAll(dr.fighter1Name,dr.fighter2Name,"DRAW");
        winnerCb.setPromptText("Winner"); winnerCb.getStyleClass().add("combo-box");

        ComboBox<String> methodCb=new ComboBox<>();
        methodCb.getItems().addAll("KO","TKO","SUBMISSION","DECISION","DRAW");
        methodCb.setPromptText("Method"); methodCb.getStyleClass().add("combo-box");

        ComboBox<Integer> roundCb=new ComboBox<>();
        roundCb.getItems().addAll(1,2,3,4,5); roundCb.setValue(1);
        roundCb.getStyleClass().add("combo-box");

        TextField dateField=new TextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateField.getStyleClass().add("text-field");

        winnerCb.setOnAction(e->{ if("DRAW".equals(winnerCb.getValue()))methodCb.setValue("DRAW"); });

        GridPane g=grid();
        addRow(g,0,"Winner *",winnerCb); addRow(g,1,"Method *",methodCb);
        addRow(g,2,"Round *",roundCb);   addRow(g,3,"Date/Time",dateField);
        p.setContent(g);

        d.showAndWait().ifPresent(btn->{
            if(btn!=ButtonType.OK)return;
            if(winnerCb.getValue()==null||methodCb.getValue()==null){showAlert("Validation","Winner and method required.");return;}
            try{
                Integer wid=null;
                if(dr.fighter1Name.equals(winnerCb.getValue()))     wid=dr.fighter1Id;
                else if(dr.fighter2Name.equals(winnerCb.getValue()))wid=dr.fighter2Id;
                LocalDateTime fd=LocalDateTime.now();
                try{fd=LocalDateTime.parse(dateField.getText().trim(),DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));}catch(Exception ignore){}
                boolean ok = resultService.enterResult(dr.resultId, wid, methodCb.getValue(), roundCb.getValue(), fd);
                if (ok) {
                    FightResult completedFight = resultService.getFightResultById(dr.resultId);
                    if (completedFight != null && "COMPLETED".equals(completedFight.getStatus())) {
                        rankingService.processCompletedFight(completedFight);
                        showNotif("Result saved! Rankings updated automatically.", false);
                    } else {
                        showNotif("Result saved.", false);
                    }
                    loadData();
                } else {
                    showAlert("Error","Could not save result.");
                }
            }catch(Exception ex){showAlert("Error",ex.getMessage());}
        });
    }

    private void showQRDialog(DisplayResult dr) {
        String data="FIGHT #"+dr.resultId+"|EVENT:"+dr.eventName+"|"+
                    dr.fighter1Name+" vs "+dr.fighter2Name+"|STATUS:"+dr.status+
                    (dr.winnerName!=null?"|WINNER:"+dr.winnerName+" by "+dr.method:"");
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("QR — Fight #"+dr.resultId); a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color:#16161b;");
        VBox box=new VBox(12); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(16));
        box.getChildren().addAll(makeQR(data), new Label(data){{
            setStyle("-fx-text-fill:#888;-fx-font-size:10px;-fx-font-family:'Courier New';");
            setWrapText(true); setMaxWidth(300);
        }});
        a.getDialogPane().setContent(box); a.showAndWait();
    }

    private Canvas makeQR(String data) {
        int S=21,C=8; Canvas cv=new Canvas(S*C,S*C);
        GraphicsContext gc=cv.getGraphicsContext2D();
        gc.setFill(Color.WHITE); gc.fillRect(0,0,S*C,S*C);
        Random rnd=new Random(data.hashCode());
        gc.setFill(Color.rgb(10,10,11));
        for(int[] co:new int[][]{{0,0},{0,14},{14,0}}){
            for(int r=co[0];r<co[0]+7;r++)
                for(int cl=co[1];cl<co[1]+7;cl++){
                    boolean b=(r==co[0]||r==co[0]+6||cl==co[1]||cl==co[1]+6);
                    boolean in=(r>=co[0]+2&&r<=co[0]+4&&cl>=co[1]+2&&cl<=co[1]+4);
                    if(b||in)gc.fillRect(cl*C,r*C,C,C);
                }
        }
        for(int r=0;r<S;r++)
            for(int cl=0;cl<S;cl++)
                if(rnd.nextBoolean()&&!(r<7&&cl<7)&&!(r>13&&cl<7)&&!(r<7&&cl>13))
                    gc.fillRect(cl*C,r*C,C,C);
        return cv;
    }

    private void exportExcel() {
        try {
            StringBuilder csv=new StringBuilder();
            csv.append("ID,Event,Fight#,Fighter1,Fighter2,Winner,Method,Round,Date,Status\n");
            for(DisplayResult dr:table.getItems())
                csv.append(dr.resultId).append(",").append(dr.eventName).append(",")
                   .append(dr.fightNumber).append(",").append(dr.fighter1Name).append(",")
                   .append(dr.fighter2Name).append(",")
                   .append(dr.winnerName!=null?dr.winnerName:"").append(",")
                   .append(dr.method!=null?dr.method:"").append(",").append(dr.round).append(",")
                   .append(dr.fightDate!=null?dr.fightDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")):"").append(",")
                   .append(dr.status).append("\n");
            java.io.File f=new java.io.File(System.getProperty("user.home")+
                "/fight_results_"+System.currentTimeMillis()+".csv");
            java.nio.file.Files.writeString(f.toPath(),csv.toString());
            showNotif("CSV exported → "+f.getName(),false);
        }catch(Exception ex){showAlert("Export Error",ex.getMessage());}
    }

    private <T> TableColumn<DisplayResult,T> col(String n,double w,
        javafx.util.Callback<TableColumn.CellDataFeatures<DisplayResult,T>,
        javafx.beans.value.ObservableValue<T>> fn){
        TableColumn<DisplayResult,T> c=new TableColumn<>(n);
        c.setPrefWidth(w); c.setCellValueFactory(fn); return c;
    }
    private GridPane grid(){
        GridPane g=new GridPane(); g.setHgap(14); g.setVgap(12);
        g.setPadding(new Insets(20)); g.setStyle("-fx-background-color:#16161b;");
        ColumnConstraints l=new ColumnConstraints(110), v=new ColumnConstraints();
        v.setHgrow(Priority.ALWAYS); g.getColumnConstraints().addAll(l,v); return g;
    }
    private void addRow(GridPane g,int row,String lbl,Node n){
        Label l=new Label(lbl); l.getStyleClass().add("muted-label");
        g.add(l,0,row); g.add(n,1,row); GridPane.setHgrow(n,Priority.ALWAYS);
    }
    private boolean confirm(String msg){
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,msg,ButtonType.YES,ButtonType.NO);
        a.setHeaderText(null); a.getDialogPane().setStyle("-fx-background-color:#16161b;");
        return a.showAndWait().filter(b->b==ButtonType.YES).isPresent();
    }
    private void showAlert(String t,String m){
        Alert a=new Alert(Alert.AlertType.ERROR,m!=null?m:"Error",ButtonType.OK);
        a.setTitle(t); a.setHeaderText(null);
        a.getDialogPane().setStyle("-fx-background-color:#16161b;"); a.showAndWait();
    }
}