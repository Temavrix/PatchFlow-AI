import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.event.EventHandler;

public class App extends Application {
    private final ObservableList<String> projects = FXCollections.observableArrayList();
    private final Map<String, List<String>> projectBugs = new HashMap<>();
    private ListView<String> bugList = new ListView<>();

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

    // Loads issues in choosen project from Database
    private void loadBugsFromDB(String project) {
        String sql = "SELECT issue FROM projects WHERE project = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, project);
            ResultSet rs = ps.executeQuery();

            bugList.getItems().clear();

            while (rs.next()) {
                bugList.getItems().add(rs.getString("issue"));
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
            loadBugsFromDB(projName);
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
            loadBugsFromDB(selectedProject);
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
                TextField projtextField = new TextField();
                Label bugLabel = new Label("Type Your Issue: ");
                TextField bugtextField = new TextField();
                Label despLabel = new Label("Type Your Description: ");
                TextField desptextField = new TextField();
                Label sevLabel = new Label("Type Your Severity: ");
                TextField sevtextField = new TextField();

                Button projbtn = new Button("Add New Issue");
                dialogVbox.getChildren().addAll(projLabel,projtextField,bugLabel,bugtextField,despLabel,desptextField,sevLabel,sevtextField,projbtn);

                projbtn.setOnAction(e -> {
                    String projName = projtextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = sevtextField.getText();
                    saveProject(projName,bugtName,despName,sevName);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 290);
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        // COLUMN 1 Project Explorer elements
        ListView<String> projectList = new ListView<>(projects);
        projectList.getItems().addAll(projectBugs.keySet());   
        
        // COLUMN 3 Bug Details elements
        Label bugDescription = new Label("Select a Issue to see Description");
        bugDescription.setWrapText(true);
        Label bugSeverity = new Label("Select a Issue to see Severity");
        bugSeverity.setWrapText(true);

        sidebar.getChildren().addAll(sidetitle, analyticsBtn, bugBtn);




        // COLUMN 1: Project Explorer
        VBox projectColumn = new VBox(new Label("Projects"),projectList);
        projectColumn.setPadding(new Insets(10));
        projectColumn.setSpacing(15);
        projectColumn.setPrefWidth(300);



        // COLUMN 2: Bug Explorer
        bugList = new ListView<>();
        VBox bugColumn = new VBox(new Label("Issues"),bugList);
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
                String selectedBug = bugList.getSelectionModel().getSelectedItem();

                Label despLabel = new Label(" Edit Your Description: ");
                TextField desptextField = new TextField();
                String editDesvar = bugDescription.getText().replace("Issue Description: ", "");
                desptextField.setText(editDesvar);

                Label sevLabel = new Label(" Edit Your Severity: ");
                TextField sevtextField = new TextField();
                String editSevVar = bugSeverity.getText().replace("Issue Severity: ", "");
                sevtextField.setText(editSevVar);

                Button projbtn = new Button("Edit Issue");
                dialogVbox.getChildren().addAll(despLabel,desptextField,sevLabel,sevtextField,projbtn);

                projbtn.setOnAction(e -> {
                    String despName = desptextField.getText();
                    String sevName = sevtextField.getText();
                    editProject(despName,sevName,selectedBug);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 160);
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        VBox detailsColumn = new VBox(
            new Label("Issue Details: "),
            bugDescription,
            bugSeverity,
            remissue,
            ediissue
        );
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(10);
        detailsColumn.setPrefWidth(400);



        // Fetching selected items for interactions
        projectList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldProject, newProject) -> {
                if (newProject != null) {
                    loadBugsFromDB(newProject);
                }
            }
        );

        bugList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String[] bug = loadBugDesp(newVal);
                    bugDescription.setText("Issue Description: "+ bug[0]);
                    bugSeverity.setText("Issue Severity: "+ bug[1]);
            }
        });



        // Main Scene Layout
        projectColumn.getStyleClass().add("column");
        bugColumn.getStyleClass().add("column");

        HBox rootmo = new HBox(sidebar,projectColumn, bugColumn, detailsColumn);
        VBox root = new VBox(title,rootmo);
        root.setSpacing(15);

        Scene scene = new Scene(root, 1000, 520);
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
