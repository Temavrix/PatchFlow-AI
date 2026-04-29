package com.patchflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URI;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.Desktop;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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


public class Patchflow extends Application {
    // Declaring all required stages, variables and listmaps
    private static final String DB_URL = "jdbc:sqlite:Patchflow.db";
    String selectedIssues;
    private Stage addIssue;
    private Stage updateIssue;
    private Stage shareIssueStage;
    private Stage mcpwindow;
    private Stage settingStage;
    private final Object DB_LOCK = new Object();
    StackPane contentArea = new StackPane();
    HBox homeView;
    User user = null;
    private final ObservableList<String> projects = FXCollections.observableArrayList();
    private final Map<String, Integer> projectissues = new HashMap<>();
    ListView<String> projectList = new ListView<>(projects);
    ListView<Map<String, String>> issueListView = new ListView<>();
    String descriptext, codeSnippettext, projectTemp, languageTemp;
    Patchflow current = this;

    // Routine checks when starting the application
    public void routineChecks(String tableName){
        FirebaseService.init();
        try(Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);){
            if(!rs.next()) {
                String sql = """
                    CREATE TABLE IF NOT EXISTS projects (
                        project TEXT NOT NULL,
                        language TEXT NOT NULL,
                        issue TEXT NOT NULL,
                        description TEXT NOT NULL,
                        severity TEXT NOT NULL,
                        progress TEXT,
                        snippet TEXT);""";

                String sqlsec = """
                    CREATE TABLE IF NOT EXISTS apikeys ( 
                    apiname TEXT,
                    apikey TEXT);""";

                String insertDefaults = """
                    INSERT OR IGNORE INTO apikeys (apiname) VALUES
                    ('gemini'),
                    ('github'),
                    ('openrouter'),
                    ('fireemail'),
                    ('firepass'),
                    ('kafka');""";

                try {
                    stmt.execute(sql);
                    stmt.execute(sqlsec);
                    stmt.execute(insertDefaults);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                   alert.setTitle("Error 000");
                   alert.setContentText("Error 000: Patchflow DB init failed!");
                   alert.show();
                }
            } else {
                loadProjectsFromDB();
            }

            if(loadKafkaState()){
                new Thread(() -> {
                try {
                    new ProcessBuilder(
                        "wscript", "runKafka.vbs"
                ).start();

                } catch (Exception e) {
                    Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Error 01");
                    alert.setContentText("Error 01: Kafka startup scripts not found!");
                    alert.show();
                    });
                }
            }).start();
            }

        } catch (SQLException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 001");
            alert.setHeaderText(null);
            alert.setContentText("Error 001: Routine checks failed!!!");             
            alert.showAndWait();
        }
    }

    public static Map<String, String> getFirebaseCredentials() {
        String sql = "SELECT apiname, apikey FROM apikeys WHERE apiname IN ('fireemail', 'firepass')";
        Map<String, String> creds = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                creds.put(rs.getString("apiname"), rs.getString("apikey"));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Firebase cred Error");
            alert.setHeaderText(null);
            alert.setContentText("Error getting firebase creds");             
            alert.showAndWait();
        }

        return creds;
    }


    // Loads issues of respective projects from Database
    public ObservableList<Map<String, String>> loadIssuesFromDatabase(String project) {
       ObservableList<Map<String, String>> issues = FXCollections.observableArrayList();

       String sql = "SELECT issue, severity FROM projects WHERE project = ?";

       try (Connection conn = DriverManager.getConnection(DB_URL);
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
           Alert alert = new Alert(Alert.AlertType.INFORMATION);
           alert.setTitle("Error 002");
           alert.setHeaderText(null);
           alert.setContentText("Error 002: Issues loading failed!!!");             
           alert.showAndWait();
       }
       return issues;
    }

    // Loads Projects form Database
    public void loadProjectsFromDB() {
        String sql = "SELECT project, COUNT(*) AS issue_count FROM projects GROUP BY project ";
        projects.clear();
        projectissues.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String project = rs.getString("project");
                int count = rs.getInt("issue_count");
                projects.add(project);
                projectissues.put(project, count);
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 003");
            alert.setHeaderText(null);
            alert.setContentText("Error 003: Projects loading failed!!!");             
            alert.showAndWait();
        }
    }

    // Loads issues's details from Database
    private String[] loadBugDesp(String bugdesc) {
        String sql = "SELECT project, language, description, severity, snippet from projects WHERE issue = ?";
        String[] issues = new String[5];

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, bugdesc);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issues[0] = rs.getString("project");
                    issues[1] = rs.getString("language");
                    issues[2] = rs.getString("description");
                    issues[3] = rs.getString("severity");
                    issues[4] = rs.getString("snippet");
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 004");
            alert.setHeaderText(null);
            alert.setContentText("Error 004: Issues details loading failed!!!");             
            alert.showAndWait();
        }
        return issues;
    }

    // Save new issue into Database
    private void saveProject(String projName,String langName,String bugtName,String despName,String sevName,String progname,String codsnip){
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, projName);
            stmt.setString(2, langName);
            stmt.setString(3, bugtName);
            stmt.setString(4, despName);
            stmt.setString(5, sevName);
            stmt.setString(6, progname);
            stmt.setString(7, codsnip);
            stmt.executeUpdate();
            loadProjectsFromDB();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 005");
            alert.setHeaderText(null);
            alert.setContentText("Error 005: Saving of Issue failed!!!");             
            alert.showAndWait();
        }
    }

    // Remove issue from Database
    private void removeBug(String selectedProject, String SelectedIssue, String bugDescription, String bugSeverity){
        String sql = "DELETE FROM projects WHERE project = ? AND issue = ? AND description = ? AND severity = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selectedProject);
            stmt.setString(2, SelectedIssue);
            stmt.setString(3, bugDescription);
            stmt.setString(4, bugSeverity);

            stmt.executeUpdate();
            loadProjectsFromDB();
            refreshIssues();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 006");
            alert.setHeaderText(null);
            alert.setContentText("Error 006: Removal of Issue failed!!!");             
            alert.showAndWait();
        }
    }

    // Edit issue in Database
    public void editProject(String despName, String sevName, String codeName, String progName, String selectedBug){
        String sql = "UPDATE projects SET description = ?, severity = ?, snippet = ?, progress = ? WHERE issue = ?";
        try (
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, despName);
            stmt.setString(2, sevName);
            stmt.setString(3, codeName);
            stmt.setString(4, progName);
            stmt.setString(5, selectedBug);

            stmt.executeUpdate();
            loadBugDesp(selectedBug);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 007");
            alert.setHeaderText(null);
            alert.setContentText("Error 007: Updation of Issue failed!!!");             
            alert.showAndWait();
        }
    }

    // A function to refresh Issues if one is deleted
    public void refreshIssues() {

        String selectedProject = projectList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            issueListView.setItems(loadIssuesFromDatabase(selectedProject));
        } else {
            issueListView.getItems().clear();
        }
    }

    // Save information from settings UI in database
    public synchronized void saveSettingsNow(String gemName, String openrouteName, String githtName, String emailName, String passName) {

    String sql = "UPDATE apikeys SET apikey = ? WHERE apiname = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        conn.createStatement().execute("PRAGMA busy_timeout = 5000");
        conn.setAutoCommit(false);

        // updates...
        stmt.setString(1, gemName);
        stmt.setString(2, "gemini");
        stmt.executeUpdate();

        stmt.setString(1, githtName);
        stmt.setString(2, "github");
        stmt.executeUpdate();

        stmt.setString(1, openrouteName);
        stmt.setString(2, "openrouter");
        stmt.executeUpdate();

        stmt.setString(1, emailName);
        stmt.setString(2, "fireemail");
        stmt.executeUpdate();

        stmt.setString(1, passName);
        stmt.setString(2, "firepass");
        stmt.executeUpdate();

        conn.commit();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("API keys saved successfully!");             
        alert.showAndWait();

    } catch (Exception e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error saving settings");
        alert.setHeaderText(null);
        alert.setContentText("Error saving settings");             
        alert.showAndWait();
    }
}

    //load apikeys from database to show in settings UI
    private void loadApiKeys(TextField geminitextField, TextField openrotextField, TextField githubtextField, TextField emailtextField, TextField passtextField){
        String sql = "SELECT apiname, apikey FROM apikeys";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("apiname");
                String key = rs.getString("apikey");

                switch (name) {
                    case "gemini" -> geminitextField.setText(key);
                    case "openrouter" -> openrotextField.setText(key);
                    case "github" -> githubtextField.setText(key);
                    case "fireemail" -> emailtextField.setText(key);
                    case "firepass" -> passtextField.setText(key);
                }
            }


        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 009");
            alert.setHeaderText(null);
            alert.setContentText("Error 009: API loading failed!!!");             
            alert.showAndWait();
        }
    }

    private boolean loadKafkaState() {
        String sql = "SELECT apikey FROM apikeys WHERE apiname='kafka'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return "1".equals(rs.getString("apikey"));
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0090");
            alert.setHeaderText(null);
            alert.setContentText("Error 0090: Kafka state loading failed!!!");             
            alert.showAndWait();
        }
        return false;
    }

    private void updateKafkaState(boolean state) {
        String sql = "UPDATE apikeys SET apikey=? WHERE apiname='kafka'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            if (state) {
                ps.setString(1, "1");
            } else {
                ps.setNull(1, java.sql.Types.VARCHAR);
            }

            ps.executeUpdate();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0091");
            alert.setHeaderText(null);
            alert.setContentText("Error 0091: Kakfka state saving failed!!!");             
            alert.showAndWait();
        }
    }



    // Main Logic Code starts here
    @Override
    public void start(Stage stage) {
        routineChecks("projects");

        // Sidebar code
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(150);
        sidebar.setStyle("-fx-background-color: #2c2f33;");

        Label sidetitle = new Label("PatchFlow");
        sidetitle.setStyle("-fx-text-fill: white; -fx-font-size: 18;");

        Button bugBtn = new Button("Add Issue");
        bugBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        bugBtn.setPrefWidth(80);

        Button theBoardBtn = new Button("Board");
        theBoardBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        theBoardBtn.setPrefWidth(80);

        Button viewIssuesBtn = new Button("Issues");
        viewIssuesBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        viewIssuesBtn.setPrefWidth(80);

        Button teamBtn = new Button("Team");
        teamBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        teamBtn.setPrefWidth(80);

        Button analyticsBtn = new Button("Analytics");
        analyticsBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        analyticsBtn.setPrefWidth(80);

        Button githubtton = new Button("Github");
        githubtton.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        githubtton.setPrefWidth(80);

        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        settingsButton.setPrefWidth(80);

        // Button to return to current file
        viewIssuesBtn.setOnAction(e -> {
            contentArea.getChildren().setAll(homeView);
        });

        // Button to open board window
        theBoardBtn.setOnAction(e -> {
            Boardpanel Boardpanel = new Boardpanel();
            VBox boardpanelView = Boardpanel.getView();
            contentArea.getChildren().setAll(boardpanelView);
        });

        // Button to open team window
        teamBtn.setOnAction(e -> {
            Team Team = new Team(this);
            VBox team = Team.getView();
            contentArea.getChildren().setAll(team);
        });

        // Button to open analytics window
        analyticsBtn.setOnAction(e -> {
            Analytics analytics = new Analytics();
            VBox analyticsView = analytics.getView();
            contentArea.getChildren().setAll(analyticsView);
        });

        // Button to open settings window
        settingsButton.setOnAction(e -> {
            if(settingStage == null || !settingStage.isShowing()){
                settingStage = new Stage();

                Label geminiLabel = new Label("Gemini API: ");
                geminiLabel.setStyle("-fx-text-fill: white;");
                TextField geminitextField = new TextField();

                Label openrouterLabel = new Label("OpenRouter API: ");
                openrouterLabel.setStyle("-fx-text-fill: white;");
                TextField openrotextField = new TextField();

                Label githubLabel = new Label("Github Token: ");
                githubLabel.setStyle("-fx-text-fill: white;");
                TextField githubtextField = new TextField();

                Label fireinfo = new Label("Enter Email and Password to share and recieve issues:");
                fireinfo.setStyle("-fx-text-fill: white;");
                Label emailLabel = new Label("Your Email (For Sharing Issues):");
                emailLabel.setStyle("-fx-text-fill: white;");
                TextField emailtextField = new TextField();

                Label passLabel = new Label("Your Password (For Sharing Issues):");
                passLabel.setStyle("-fx-text-fill: white;");
                TextField passtextField = new TextField();

                Label kafkaLabel = new Label("Activate Kafka (Unstable): ");
                kafkaLabel.setStyle("-fx-text-fill: white;");
                kafkaLabel.setPadding(new Insets(5));
                StackPane kToggle = new StackPane();
                kToggle.setPrefSize(50, 25);

                Rectangle background = new Rectangle(50, 25);
                background.setArcWidth(25);
                background.setArcHeight(25);
                background.setStyle("-fx-fill: #555;");

                Circle thumb = new Circle(10);
                thumb.setTranslateX(-12);
                thumb.setStyle("-fx-fill: white;");

                kToggle.getChildren().addAll(background, thumb);
                final boolean[] isOn = {false};

                // Click handler
                kToggle.setOnMouseClicked(event -> {
                    isOn[0] = !isOn[0];
                    TranslateTransition transition = new TranslateTransition(Duration.millis(200), thumb);

                    if (isOn[0]) {
                        transition.setToX(12);
                        background.setStyle("-fx-fill: #4caf50;");
                    } else {
                        transition.setToX(-12);
                        background.setStyle("-fx-fill: #555;");
                    }
                    transition.play();
                });

                HBox kafkaToggleBox = new HBox();
                kafkaToggleBox.getChildren().addAll(kafkaLabel, kToggle);

                loadApiKeys(geminitextField, openrotextField, githubtextField, emailtextField, passtextField);

                boolean dbState = loadKafkaState();
                isOn[0] = dbState;
                if (dbState) {
                    thumb.setTranslateX(12);
                    background.setStyle("-fx-fill: #4caf50;");
                }

                Button registerFirebase = new Button("Register to share issues");
                registerFirebase.setOnAction(ev ->{
                    if(emailtextField.getText().isEmpty() || passtextField.getText().isEmpty()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Credentials");
                        alert.setHeaderText(null);
                        alert.setContentText("Please fill up both Email and Password to register.");
                        alert.showAndWait();
                        return;
                    }
                    user = FirebaseService.signUp(emailtextField.getText(), passtextField.getText());
                    if (user == null) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Registration failed: Email already exists");             
                        alert.showAndWait();
                    } else {
                        FirebaseService.saveUser(user);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Register success");
                        alert.setHeaderText(null);
                        alert.setContentText("Account registered successfully");             
                        alert.showAndWait();
                    }
                });

                Button savebtn = new Button("Save");
                VBox settingWindow = new VBox(10);
                settingWindow.setPadding(new Insets(10));
                settingWindow.getChildren().addAll(geminiLabel,geminitextField,openrouterLabel,openrotextField,githubLabel,githubtextField,fireinfo,emailLabel,emailtextField,
                    passLabel,passtextField,registerFirebase,kafkaToggleBox,savebtn);
                settingWindow.setStyle("-fx-background-color: #454648;");

                savebtn.setOnAction(ev -> {
                    savebtn.setDisable(true);

                    synchronized (DB_LOCK) {
                        String gemName = geminitextField.getText();
                        String openrouteName = openrotextField.getText();
                        String githtName = githubtextField.getText();
                        String emailName = emailtextField.getText();
                        String passName = passtextField.getText();

                        saveSettingsNow(gemName, openrouteName, githtName, emailName, passName);
                        updateKafkaState(isOn[0]);
                    }
                    savebtn.setDisable(false);
                    settingStage.close();
                });

                Scene settingScene = new Scene(settingWindow, 400, 460);
                settingStage.setScene(settingScene);
                settingStage.setTitle("Settings");
                settingStage.getIcons().add(new Image("/icons/patchflowtrim.png"));
                settingStage.show();
            } else {
                settingStage.toFront();
            }
        });

        // Button to open github window
        githubtton.setOnAction(e -> {
            Githubpanel githubWindow = new Githubpanel(this);
            VBox githubView = githubWindow.getView(stage);
            contentArea.getChildren().setAll(githubView);
        });

        // Button to add new issue
        bugBtn.setOnAction(e -> {
            if (addIssue == null || !addIssue.isShowing()){
                addIssue = new Stage();
                addIssue.initOwner(stage);
                VBox dialogVbox = new VBox(10);
                Label projLabel = new Label("Enter Your Project: ");
                projLabel.setStyle("-fx-text-fill: white;");
                TextField projtextField = new TextField();
            
                Label lanLabel = new Label("Enter Project's Language: ");
                lanLabel.setStyle("-fx-text-fill: white;");
                TextField lantextField = new TextField();
            
                Label bugLabel = new Label("Enter Issue Title: ");
                bugLabel.setStyle("-fx-text-fill: white;");
                TextField bugtextField = new TextField();

                Label despLabel = new Label("Enter Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextArea desptextField = new TextArea();
                desptextField.setPromptText("Enter your Issue Description here...");
                desptextField.setWrapText(true); 
                desptextField.setPrefRowCount(4);
                desptextField.setPrefWidth(300);

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
                sniptextArea.setPrefWidth(300);
                VBox snipbox = new VBox(sniplabel,sniptextArea);
                
                VBox colone = new VBox(2,projLabel,projtextField,lanLabel,lantextField,bugLabel,bugtextField,despLabel,desptextField);
                VBox coltwo = new VBox(19,chooseTime,snipbox);
                HBox colallign = new HBox(10,colone,coltwo);
            
                Button projbtn = new Button("Add New Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                Button aihelp = new Button("✦ AI Autofill");
                aihelp.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                HBox ctrlbutton = new HBox(10,projbtn,aihelp);

                dialogVbox.getChildren().addAll(colallign,ctrlbutton);
            
                projbtn.setOnAction(ev -> {
                    String projName = projtextField.getText();
                    String langName = lantextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    String sevName = combo_box.getValue();
                    String progname = progcombo_box.getValue();
                    String codsnip = sniptextArea.getText();

                    if(projName.isEmpty() || langName.isEmpty() || bugtName.isEmpty() || despName.isEmpty() || sevName == null || progname == null){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Fields");
                        alert.setHeaderText(null);
                        alert.setContentText("Please fill all the fields (Code snippet is optional).");
                        alert.showAndWait();
                        return;
                    }

                    saveProject(projName,langName,bugtName,despName,sevName,progname,codsnip);

                    if(loadKafkaState()){
                        String issueJson = String.format(
                            "{\"project\":\"%s\",\"language\":\"%s\",\"issue\":\"%s\",\"severity\":\"%s\"}",
                            projName, langName, bugtName, sevName
                        );
                        IssueProducer.sendIssue(issueJson);
                    }

                    projectList.getSelectionModel().select(projName);
                    refreshIssues();
                    addIssue.close();
                });

                aihelp.setOnAction(ex -> {
                    String langName = lantextField.getText();
                    String bugtName = bugtextField.getText();
                    String despName = desptextField.getText();
                    
                    if(langName.isEmpty() || bugtName.isEmpty()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Fields");
                        alert.setHeaderText(null);
                        alert.setContentText("Please fill Project's Language and Issue's Title.");
                        alert.showAndWait();
                        return;
                    }

                    desptextField.setText("Loading AI Response");
                    sniptextArea.setText("Loading AI Response");

                    new Thread(() -> {
                        try {
                            
                            String prompt = """
                                Just Give me a simple 10 line description of the following issue:

                                Language Used: %s
                                Issue Title: %s

                                Please do not regugitate what I have told and just give me the description
                                """.formatted(langName,bugtName);

                            String response = AIService.sendPromptTwoFlash(prompt);
                            desptextField.setText(response);
                        
                        } catch (Exception exx) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("AI Connection Problem");
                            alert.setHeaderText(null);
                            alert.setContentText("Please ensure Gemini API is entered in settings and there is stable internet connection.");
                            alert.showAndWait();
                            return;
                        }
                    }).start();

                    new Thread(() -> {
                        try {
                            String prompt = """
                                Just Give me a simple code snippet for the following issue:

                                Language Used: %s
                                Issue Title: %s
                                Issue Description: %s

                                Please do not regugitate what I have told and just give me the code snippet
                                """.formatted(langName,bugtName,despName);

                            String response = AIService.sendPromptTwoFlash(prompt);
                            sniptextArea.setText(response);
                        
                        } catch (Exception exx) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("AI Connection Problem");
                            alert.setHeaderText(null);
                            alert.setContentText("Please ensure Gemini API is entered in settings and there is stable internet connection.");
                            alert.showAndWait();
                            return;
                        }
                    }).start();
                });
            
                Scene dialogScene = new Scene(dialogVbox, 630, 300);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #222222;");
                addIssue.setScene(dialogScene);
                addIssue.setTitle("Issue Ticket ");
                addIssue.getIcons().add(new Image("/icons/patchflowtrim.png"));
                addIssue.show();
            } else {
                addIssue.toFront();
            }
        });

        Button supportBtn = new Button("Support Us");
        supportBtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        supportBtn.setPrefWidth(80);

        // Button for supporting us
        supportBtn.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://buymeacoffee.com/mahadhevha"));
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error 400");
                alert.setHeaderText(null);
                alert.setContentText("Error 400: Error opening Support page!!!");             
                alert.showAndWait();
            }
        });

        sidebar.getChildren().addAll(sidetitle,bugBtn,theBoardBtn,viewIssuesBtn,teamBtn,githubtton,analyticsBtn,settingsButton,supportBtn);




        // COLUMN 1: Project Explorer Column
        // Consists of map that contains all projects currently opened
        projectList.setItems(projects);
        projectList.setPrefWidth(350);
        issueListView.setPrefHeight(500);
        projectList.setFixedCellSize(75);

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
                row.setPadding(new Insets(10));
                row.setStyle(
                    "-fx-background-color: #3c3c3e;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0 0 0 1;" +  
                    "-fx-border-color: #808080;"
                );
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
                setStyle("-fx-background-color: #222222;");
            }
        });

        Label projlabel = new Label("Projects");
        projlabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        projectList.setStyle(
            "-fx-background-color: #222222;" +
            "-fx-control-inner-background: #222222;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );
        VBox projectColumn = new VBox(projlabel,projectList);
        projectColumn.setPadding(new Insets(10));
        projectColumn.setSpacing(15);
        projectColumn.setPrefWidth(250);



        // COLUMN 2: Bug Explorer Column
        // Consists of Map that contains all the issues and it's priority
        issueListView.setPrefWidth(300);
        issueListView.setPrefHeight(500);
        issueListView.setFixedCellSize(75);
        issueListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, String> issue, boolean empty) {
                super.updateItem(issue, empty);
                String borderColor;

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

                switch (priorityText) {
                    case "Critical" -> borderColor = "#FF0000";
                    case "High" -> borderColor = "#FF4500";
                    case "Medium" -> borderColor = "#FFD700";
                    case "Low" -> borderColor = "#32CD32";
                    default -> borderColor = "#808080";
                }

                Label title = new Label(titleText);
                title.setWrapText(true);
                title.prefWidthProperty().bind(issueListView.widthProperty().subtract(55));
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");

                Label priority = new Label(priorityText);
                priority.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 13px;");

                VBox text = new VBox(4, title, priority);
                HBox row = new HBox(10, dot, text);
                row.setPadding(new Insets(10));
                row.setStyle(
                    "-fx-background-color: #3c3c3e;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 0 0 0 1;" +  
                    "-fx-border-color: " + borderColor + ";"
                );
                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
                setStyle("-fx-background-color: #222222;");
            }
        });

        Label emptyLabel = new Label("No issues found");
        emptyLabel.setStyle(
            "-fx-text-fill: #cfcfcf;" +
            "-fx-font-size: 14;"
        );

        issueListView.setPlaceholder(emptyLabel);

        Label issulabel = new Label("Issues");
        issulabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        issueListView.setStyle(
            "-fx-background-color: #222222;" +
            "-fx-control-inner-background: #222222;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
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

        Button viewDescription = new Button("Description");
        viewDescription.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Button viewCode = new Button("Code Snippet");
        viewCode.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
        Button shareIssue = new Button("Assign Issue");
        shareIssue.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        Label currentViewing = new Label();
        currentViewing.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        TextArea viewingtextArea = new TextArea();
        viewingtextArea.setWrapText(true);
        viewingtextArea.getStyleClass().add("dark-text-area");
        viewingtextArea.setPrefRowCount(13);

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

        Button mcpButton = new Button("✦ AI (Beta)");
        mcpButton.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        viewDescription.setOnAction(e -> {
            currentViewing.setText("Description: ");
            viewingtextArea.setText(descriptext);
        });

        viewCode.setOnAction(e -> {
            currentViewing.setText("Code Snippet: ");
            viewingtextArea.setText(codeSnippettext);
        });

        shareIssue.setOnAction(e -> {
            if (shareIssueStage == null || !shareIssueStage.isShowing()){
                shareIssueStage = new Stage();
                shareIssueStage.initOwner(stage);
                VBox dialogVbox = new VBox(10);

                Map<String, String> selectedIssue = issueListView.getSelectionModel().getSelectedItem();
                String selectedBug = selectedIssue.get("title");

                String severityText = bugSeverity.getText().replace("Issue Severity: ","");

                Label despLabel = new Label("Share Your Issue");
                despLabel.setStyle("-fx-text-fill: white;");

                Label emailLabel = new Label("Enter Email to assign to:");
                emailLabel.setStyle("-fx-text-fill: white;");
                TextField emailfield = new TextField();

                Button projbtn = new Button("Assign Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

                dialogVbox.getChildren().addAll(despLabel,emailLabel,emailfield,projbtn);
                dialogVbox.setSpacing(10);
                
                projbtn.setOnAction(ev -> {
                try {
                    Map<String, String> creds = getFirebaseCredentials();

                    String fireEmail = creds.get("fireemail");
                    String firePass = creds.get("firepass");

                    // Save user locally for reuse
                    user = FirebaseService.login(fireEmail,firePass);
                    FirebaseService.saveUser(user);

                    FirebaseService.createIssue(
                        user, emailfield.getText(),
                        projectTemp, languageTemp,
                        selectedBug, descriptext,
                        codeSnippettext, severityText
                    );
                    shareIssueStage.close();

                } catch (Exception exc) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error creating Issue");
                    alert.setHeaderText(null);
                    alert.setContentText("Please check fireemail and firepass in settings");             
                    alert.showAndWait();
                }
            });

                Scene dialogScene = new Scene(dialogVbox, 300, 150);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #222222;");
                shareIssueStage.setScene(dialogScene);
                shareIssueStage.setTitle("Assign Issue");
                shareIssueStage.getIcons().add(new Image("/icons/patchflowtrim.png"));
                shareIssueStage.show();
            } else {
                shareIssueStage.toFront();
            }
        });

        // Remove Button to remove an issue
        remissue.setOnAction(e -> {
            String selectedProject = projectList.getSelectionModel().getSelectedItem();
            if (selectedProject == null) return;

            String descriptionText = descriptext;

            String severityText = bugSeverity.getText();
            severityText = severityText.replace("Issue Severity: ","");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Close Issue");
            alert.setContentText("Are you sure you want to close this issue?");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/icons/patchflowtrim.png"));
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                removeBug(selectedProject, selectedIssues, descriptionText, severityText);
                projectList.getSelectionModel().select(selectedProject);
                Projectdeslabel.setText("Select a Project");
                refreshIssues();
            }
            
        });

        currentViewing.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        viewingtextArea.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        viewDescription.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        viewCode.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        shareIssue.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Hides bug serverity when issue not selected
        bugSeverity.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Hides remove issue when issue not selected
        remissue.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Hides severity dot when issue not selected
        dottwo.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Hides edit button when issue not selected
        ediissue.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Hides AI button when issue not selected
        mcpButton.visibleProperty().bind(
        Projectdeslabel.textProperty()
                .isEqualTo("Select a Project")
                .not()
        );

        // Edit button to edit an issue
        ediissue.setOnAction(e -> {
            if (updateIssue == null || !updateIssue.isShowing()){
                updateIssue = new Stage();
                updateIssue.initOwner(stage);
                VBox dialogVbox = new VBox(10);
                Map<String, String> selectedIssue = issueListView.getSelectionModel().getSelectedItem();
                String selectedBug = selectedIssue.get("title");

                Label despLabel = new Label(" Edit Your Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextArea desptextField = new TextArea();
                desptextField.setText(descriptext);
                desptextField.setWrapText(true);
                desptextField.setPrefRowCount(5);

                Label codelabel = new Label("Edit Code Snippet: ");
                codelabel.setStyle("-fx-text-fill: white;");
                TextArea codetextfield = new TextArea();
                codetextfield.setText(codeSnippettext);
                codetextfield.setWrapText(true);
                codetextfield.setPrefRowCount(5);

                Label sevLabel = new Label("Choose Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");
                String bugCategories [] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugCategories));
                combo_box.setValue(bugSeverity.getText().replace("Issue Severity: ", ""));
                VBox severbox = new VBox(sevLabel,combo_box);

                Label progLabel = new Label("Choose Progress: ");
                progLabel.setStyle("-fx-text-fill: white;");
                String progCategories [] = { "To Do", "In Progress", "Code Review"};
                ComboBox<String> progcombo_box = new ComboBox<>(FXCollections.observableArrayList(progCategories));
                VBox progbox = new VBox(progLabel,progcombo_box);

                HBox chooseTime = new HBox(severbox,progbox);
                chooseTime.setSpacing(10);

                Button projbtn = new Button("Edit Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");
                dialogVbox.getChildren().addAll(despLabel,desptextField,codelabel,codetextfield,chooseTime,projbtn);

                projbtn.setOnAction(ev -> {
                    String despName = desptextField.getText();
                    String codeName = codetextfield.getText();
                    String sevName = combo_box.getValue();
                    String progName = progcombo_box.getValue();

                    if(despName.isEmpty() || sevName == null){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Missing Severity");
                        alert.setHeaderText(null);
                        alert.setContentText("Please select an issue severity.");
                        alert.showAndWait();
                        return;
                    }

                    editProject(despName,sevName,codeName,progName,selectedBug);
                    refreshIssues();

                    Platform.runLater(() -> {
                        for (Map<String, String> item : issueListView.getItems()) {
                            if (item.get("title").equals(selectedBug)) {
                                issueListView.getSelectionModel().select(item);
                                break;
                            }
                        }
                    });

                    updateIssue.close();
                });

                Scene dialogScene = new Scene(dialogVbox, 300, 350);
                dialogVbox.setPadding(new Insets(10));
                dialogVbox.setStyle("-fx-background-color: #222222;");
                updateIssue.setScene(dialogScene);
                updateIssue.setTitle("Update Issue");
                updateIssue.getIcons().add(new Image("/icons/patchflowtrim.png"));
                updateIssue.show();
            } else {
                updateIssue.toFront();
            }
        });

        // Button to pass values and open MCP (AI) Window
        mcpButton.setOnAction(e -> {
            if (mcpwindow == null || !mcpwindow.isShowing()){
                mcpwindow = new Stage();
                mcpwindow.initOwner(stage);
                try {
                    Mcpserver mcp = new Mcpserver(Projectdeslabel.getText(), selectedIssues, descriptext);
                    mcp.start(mcpwindow);
                    mcpwindow.setOnCloseRequest(ev -> mcpwindow = null);
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error 013");
                    alert.setHeaderText(null);
                    alert.setContentText("Error 013: Error opening MCP window!!!");             
                    alert.showAndWait();
                }
            } else {
                mcpwindow.toFront();
            }
        });

        Label issudetlabel = new Label("Issue Details: ");
        issudetlabel.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        
        HBox ledlight = new HBox(8,bugSeverity,dottwo);
        ledlight.setAlignment(Pos.CENTER_LEFT);
        HBox options = new HBox(8,ediissue,remissue,mcpButton);
        HBox viewing = new HBox(8, viewDescription, viewCode, shareIssue);

        VBox detailsColumn = new VBox(issudetlabel,Projectdeslabel,viewing,currentViewing,viewingtextArea,ledlight,options);
        detailsColumn.setPadding(new Insets(10));
        detailsColumn.setSpacing(10);
        detailsColumn.setPrefWidth(400);
        detailsColumn.setPrefHeight(500);

        // Fetching selected project and loading project's issues
        projectList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldProject, newProject) -> {
                if (newProject != null) {
                    issueListView.setItems(loadIssuesFromDatabase(newProject));
                }
            }
        );

        // Loads issue details and displays it in UI
        issueListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldIssue, newIssue) -> {
                if (newIssue != null) {
                    selectedIssues = newIssue.get("title");
                    String[] bug = loadBugDesp(newIssue.get("title"));
                    projectTemp = bug[0];
                    languageTemp = bug[1];
                    Projectdeslabel.setText("Project: "+ projectTemp + "\nLanguage: "+ languageTemp);
                    Projectdeslabel.setEditable(false);
                    descriptext = bug[2];
                    viewingtextArea.setText(bug[2]);
                    viewingtextArea.setEditable(false);
                    currentViewing.setText("Description: ");

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
                        codeSnippettext = "";
                    } else{
                        codeSnippettext = bug[4];
                    }
                }
            }
        );



        // Main Scene Layout
        projectColumn.getStyleClass().add("column");
        bugColumn.getStyleClass().add("column");

        contentArea.setPrefWidth(950);
        homeView = new HBox(projectColumn, bugColumn, detailsColumn);
        contentArea.getChildren().add(homeView);
        
        HBox rootmo = new HBox(sidebar, contentArea);
        VBox root = new VBox(rootmo);
        root.setSpacing(15);
        root.setStyle("-fx-background-color: #222222;");

        Scene scene = new Scene(root, 1010, 505);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
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
