package com.patchflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;


public class Boardpanel {
    private static final String DB_URL = "jdbc:sqlite:Patchflow.db";
    ObservableList<Map<String, String>> issues = FXCollections.observableArrayList();
    ObservableList<Map<String, String>> issuesone = FXCollections.observableArrayList();
    ObservableList<Map<String, String>> issuestwo = FXCollections.observableArrayList();
    ListView<Map<String, String>> todoList = new ListView<>(issues);
    ListView<Map<String, String>> progressList = new ListView<>(issuesone);
    ListView<Map<String, String>> codeRevList = new ListView<>(issuestwo);
    

    private void loadToDo(String prgdesc) {
        String sql = "SELECT project, issue, severity from projects WHERE progress = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, prgdesc);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> issue1 = new HashMap<>();
                    issue1.put("project", rs.getString("project"));
                    issue1.put("title", rs.getString("issue"));
                    issue1.put("severity", rs.getString("severity"));
                    issues.add(issue1);
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 501");
            alert.setHeaderText(null);
            alert.setContentText("Error 501: Issues details loading failed!!!");             
            alert.showAndWait();
        }
    }

    private void loadInProg(String prgdesc) {
        String sql = "SELECT project, issue, severity from projects WHERE progress = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, prgdesc);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> issue2 = new HashMap<>();
                    issue2.put("project", rs.getString("project"));
                    issue2.put("title", rs.getString("issue"));
                    issue2.put("severity", rs.getString("severity"));
                    issuesone.add(issue2);
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 502");
            alert.setHeaderText(null);
            alert.setContentText("Error 502: Issues details loading failed!!!");             
            alert.showAndWait();
        }
    }

    private void loadCodeRev(String prgdesc) {
        String sql = "SELECT project, issue, severity from projects WHERE progress = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, prgdesc);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> issue3 = new HashMap<>();
                    issue3.put("project", rs.getString("project"));
                    issue3.put("title", rs.getString("issue"));
                    issue3.put("severity", rs.getString("severity"));
                    issuestwo.add(issue3);
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 503");
            alert.setHeaderText(null);
            alert.setContentText("Error 503: Issues details loading failed!!!");             
            alert.showAndWait();
        }
    }


    
    public VBox getView(){
        loadToDo("To Do");
        loadInProg("In Progress");
        loadCodeRev("Code Review");

        todoList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                String borderColor;

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                String project = item.get("project");
                String titleText = item.get("title");
                String severity = item.get("severity");

                Label title = new Label(titleText);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
                Label proj = new Label(project);
                proj.setStyle("-fx-text-fill: gray;");
                Label sev = new Label(severity);

                switch (severity) {
                    case "Critical" -> sev.setStyle("-fx-text-fill: #FF0000;");
                    case "High" -> sev.setStyle("-fx-text-fill: #FF4500;");
                    case "Medium" -> sev.setStyle("-fx-text-fill: #FFD700;");
                    case "Low" -> sev.setStyle("-fx-text-fill: #32CD32;");
                    default -> sev.setStyle("-fx-text-fill: #808080;");
                }

                switch (severity) {
                    case "Critical" -> borderColor = "#FF0000";
                    case "High" -> borderColor = "#FF4500";
                    case "Medium" -> borderColor = "#FFD700";
                    case "Low" -> borderColor = "#32CD32";
                    default -> borderColor = "#808080";
                }

                VBox layoutone = new VBox(title, proj, sev);
                layoutone.setPadding(new Insets(10));
                layoutone.setStyle(
                    "-fx-background-color: #3c3c3e;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0 0 0 1;" +  
                    "-fx-border-color: " + borderColor + ";"
                );
                setGraphic(layoutone);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });

        todoList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );

        progressList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                String borderColor;

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null); 
                    return;
                }

                String project = item.get("project");
                String titleText = item.get("title");
                String severity = item.get("severity");

                Label title = new Label(titleText);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
                Label proj = new Label(project);
                proj.setStyle("-fx-text-fill: gray;");
                Label sev = new Label(severity);

                switch (severity) {
                    case "Critical" -> sev.setStyle("-fx-text-fill: #FF0000;");
                    case "High" -> sev.setStyle("-fx-text-fill: #FF4500;");
                    case "Medium" -> sev.setStyle("-fx-text-fill: #FFD700;");
                    case "Low" -> sev.setStyle("-fx-text-fill: #32CD32;");
                    default -> sev.setStyle("-fx-text-fill: #808080;");
                }

                switch (severity) {
                    case "Critical" -> borderColor = "#FF0000";
                    case "High" -> borderColor = "#FF4500";
                    case "Medium" -> borderColor = "#FFD700";
                    case "Low" -> borderColor = "#32CD32";
                    default -> borderColor = "#808080";
                }

                VBox layoutone = new VBox(title, proj, sev);
                layoutone.setPadding(new Insets(10));
                layoutone.setStyle(
                    "-fx-background-color: #3c3c3e;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0 0 0 1;" +  
                    "-fx-border-color: " + borderColor + ";"
                );
                setGraphic(layoutone);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });

        progressList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );

        codeRevList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                String borderColor;

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                String project = item.get("project");
                String titleText = item.get("title");
                String severity = item.get("severity");

                Label title = new Label(titleText);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
                Label proj = new Label(project);
                proj.setStyle("-fx-text-fill: gray;");
                Label sev = new Label(severity);

                switch (severity) {
                    case "Critical" -> sev.setStyle("-fx-text-fill: #FF0000;");
                    case "High" -> sev.setStyle("-fx-text-fill: #FF4500;");
                    case "Medium" -> sev.setStyle("-fx-text-fill: #FFD700;");
                    case "Low" -> sev.setStyle("-fx-text-fill: #32CD32;");
                    default -> sev.setStyle("-fx-text-fill: #808080;");
                }

                switch (severity) {
                    case "Critical" -> borderColor = "#FF0000";
                    case "High" -> borderColor = "#FF4500";
                    case "Medium" -> borderColor = "#FFD700";
                    case "Low" -> borderColor = "#32CD32";
                    default -> borderColor = "#808080";
                }

                VBox layoutone = new VBox(title, proj, sev);
                layoutone.setPadding(new Insets(10));
                layoutone.setStyle(
                    "-fx-background-color: #3c3c3e;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0 0 0 1;" +  
                    "-fx-border-color: " + borderColor + ";"
                );
                setGraphic(layoutone);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });
        
        codeRevList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );

        Label todoLabel = new Label("To Do");
        todoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        Label progressLabel = new Label("In Progress");
        progressLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        Label codeRevLabel = new Label("Code Review");
        codeRevLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");        
        
        todoList.setMinWidth(260);
        progressList.setMinWidth(270);
        codeRevList.setMinWidth(270);

        todoList.setMinHeight(450);
        progressList.setMinHeight(450);
        codeRevList.setMinHeight(450);

        VBox columnone = new VBox(todoLabel,todoList);
        columnone.setPadding(new Insets(10));
        columnone.setSpacing(10);
        VBox columntwo = new VBox(progressLabel,progressList);
        columntwo.setPadding(new Insets(10));
        columntwo.setSpacing(10);
        VBox columnthree = new VBox(codeRevLabel,codeRevList);
        columnthree.setPadding(new Insets(10));
        columnthree.setSpacing(10);

        columnone.getStyleClass().add("column");
        columntwo.getStyleClass().add("column");

        HBox draftlayout = new HBox(columnone, columntwo, columnthree);

        VBox layout = new VBox(draftlayout);
        layout.setPadding(new Insets(1));

        VBox.setVgrow(draftlayout, Priority.ALWAYS);
        layout.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return layout;
    }
}
