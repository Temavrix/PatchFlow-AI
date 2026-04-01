package com.patchflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.github.*;

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

public class Githubpanel{
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

    private void saveProject(String projName,String langName, String bugtName,String despName,String sevName, String progname,String codsnip){
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, projName);
            stmt.setString(2, langName);
            stmt.setString(3, bugtName);
            stmt.setString(4, despName);
            stmt.setString(5, sevName);
            stmt.setString(6, progname);
            stmt.setString(7, codsnip);
            stmt.executeUpdate();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0161");
            alert.setHeaderText(null);
            alert.setContentText("Error 0161: Adding of Issue failed!");             
            alert.showAndWait();
        }
    }

    public VBox getView(Stage stage) {
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
        scrollPane.setMinWidth(600);
        scrollPane.setMaxWidth(600);
        scrollPane.setMinHeight(430);
        scrollPane.setMaxHeight(430);

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

                Label sevLabel = new Label("Choose Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");
                String bugCategories [] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugCategories));
                VBox severbox = new VBox(sevLabel,combo_box);

                Label progLabel = new Label("Choose Progress: ");
                progLabel.setStyle("-fx-text-fill: white;");
                String progCategories [] = { "To Do", "In Progress", "Code Review"};
                ComboBox<String> progcombo_box = new ComboBox<>(FXCollections.observableArrayList(progCategories));
                VBox progbox = new VBox(progLabel,progcombo_box);

                HBox chooseTime = new HBox(severbox,progbox);
                chooseTime.setSpacing(10);

                Label sniplabel = new Label("Enter Code Snippet (Optional): ");
                sniplabel.setStyle("-fx-text-fill: white;");
                TextArea sniptextArea = new TextArea();
                sniptextArea.setPromptText("Enter your code snippet here...");
                sniptextArea.setWrapText(true); 
                sniptextArea.setPrefRowCount(9);
            
                Button projbtn = new Button("Add New Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(projLabel,projtextField,lanLabel,lantextField,bugLabel,bugtextField,despLabel,desptextField,chooseTime,sniplabel,sniptextArea,projbtn);
            
                projbtn.setOnAction(ev -> {
                    String projName = projtextField.getText();
                    String langName = lantextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    String progname = progcombo_box.getValue();
                    String codsnip = sniptextArea.getText();

                    if(projName.isEmpty() || langName.isEmpty() || bugtName.isEmpty() || despName.isEmpty() || sevName == null){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Severity");
                        alert.setHeaderText(null);
                        alert.setContentText("Please select an issue severity.");
                        alert.showAndWait();
                        return;
                    }
                    saveProject(projName,langName,bugtName,despName,sevName,progname,codsnip);
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

        Gitmenu.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: black; -fx-control-inner-background: #eeeeee;");

        VBox issueArea = new VBox(8,scrollPane, addissue);
        issueArea.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        HBox searcharea = new HBox(4,Gitmenu, searchBtn);
        HBox issuearae = new HBox(projectList, issueArea);
        VBox layout = new VBox(searcharea, issuearae);
        layout.setPadding(new Insets(10));
        
        return layout;
    }

}
