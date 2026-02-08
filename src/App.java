import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.event.EventHandler;


public class App extends Application {
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
        String sql = "SELECT project, description, severity from projects WHERE issue = '"+bugdesc+"';";
        String[] issues = new String[3];

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();) {

            java.sql.ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                issues[0] = rs.getString("project");
                issues[1] = rs.getString("description");
                issues[2] = rs.getString("severity");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return issues;
    }

    // Save new project into Database
    private void saveProject(String projName,String bugtName,String despName,String sevName){
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, projName);
            stmt.setString(2, bugtName);
            stmt.setString(3, despName);
            stmt.setString(4, sevName);
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
    public void editProject(String despName, String sevName, String selectedBug){
        String sql = "UPDATE projects SET description='"+despName+"', severity='"+sevName+"' WHERE issue = '"+selectedBug+"';";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
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
        Label title = new Label("  PATCHFLOW");
        title.setLayoutX(30);
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
                Label bugLabel = new Label("Type Your Issue: ");
                bugLabel.setStyle("-fx-text-fill: white;");
                TextField bugtextField = new TextField();
                Label despLabel = new Label("Type Your Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextField desptextField = new TextField();
                Label sevLabel = new Label("Choose Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");

                String bugscategory[] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugscategory));

                Button projbtn = new Button("Add New Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(projLabel,projtextField,bugLabel,bugtextField,despLabel,desptextField,sevLabel,combo_box,projbtn);

                projbtn.setOnAction(e -> {
                    String projName = projtextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    saveProject(projName,bugtName,despName,sevName);
                    projectList.getSelectionModel().select(projName);
                    refreshIssues();
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 300);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });

        sidebar.getChildren().addAll(sidetitle, analyticsBtn, bugBtn);




        // COLUMN 1: Project Explorer

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



        // COLUMN 2: Bug Explorer

        
        issueListView.setPrefWidth(350);
        issueListView.setFixedCellSize(75);

        // Custom cell rendering
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



        // COLUMN 3: Bug Details 

        // Bug Details elements
        Label Projectdeslabel = new Label("Select a Project");
        Projectdeslabel.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Label bugDescription = new Label("Select a Issue to see Description");
        bugDescription.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        bugDescription.setWrapText(true);

        Label bugSeverity = new Label("Select a Issue to see Severity");
        bugSeverity.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        bugSeverity.setWrapText(true);

        Button ediissue = new Button("Edit Issue");
        ediissue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Button remissue = new Button("Remove Issue");
        remissue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Rectangle dottwo = new Rectangle(8, 8);
        dottwo.setArcWidth(8);
        dottwo.setArcHeight(8);

        remissue.setOnAction(e -> {
            String selectedProject = projectList.getSelectionModel().getSelectedItem();
            if (selectedProject == null) return;

            String descriptionText = bugDescription.getText();
            descriptionText = descriptionText.replace("Issue Description: ", "");

            String severityText = bugSeverity.getText();
            severityText = severityText.replace("Issue Severity: ","");

            removeBug(selectedProject, descriptionText, severityText);

            projectList.getSelectionModel().select(selectedProject);
            refreshIssues();
            
            Projectdeslabel.setText("Select a Project");
            bugDescription.setText("Select a Issue to see Description");
            bugSeverity.setText("Select a Issue to see Severity");
        });

        

        // Hides remove issue button when issue not selected
        remissue.visibleProperty().bind(
        bugSeverity.textProperty()
                .isEqualTo("Select a Issue to see Severity")
                .not()
        );

        dottwo.visibleProperty().bind(
        bugSeverity.textProperty()
                .isEqualTo("Select a Issue to see Severity")
                .not()
        );

        // Hides edit issue button when issue not selected
        ediissue.visibleProperty().bind(
        bugDescription.textProperty()
                .isEqualTo("Select a Issue to see Description")
                .not()
        );

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
                String editDesvar = bugDescription.getText().replace("Issue Description: ", "");
                desptextField.setText(editDesvar);

                Label sevLabel = new Label(" Edit Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");

                String bugscategory[] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugscategory));

                Button projbtn = new Button("Edit Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(despLabel,desptextField,sevLabel,combo_box,projbtn);

                projbtn.setOnAction(e -> {
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    editProject(despName,sevName,selectedBug);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 180);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        Label issudetlabel = new Label("Issue Details: ");
        issudetlabel.setStyle("-fx-text-fill: white;");
        issudetlabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        
        HBox ledlight = new HBox(8,bugSeverity,dottwo);
        ledlight.setAlignment(Pos.CENTER_LEFT);

        HBox options = new HBox(8,ediissue,remissue);

        VBox detailsColumn = new VBox(issudetlabel,Projectdeslabel,bugDescription,ledlight,options);
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(19);
        detailsColumn.setPrefWidth(400);



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
                        Projectdeslabel.setText("Project: "+bug[0]);
                        bugDescription.setText("Issue Description: "+ bug[1]);
                        bugSeverity.setText("Issue Severity: "+ bug[2]);

                        String editDesvarone = bugSeverity.getText().replace("Issue Severity: ", "");
                        switch (editDesvarone) {
                            case "Critical" -> dottwo.setFill(Color.RED);
                            case "High" -> dottwo.setFill(Color.ORANGERED);
                            case "Medium" -> dottwo.setFill(Color.GOLD);
                            case "Low" -> dottwo.setFill(Color.LIMEGREEN);
                            default -> dottwo.setFill(Color.GRAY);
                        }
                    }
                }
        );



        // Main Scene Layout
        projectColumn.getStyleClass().add("column");
        bugColumn.getStyleClass().add("column");

        
        HBox rootmo = new HBox(sidebar, projectColumn, bugColumn, detailsColumn);
        rootmo.setStyle("-fx-background-color: #2e2f31;");
        VBox root = new VBox(title,rootmo);
        root.setSpacing(15);

        Scene scene = new Scene(root, 1010, 498);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("PatchFlow");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
