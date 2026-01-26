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
import javafx.stage.Modality;
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



    @Override
    public void start(Stage stage) {

        Label title = new Label("  Welcome To PatchFlow");
        title.setLayoutX(30);
        loadProjectsFromDB();



        // COLUMN 1: Project Explorer
        ListView<String> projectList = new ListView<>(projects);
        projectList.getItems().addAll(projectBugs.keySet());
        Button btn = new Button("Add New Project");

        btn.setOnAction(
        new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
                VBox dialogVbox = new VBox(20);
                TextField textField = new TextField();

                Button projbtn = new Button("Add Project");
                dialogVbox.getChildren().add(textField);
                dialogVbox.getChildren().add(projbtn);

                projbtn.setOnAction(e -> {
                    String projectName = textField.getText();
                    //saveProject(projectName);
                    dialog.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 90);
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        VBox projectColumn = new VBox(
                new Label("Projects"),btn,projectList
        );
        projectColumn.setPadding(new Insets(10));
        projectColumn.setSpacing(10);
        projectColumn.setPrefWidth(300);



        // COLUMN 2: Bug Explorer
        bugList = new ListView<>();
        Button bugbtn = new Button("Add New Bug");

        bugbtn.setOnAction(
        new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
                VBox dialogVbox = new VBox(20);
                TextField textField = new TextField();

                Button bugsbtn = new Button("Add Bug");
                dialogVbox.getChildren().add(textField);
                dialogVbox.getChildren().add(bugsbtn);

                Scene dialogScene = new Scene(dialogVbox, 300, 90);
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });

        VBox bugColumn = new VBox(
                new Label("Bugs"),bugbtn,bugList
        );
        bugColumn.setPadding(new Insets(10));
        bugColumn.setSpacing(10);
        bugColumn.setPrefWidth(300);



        // COLUMN 3: Bug Details
        Label bugDescription = new Label("Select a bug to see Description");
        bugDescription.setWrapText(true);

        Label bugSeverity = new Label("Select a bug to see Severity");
        bugSeverity.setWrapText(true);

        VBox detailsColumn = new VBox(
                new Label("Bug Details"),
                bugDescription,
                bugSeverity
        );
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(10);
        detailsColumn.setPrefWidth(400);



        // INTERACTIONS
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
                    bugDescription.setText("Bug Description: "+ bug[0]);
                    bugSeverity.setText("Bug Severity: "+ bug[1]);
            }
        });



        // MAIN LAYOUT
        projectColumn.getStyleClass().add("column");
        bugColumn.getStyleClass().add("column");

        HBox rootmo = new HBox(projectColumn, bugColumn, detailsColumn);
        VBox root = new VBox(title,rootmo);
        root.setSpacing(15);


        Scene scene = new Scene(root, 1000, 520);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("PatchFlow");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
