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

class Issue {
    private final String title;
    private final String priority;

    public Issue(String title, String priority) {
        this.title = title;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }
}

public class App extends Application {
    private final ObservableList<String> projects = FXCollections.observableArrayList();
    private final Map<String, List<String>> projectBugs = new HashMap<>();

    // Loads issues of respective projects from Database
    private ObservableList<Issue> loadIssuesFromDatabase(String project) {
       ObservableList<Issue> issues = FXCollections.observableArrayList();

       String sql = "SELECT issue, severity FROM projects WHERE project = ?";

       try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
                PreparedStatement ps = conn.prepareStatement(sql)) {

               ps.setString(1, project);
               ResultSet rs = ps.executeQuery();

           while (rs.next()) {
               String title = rs.getString("issue");
               String priority = rs.getString("severity");

               issues.add(new Issue(title, priority));
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return issues;
    }

    // Loads Projects form Database
    private void loadProjectsFromDB() {
        String sql = "SELECT DISTINCT project FROM projects";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            projects.clear(); // important: avoid duplicates

            while (rs.next()) {
                projects.add(rs.getString("project"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Loads issues's description and severity from Database
    private String[] loadBugDesp(String bugdesc) {
        String sql = "SELECT description, severity from projects WHERE issue = '"+bugdesc+"';";
        String[] issues = new String[2];

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();) {

            java.sql.ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                issues[0] = rs.getString("description");
                issues[1] = rs.getString("severity");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return issues;
    }

    // Save new project into Database
    private void saveProject(String projName,String bugtName,String despName,String sevName){
        String sql = "INSERT INTO projects VALUES('"+projName+"','"+bugtName+"','"+despName+"','"+sevName+"');";
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();
            loadProjectsFromDB();
            loadIssuesFromDatabase(projName);
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
            loadIssuesFromDatabase(selectedProject);
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
        sidebar.setPrefWidth(190);
        sidebar.setStyle("-fx-background-color: #2c2f33;");

        Label sidetitle = new Label("PatchFlow");
        sidetitle.setStyle("-fx-text-fill: white; -fx-font-size: 18;");

        Button analyticsBtn = new Button("Your Analytics");
        Button bugBtn = new Button("Add New Bug");

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
                dialogVbox.getChildren().addAll(projLabel,projtextField,bugLabel,bugtextField,despLabel,desptextField,sevLabel,combo_box,projbtn);

                projbtn.setOnAction(e -> {
                    String projName = projtextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    saveProject(projName,bugtName,despName,sevName);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 300);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #454648;");
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        // COLUMN 1 Project Explorer elements
          
        
        // COLUMN 3 Bug Details elements
        Label bugDescription = new Label("Select a Issue to see Description");
        bugDescription.setStyle("-fx-text-fill: white;");
        bugDescription.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        bugDescription.setWrapText(true);
        Label bugSeverity = new Label("Select a Issue to see Severity");
        bugSeverity.setStyle("-fx-text-fill: white;");
        bugSeverity.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        bugSeverity.setWrapText(true);

        sidebar.getChildren().addAll(sidetitle, analyticsBtn, bugBtn);




        // COLUMN 1: Project Explorer
        ListView<String> projectList = new ListView<>(projects);
        projectList.getItems().addAll(projectBugs.keySet());
        projectList.setPrefWidth(350);
        projectList.setFixedCellSize(60);

        projectList.setCellFactory(listview -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Label title = new Label(item);
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                VBox text = new VBox(4, title);
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
        projectColumn.setPrefWidth(300);



        // COLUMN 2: Bug Explorer

        ListView<Issue> issueListView = new ListView<>();
        String selectedProjectq = projectList.getSelectionModel().getSelectedItem();
        issueListView.setItems(loadIssuesFromDatabase(selectedProjectq));
        issueListView.setPrefWidth(350);
        issueListView.setFixedCellSize(75);

        // Custom cell rendering
        issueListView.setCellFactory(listView -> new ListCell<Issue>() {
            @Override
            protected void updateItem(Issue issue, boolean empty) {
                super.updateItem(issue, empty);

                if (empty || issue == null) {
                    setGraphic(null);
                    return;
                }

                Rectangle dot = new Rectangle(8, 8);
                dot.setArcWidth(8);
                dot.setArcHeight(8);

                switch (issue.getPriority()) {
                    case "High" -> dot.setFill(Color.ORANGERED);
                    case "Medium" -> dot.setFill(Color.GOLD);
                    case "Low" -> dot.setFill(Color.LIMEGREEN);
                    default -> dot.setFill(Color.GRAY);
                }

                Label title = new Label(issue.getTitle());
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                Label priority = new Label(issue.getPriority());
                priority.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 11px;");

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
        issulabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        issueListView.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        VBox bugColumn = new VBox(issulabel,issueListView);
        bugColumn.setPadding(new Insets(10));
        bugColumn.setSpacing(15);
        bugColumn.setPrefWidth(300);



        // COLUMN 3: Bug Details 
        Button remissue = new Button("Remove Issue");
        Button ediissue = new Button("Edit Description");

        remissue.setOnAction(e -> {
           String selectedProject = projectList.getSelectionModel().getSelectedItem();
           String descriptionText = bugDescription.getText();
           descriptionText = descriptionText.replace("Issue Description: ", "");
           String severityText = bugSeverity.getText();
           severityText = severityText.replace("Issue Severity: ","");
           removeBug(selectedProject, descriptionText, severityText);
        });

        // Hides remove issue button when issue not selected
        remissue.visibleProperty().bind(
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
                Issue selectedIssue = issueListView.getSelectionModel().getSelectedItem();
                String selectedBug = selectedIssue.getTitle();

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
        issudetlabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");

        VBox detailsColumn = new VBox(
            issudetlabel,
            bugDescription,
            bugSeverity,
            ediissue,
            remissue
            
        );
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(10);
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
                        String[] bug = loadBugDesp(newIssue.getTitle());
                        bugDescription.setText("Issue Description: "+ bug[0]);
                        bugSeverity.setText("Issue Severity: "+ bug[1]);
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

        Scene scene = new Scene(root, 1000, 498);
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
