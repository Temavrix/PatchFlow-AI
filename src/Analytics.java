import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;

public class Analytics extends Application {

    String critical_count;
    String high_count;
    String medium_count;
    String low_count;

    private HBox createBarIcon(String color) {

        Rectangle bar1 = new Rectangle(4, 8);
        Rectangle bar2 = new Rectangle(4, 12);
        Rectangle bar3 = new Rectangle(4, 16);

        bar1.setStyle("-fx-fill: " + color + ";");
        bar2.setStyle("-fx-fill: " + color + ";");
        bar3.setStyle("-fx-fill: " + color + ";");

        HBox bars = new HBox(3, bar1, bar2, bar3);
        bars.setAlignment(Pos.CENTER);

        return bars;
    }
    

    public void getCriticalPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS critical_count FROM projects WHERE severity='Critical';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                critical_count = rs.getString("critical_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHighPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS high_count FROM projects WHERE severity='High';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                high_count = rs.getString("high_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMediumPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS medium_count FROM projects WHERE severity='Medium';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medium_count = rs.getString("medium_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLowPriorityCountFromDB(){
        
        String sql = "SELECT COUNT(severity) AS low_count FROM projects WHERE severity='Low';";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                low_count = rs.getString("low_count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start(Stage stage) {
        Analytics t = new Analytics();
        t.getHighPriorityCountFromDB();
        t.getCriticalPriorityCountFromDB();
        t.getMediumPriorityCountFromDB();
        t.getLowPriorityCountFromDB();

        Label titLabel = new Label("Analytics Dashboard");
        titLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        //Bar-chart
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();


        StackedBarChart<Number, String> chart =
                new StackedBarChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);
        chart.setMinHeight(120);
        chart.setMaxHeight(120);
        chart.setTitle("Current Open Issues by Priority");
        chart.lookup(".chart-title").setStyle("-fx-text-fill: white;");
        chart.setCategoryGap(0);

        // Example counts (replace with DB values)
        int critical = Integer.parseInt(t.critical_count);
        int high = Integer.parseInt(t.high_count);
        int medium = Integer.parseInt(t.medium_count);
        int low = Integer.parseInt(t.low_count);
        int total = critical + high + medium + low;

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(total);
        xAxis.setTickUnit(1);

        // Create series for each priority
        XYChart.Series<Number, String> criticalSeries = new XYChart.Series<>();
        criticalSeries.getData().add(new XYChart.Data<>(critical, ""));

        XYChart.Series<Number, String> highSeries = new XYChart.Series<>();
        highSeries.getData().add(new XYChart.Data<>(high, ""));

        XYChart.Series<Number, String> mediumSeries = new XYChart.Series<>();
        mediumSeries.getData().add(new XYChart.Data<>(medium, ""));

        XYChart.Series<Number, String> lowSeries = new XYChart.Series<>();
        lowSeries.getData().add(new XYChart.Data<>(low, ""));

        chart.getData().add(criticalSeries);
        chart.getData().add(highSeries);
        chart.getData().add(mediumSeries);
        chart.getData().add(lowSeries);

        chart.applyCss();
        
        chart.lookup(".chart-plot-background").setStyle("-fx-background-color: #2e2f31;");
        chart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: transparent;");
        chart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: transparent;");

        chart.lookupAll(".chart-bar").forEach(node -> {
            if (node.getStyleClass().contains("series0"))
                node.setStyle("-fx-bar-fill: #FF0000;");
            if (node.getStyleClass().contains("series1"))
                node.setStyle("-fx-bar-fill: #FF4500;");
            if (node.getStyleClass().contains("series2"))
                node.setStyle("-fx-bar-fill: #FFD700;");
            if (node.getStyleClass().contains("series3"))
                node.setStyle("-fx-bar-fill: #32CD32;");
        });





        //Labels regarding issues pending
        Label Criticallabel = new Label("Critical Issues Pending: " + t.critical_count + " pending");
        Criticallabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        Label Highlabel = new Label("High Issues Pending: " + t.high_count + " pending");
        Highlabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        Label Mediumlabel = new Label("Medium Issues Pending: " + t.medium_count + " pending");
        Mediumlabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        Label Lowlabel = new Label("Low Issues Pending: " + t.low_count + " pending");
        Lowlabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        HBox criticalBox = new HBox(10,createBarIcon("#FF0000"),Criticallabel);
        criticalBox.setAlignment(Pos.CENTER_LEFT);
        HBox highBox = new HBox(10,createBarIcon("#FF4500"),Highlabel);
        highBox.setAlignment(Pos.CENTER_LEFT);
        HBox mediumBox = new HBox(10,createBarIcon("#FFD700"),Mediumlabel);
        mediumBox.setAlignment(Pos.CENTER_LEFT);
        HBox lowBox = new HBox(10,createBarIcon("#32CD32"),Lowlabel);
        lowBox.setAlignment(Pos.CENTER_LEFT);

        VBox labelsBox = new VBox(15);
        VBox chartlabelBox = new VBox(30);
        labelsBox.setPadding(new Insets(20));
        labelsBox.getChildren().addAll(titLabel, criticalBox, highBox, mediumBox, lowBox);
        chartlabelBox.getChildren().addAll(labelsBox,chart);

        


        //HBOX (Full root window) definition
        HBox root = new HBox(chartlabelBox);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #2e2f31;");

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icons/patchflowtrim.png"));
        stage.setTitle("Analytics Dashboard");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



//// Sidebar section
//        VBox sidebar = new VBox(10);
//        sidebar.setPadding(new Insets(10));
//        sidebar.setPrefWidth(150);
//        sidebar.setStyle("-fx-background-color: #2c2f33;");
//        Label sidetitle = new Label("PatchFlow");
//        sidetitle.setStyle("-fx-text-fill: white; -fx-font-size: 18;");
//        Button analyticsBtn = new Button("Your Analytics");
//        analyticsBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
//
//        sidebar.getChildren().addAll(sidetitle, analyticsBtn);