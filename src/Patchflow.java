import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.event.EventHandler;


public class Patchflow extends Application {
    //Declaring All the Lists and Hashmaps used
    private final ObservableList<String> projects = FXCollections.observableArrayList();
    private final Map<String, Integer> projectissues = new HashMap<>();
    private final Map<String, List<String>> projectBugs = new HashMap<>();
    ListView<String> projectList = new ListView<>(projects);
    ListView<Map<String, String>> issueListView = new ListView<>();

    // Loads issues of respective projects from Database
    private ObservableList<Map<String, String>> loadIssuesFromDatabase(String project) {
       ObservableList<Map<String, String>> issues = FXCollections.observableArrayList();

       String sql = "SELECT issue, severity FROM projects WHERE project = ?";

       try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
                PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setString(1, project);
               ResultSet rs = ps.executeQuery();

           while (rs.next()) {
                Map<String, String> issue = new HashMap<>();
                issue.put("title", rs.getString("issue"));
                issue.put("priority", rs.getString("severity"));

                issues.add(issue);
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return issues;
    }

    // Loads Projects form Database
    private void loadProjectsFromDB() {
        String sql = "SELECT project, COUNT(*) AS issue_count FROM projects GROUP BY project ";

        projects.clear();
        projectissues.clear();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String project = rs.getString("project");
                int count = rs.getInt("issue_count");

                projects.add(project);
                projectissues.put(project, count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Loads issues's description and severity from Database
    private String[] loadBugDesp(String bugdesc) {
        String sql = "SELECT project, language, description, severity, snippet from projects WHERE issue = '"+bugdesc+"';";
        String[] issues = new String[5];

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();) {

            java.sql.ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                issues[0] = rs.getString("project");
                issues[1] = rs.getString("language");
                issues[2] = rs.getString("description");
                issues[3] = rs.getString("severity");
                issues[4] = rs.getString("snippet");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return issues;
    }

    // Save new project into Database
    private void saveProject(String projName,String langName, String bugtName,String despName,String sevName, String codsnip){
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, projName);
            stmt.setString(2, langName);
            stmt.setString(3, bugtName);
            stmt.setString(4, despName);
            stmt.setString(5, sevName);
            stmt.setString(6, codsnip);
            stmt.executeUpdate();
            loadProjectsFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove issue from Database
    private void removeBug(String selectedProject, String bugDescription, String bugSeverity){
        String sql = "DELETE FROM projects WHERE project = '"+selectedProject+"' AND description='"+bugDescription+"';";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            loadProjectsFromDB();
            refreshIssues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Edit issue in Database
    public void editProject(String despName, String sevName, String codeName, String selectedBug){
        String sql = "UPDATE projects SET description = ?, severity = ?, snippet = ? WHERE issue = ?";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, despName);
            stmt.setString(2, sevName);
            stmt.setString(3, codeName);
            stmt.setString(4, selectedBug);

            stmt.executeUpdate();
            loadBugDesp(selectedBug);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A function to refresh Issues if one is deleted
    private void refreshIssues() {

        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            issueListView.setItems(loadIssuesFromDatabase(selectedProject));
        } else {
            issueListView.getItems().clear();
        }
    }



    // Main Logic Code starts here
    @Override
    public void start(Stage stage) {

        // Top title
        loadProjectsFromDB();

        // Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(150);
        sidebar.setStyle("-fx-background-color: #2c2f33;");

        Label sidetitle = new Label("PatchFlow");
        sidetitle.setStyle("-fx-text-fill: white; -fx-font-size: 18;");

        Button analyticsBtn = new Button("Your Analytics");
        analyticsBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        analyticsBtn.setOnAction(e -> {
            Analytics analyticsWindow = new Analytics();
            try {
                analyticsWindow.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button bugBtn = new Button("Add New Bug");
        bugBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        bugBtn.setOnAction(
        new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initOwner(stage);
                VBox dialogVbox = new VBox(10);
                Label projLabel = new Label("Type Your Project: ");
                projLabel.setStyle("-fx-text-fill: white;");
                TextField projtextField = new TextField();

                Label lanLabel = new Label("Type Project's Language: ");
                lanLabel.setStyle("-fx-text-fill: white;");
                TextField lantextField = new TextField();

                Label bugLabel = new Label("Type Your Issue: ");
                bugLabel.setStyle("-fx-text-fill: white;");
                TextField bugtextField = new TextField();
                Label despLabel = new Label("Type Your Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextField desptextField = new TextField();
                Label sevLabel = new Label("Choose Your Severity: ");
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

                projbtn.setOnAction(e -> {
                    String projName = projtextField.getText();
                    String langName = lantextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    String codsnip = sniptextArea.getText();
                    saveProject(projName,langName,bugtName,despName,sevName,codsnip);
                    projectList.getSelectionModel().select(projName);
                    refreshIssues();
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 500);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                dialog.setScene(dialogScene);
                dialog.getIcons().add(new Image("/icons/patchflowtrim.png"));
                dialog.show();
            }
        });

        sidebar.getChildren().addAll(sidetitle, analyticsBtn, bugBtn);




        // COLUMN 1: Project Explorer Column
        // Consists of map that contains all projects currently opened

        projectList.getItems().addAll(projectBugs.keySet());
        projectList.setPrefWidth(350);
        projectList.setFixedCellSize(60);

        projectList.setCellFactory(listview -> new ListCell<String>() {
            @Override
            protected void updateItem(String project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setGraphic(null);
                    return;
                }

                int count = projectissues.getOrDefault(project, 0);

                Label title = new Label(project);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");

                Label badge = new Label(String.valueOf(count) + " Issues Pending");
                badge.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 13px;");

                VBox text = new VBox(4, title, badge);
                HBox row = new HBox(10, text);
                setGraphic(row);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });

        Label projlabel = new Label("Projects");
        projlabel.setStyle("-fx-text-fill: white;");
        projlabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        projectList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );
        VBox projectColumn = new VBox(projlabel,projectList);
        projectColumn.setPadding(new Insets(10));
        projectColumn.setSpacing(15);
        projectColumn.setPrefWidth(250);



        // COLUMN 2: Bug Explorer Column
        // Consists of Map that contains all the issues and it's priority

        issueListView.setPrefWidth(350);
        issueListView.setFixedCellSize(75);
        issueListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, String> issue, boolean empty) {
                super.updateItem(issue, empty);

                if (empty || issue == null) {
                    setGraphic(null);
                    return;
                }

                String titleText = issue.get("title");
                String priorityText = issue.get("priority");

                Rectangle dot = new Rectangle(8, 8);
                dot.setArcWidth(8);
                dot.setArcHeight(8);

                switch (priorityText) {
                    case "Critical" -> dot.setFill(Color.RED);
                    case "High" -> dot.setFill(Color.ORANGERED);
                    case "Medium" -> dot.setFill(Color.GOLD);
                    case "Low" -> dot.setFill(Color.LIMEGREEN);
                    default -> dot.setFill(Color.GRAY);
                }

                Label title = new Label(titleText);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");

                Label priority = new Label(priorityText);
                priority.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 13px;");

                VBox text = new VBox(4, title, priority);
                HBox row = new HBox(10, dot, text);
                row.setPadding(new Insets(10));
                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
                setStyle("-fx-background-color: #2e2f31;");
            }
        });

        Label emptyLabel = new Label("No issues found");
        emptyLabel.setStyle(
            "-fx-text-fill: #cfcfcf;" +
            "-fx-font-size: 14;"
        );

        issueListView.setPlaceholder(emptyLabel);

        Label issulabel = new Label("Issues");
        issulabel.setStyle("-fx-text-fill: white;");
        issulabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        issueListView.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        VBox bugColumn = new VBox(issulabel,issueListView);
        bugColumn.setPadding(new Insets(10));
        bugColumn.setSpacing(7);
        bugColumn.setPrefWidth(300);



        // COLUMN 3: Bug Details Column
        // Contains Labels, Textareas, buttons and functions
        // Functions to hide texts and load data

        TextArea Projectdeslabel = new TextArea("Select a Project");
        Projectdeslabel.setWrapText(true);
        Projectdeslabel.setMaxHeight(60);
        Projectdeslabel.getStyleClass().add("dark-text-area");

        TextArea descriptextArea = new TextArea();
        descriptextArea.setWrapText(true);
        descriptextArea.getStyleClass().add("dark-text-area");
        descriptextArea.setPrefRowCount(4);

        TextArea codsnippettextArea = new TextArea();
        codsnippettextArea.getStyleClass().add("dark-text-area-one");
        codsnippettextArea.setPrefRowCount(9);

        Label bugSeverity = new Label();
        bugSeverity.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        bugSeverity.setWrapText(true);

        Button ediissue = new Button("Edit Issue");
        ediissue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Button remissue = new Button("Close Issue");
        remissue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Rectangle dottwo = new Rectangle(8, 8);
        dottwo.setArcWidth(8);
        dottwo.setArcHeight(8);

        //Button mcpButton = new Button("✦ AI (Beta)");
        //mcpButton.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        remissue.setOnAction(e -> {
            String selectedProject = projectList.getSelectionModel().getSelectedItem();
            if (selectedProject == null) return;

            String descriptionText = descriptextArea.getText();
            descriptionText = descriptionText.replace("Issue Description: \n", "");

            String severityText = bugSeverity.getText();
            severityText = severityText.replace("Issue Severity: ","");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Close Issue");
            alert.setContentText("Are you sure you want to close this issue?");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/icons/patchflowtrim.png"));
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
            
                removeBug(selectedProject, descriptionText, severityText);
            
                projectList.getSelectionModel().select(selectedProject);
                refreshIssues();
            }
            
        });

        
        // Hides remove issue button when issue not selected
        descriptextArea.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        codsnippettextArea.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        bugSeverity.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        remissue.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        dottwo.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        ediissue.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        //mcpButton.visibleProperty().bind(
        //Projectdeslabel.textProperty()
        //        .isEqualTo("Select a Project")
        //        .not()
        //);

        ediissue.setOnAction(
        new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initOwner(stage);
                VBox dialogVbox = new VBox(10);
                Map<String, String> selectedIssue = issueListView.getSelectionModel().getSelectedItem();
                String selectedBug = selectedIssue.get("title");

                Label despLabel = new Label(" Edit Your Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextField desptextField = new TextField();
                String editDesvar = descriptextArea.getText().replace("Issue Description: \n", "");
                desptextField.setText(editDesvar);

                Label sevLabel = new Label(" Edit Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");
                String bugscategory[] = {"Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugscategory));


                Label codelabel = new Label("Edit Code Snippet: ");
                codelabel.setStyle("-fx-text-fill: white;");
                TextArea codetextfield = new TextArea();
                String editcode = codsnippettextArea.getText().replace("Code Snippet: \n\n", "");
                codetextfield.setText(editcode);
                codetextfield.setWrapText(true);
                codetextfield.setPrefRowCount(5);

                Button projbtn = new Button("Edit Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(despLabel,desptextField,codelabel,codetextfield,sevLabel,combo_box,projbtn);

                projbtn.setOnAction(e -> {
                    String despName = desptextField.getText();
                    String codeName = codetextfield.getText();
                    String sevName = combo_box.getValue();
                    editProject(despName,sevName,codeName,selectedBug);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 300);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                dialog.setScene(dialogScene);
                dialog.getIcons().add(new Image("/icons/patchflowtrim.png"));
                dialog.show();
            }
        });

        Label issudetlabel = new Label("Issue Details: ");
        issudetlabel.setStyle("-fx-text-fill: white;");
        issudetlabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        
        HBox ledlight = new HBox(8,bugSeverity,dottwo);
        ledlight.setAlignment(Pos.CENTER_LEFT);

        HBox options = new HBox(8,ediissue,remissue);//,mcpButton);

        VBox detailsColumn = new VBox(issudetlabel,Projectdeslabel,descriptextArea,codsnippettextArea,ledlight,options);
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(10);
        detailsColumn.setPrefWidth(400);
        detailsColumn.setPrefHeight(500);

        // Fetching selected items for interactions
        projectList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldProject, newProject) -> {
                if (newProject != null) {
                    issueListView.setItems(loadIssuesFromDatabase(newProject));
                }
            }
        );

        issueListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldIssue, newIssue) -> {
                    if (newIssue != null) {
                        String[] bug = loadBugDesp(newIssue.get("title"));
                        Projectdeslabel.setText("Project: "+ bug[0] + "\nLanguage: "+ bug[1]);
                        Projectdeslabel.setEditable(false);
                        descriptextArea.setText("Issue Description: \n"+ bug[2]);
                        descriptextArea.setEditable(false);

                        bugSeverity.setText("Issue Severity: " + bug[3]);
                        String editDesvarone = bugSeverity.getText().replace("Issue Severity: ", "");
                        switch (editDesvarone) {
                            case "Critical" -> dottwo.setFill(Color.RED);
                            case "High" -> dottwo.setFill(Color.ORANGERED);
                            case "Medium" -> dottwo.setFill(Color.GOLD);
                            case "Low" -> dottwo.setFill(Color.LIMEGREEN);
                            default -> dottwo.setFill(Color.GRAY);
                        }

                        if (bug[4] == null || bug[4].trim().isEmpty()){
                            codsnippettextArea.setText("");
                        } else{
                            codsnippettextArea.setText("Code Snippet: \n\n" + bug[4]);
                            codsnippettextArea.setEditable(false);
                        }
                    }
                }
        );



        // Main Scene Layout
        projectColumn.getStyleClass().add("column");
        bugColumn.getStyleClass().add("column");

        
        HBox rootmo = new HBox(sidebar, projectColumn, bugColumn, detailsColumn);
        
        VBox root = new VBox(rootmo);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #2e2f31;");

        Scene scene = new Scene(root, 1010, 505);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("PatchFlow");
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icons/patchflowtrim.png"));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
