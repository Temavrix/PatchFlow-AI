package com.patchflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Githubpanel extends Application{
    private Stage addIssue;
    String titleofficial;
    String projectofficial;
    String languageofficial;
    String descripofficial;
    String key;
    ComboBox<String> Gitmenu = new ComboBox<>();
    private final ObservableList<String> gitprojects = FXCollections.observableArrayList();
    private final Map<String, Map<String, String>> githubissues = new HashMap<>();
    ListView<String> projectList = new ListView<>(gitprojects);

    private void loadProjectsFromGithub(String projName) {
        gitprojects.clear();
        githubissues.clear();
        try{
            GitHub github = new GitHubBuilder().withOAuthToken(key).build(); 
            GHRepository repo = github.getRepository(projName);

            for (GHIssue issue : repo.getIssues(GHIssueState.OPEN)) {
                String number = String.valueOf(issue.getNumber());
                String title = issue.getTitle();
                String body = issue.getBody();

                gitprojects.add(title);
                Map<String,String> issueData = new HashMap<>();
                issueData.put("number", number);
                projectofficial = repo.getName();
                languageofficial = repo.getLanguage();
                issueData.put("title", title);
                titleofficial = title;
                issueData.put("body", body);
                descripofficial = body;

                githubissues.put(title, issueData);
            }
        } catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 016");
            alert.setHeaderText(null);
            alert.setContentText("Error 016: Please ensure validity of API Keys in settings!");             
            alert.showAndWait();
        }
    }

    private void saveProject(String projName,String langName, String bugtName,String despName,String sevName, String codsnip){
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, projName);
            stmt.setString(2, langName);
            stmt.setString(3, bugtName);
            stmt.setString(4, despName);
            stmt.setString(5, sevName);
            stmt.setString(6, codsnip);
            stmt.executeUpdate();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0161");
            alert.setHeaderText(null);
            alert.setContentText("Error 0161: Adding of Issue failed!");             
            alert.showAndWait();
        }
    }

    @Override
    public void start(Stage stage) {
        String sql = "SELECT apikey FROM apikeys WHERE apiname='github'";
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            key = rs.getString("apikey");

            GitHub github = new GitHubBuilder().withOAuthToken(key).build();
            Map<String, GHRepository> repos = github.getMyself().getAllRepositories();
            
            for (GHRepository repo : repos.values()) {
                Gitmenu.getItems().add(repo.getFullName());
            }
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0160");
            alert.setHeaderText(null);
            alert.setContentText("Error 0160: Please ensure API Keys is entered in settings!");             
            alert.showAndWait();
        }

        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        searchBtn.setOnAction(ev -> {
            String projName = Gitmenu.getValue();
            loadProjectsFromGithub(projName);
        });


        projectList.setPrefWidth(350);
        projectList.setFixedCellSize(60);
        projectList.setCellFactory(listview -> new ListCell<String>() {
            @Override
            protected void updateItem(String issueTitle, boolean empty) {
                super.updateItem(issueTitle, empty);
                if (empty || issueTitle == null) {
                    setGraphic(null);
                    return;
                }

                Map<String,String> issue = githubissues.get(issueTitle);
                if (issue == null) {
                    setGraphic(null);
                    return;
                }

                String IssueNumber = issue.get("number");
                Label title = new Label(issueTitle);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
                Label badge = new Label("Issue #" + IssueNumber);
                badge.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 13px;");
                VBox text = new VBox(4, title, badge);
                HBox row = new HBox(10, text);
                setGraphic(row);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });

        projectList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        

        Label issulabel = new Label("");
        issulabel.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-background-color: #2e2f31; -fx-control-inner-background: #2e2f31;");


        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Map<String,String> issue = githubissues.get(newVal);
                if(issue == null){
                    return;
                }
            
                String number = issue.get("number");
                String title = issue.get("title");
                titleofficial = title;
                String body = issue.get("body");
                descripofficial = body;
            
                issulabel.setText(
                    "Issue #" + number +
                    "\n\nTitle: " + title +
                    "\n\nDescription:\n" + body
                );

            }
        });

        issulabel.setWrapText(true);
        VBox issueContent = new VBox(issulabel);
        issueContent.setSpacing(10);
        issueContent.setStyle("-fx-background-color: #2e2f31;");
        ScrollPane scrollPane = new ScrollPane(issueContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: #2e2f31;" +
            "-fx-background-color: #2e2f31;"
        );
        scrollPane.setMinWidth(400);
        scrollPane.setMaxWidth(400);
        scrollPane.setMinHeight(300);
        scrollPane.setMaxHeight(300);

        Button addissue = new Button("Add issue");
        addissue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        
        addissue.visibleProperty().bind(
        issulabel.textProperty()
                .isEqualTo("")
                .not()
        );

        addissue.setOnAction(e -> {
            if (addIssue == null || !addIssue.isShowing()){
                addIssue = new Stage();
                addIssue.initOwner(stage);
                VBox dialogVbox = new VBox(10);
                Label projLabel = new Label("Project Name: ");
                projLabel.setStyle("-fx-text-fill: white;");
                TextField projtextField = new TextField(projectofficial);
            
                Label lanLabel = new Label("Project's Language: ");
                lanLabel.setStyle("-fx-text-fill: white;");
                TextField lantextField = new TextField(languageofficial);
            
                Label bugLabel = new Label("Issue Title: ");
                bugLabel.setStyle("-fx-text-fill: white;");
                TextField bugtextField = new TextField(titleofficial);
                Label despLabel = new Label("Issue Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextField desptextField = new TextField(descripofficial);
                Label sevLabel = new Label("Issue's Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");
                
                Label sniplabel = new Label("Enter Code Snippet (Optional): ");
                sniplabel.setStyle("-fx-text-fill: white;");
                TextArea sniptextArea = new TextArea();
                sniptextArea.setPromptText("Enter your code snippet here...");
                sniptextArea.setWrapText(true); 
                sniptextArea.setPrefRowCount(9);
            
                String bugscategory[] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugscategory));
            
                Button projbtn = new Button("Add New Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(projLabel,projtextField,lanLabel,lantextField,bugLabel,bugtextField,despLabel,desptextField,sevLabel,combo_box,sniplabel,sniptextArea,projbtn);
            
                projbtn.setOnAction(ev -> {
                    String projName = projtextField.getText();
                    String langName = lantextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    String codsnip = sniptextArea.getText();

                    if(projName.isEmpty() || langName.isEmpty() || bugtName.isEmpty() || despName.isEmpty() || sevName == null){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Severity");
                        alert.setHeaderText(null);
                        alert.setContentText("Please select an issue severity.");
                        alert.showAndWait();
                        return;
                    }
                    saveProject(projName,langName,bugtName,despName,sevName,codsnip);
                    addIssue.close();
                });
            
                Scene dialogScene = new Scene(dialogVbox, 300, 500);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                addIssue.setScene(dialogScene);
                addIssue.getIcons().add(new Image("/icons/patchflowtrim.png"));
                addIssue.show();
            } else {
                addIssue.toFront();
            }
        });


        VBox issueArea = new VBox(8,scrollPane, addissue);
        issueArea.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        HBox searcharea = new HBox(Gitmenu, searchBtn);
        HBox issuearae = new HBox(projectList, issueArea);
        VBox root = new VBox(searcharea, issuearae);
        root.setSpacing(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #2e2f31;");

        Scene scene = new Scene(root, 700, 400);
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icons/patchflowtrim.png"));
        stage.setTitle("Github Dashboard");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
