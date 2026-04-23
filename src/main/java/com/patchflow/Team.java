package com.patchflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.ListView;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import javafx.scene.image.Image;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Team {
    private static final String DB_URL = "jdbc:sqlite:Patchflow.db";
    Map<String, String> creds = getFirebaseCredentials();
    private Stage addIssue;
    private Patchflow patchflow;
    User user = null;

    public Team(Patchflow patchflow) {
        this.patchflow = patchflow;
    }

    public static Map<String, Map<String, String>> fetchIssues(User user) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Map<String, String>> issuesMap = new HashMap<>();
        ApiFuture<QuerySnapshot> future = db.collection("issues")
                .whereEqualTo("assignedToUid", user.uid)
                .get();
        QuerySnapshot snapshot = future.get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String, String> issueData = new HashMap<>();
            String id = doc.getId();

            issueData.put("project", doc.getString("project"));
            issueData.put("language", doc.getString("language"));
            issueData.put("title", doc.getString("title"));
            issueData.put("description", doc.getString("description"));
            issueData.put("severity", doc.getString("severity"));
            issueData.put("status", doc.getString("status"));
            issueData.put("codeSnippet", doc.getString("codeSnippet"));

            issuesMap.put(id, issueData);
        }

        return issuesMap;
    }

    private void saveToLocalDB(String projName,String langName, String bugtName,String despName,String sevName, String progname,String codsnip) {
        String sql = "INSERT INTO projects VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projName);
            stmt.setString(2, langName);
            stmt.setString(3, bugtName);
            stmt.setString(4, despName);
            stmt.setString(5, sevName);
            stmt.setString(6, progname);
            stmt.setString(7, codsnip);
            stmt.executeUpdate();
            patchflow.loadProjectsFromDB();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 0162");
            alert.setHeaderText(null);
            alert.setContentText("Error 0162: Adding of Issue failed!");             
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
            e.printStackTrace();
        }

        return creds;
    }

    public VBox getView() {

        String fireEmail = creds.get("fireemail");
        String firePass = creds.get("firepass");

        user = FirebaseService.login(fireEmail, firePass);
        FirebaseService.saveUser(user);

        ObservableList<String> issueTitles = FXCollections.observableArrayList();
        Map<String, Map<String, String>> issuesMap = new HashMap<>();

        ListView<String> issueList = new ListView<>(issueTitles);
        Label issueDetails = new Label("");
        issueDetails.setWrapText(true);
        issueDetails.setStyle("-fx-background-color: #2e2f31; -fx-text-fill: white; -fx-control-inner-background: #2e2f31;");

        // Load from Firebase
        try {
            issuesMap.putAll(fetchIssues(user));
            issueTitles.addAll(issuesMap.keySet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // LEFT SIDE LIST
        issueList.setMaxWidth(300);

        issueList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Map<String, String> issue = issuesMap.get(newVal);

                issueDetails.setText(
                    "\nProject: " + issue.get("project") +
                    "\n\nLanguage: " + issue.get("language") +
                    "\n\nTitle: " + issue.get("title") +
                    "\n\nDescription:\n" + issue.get("description") +
                    "\n\nSeverity: " + issue.get("severity") +
                    "\n\nStatus: " + issue.get("status") +
                    "\n\nCode:\n" + issue.get("codeSnippet")
                );
            }
        });

        issueList.setStyle(
            "-fx-background-color: #2e2f31;" +
            "-fx-control-inner-background: #2e2f31;"
        );

        issueList.getStyleClass().add("column");
        issueList.setMinHeight(470);
        

        // RIGHT SIDE SCROLL
        ScrollPane scrollPane = new ScrollPane(issueDetails);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2e2f31; -fx-text-fill: white; -fx-control-inner-background: #2e2f31;");

        scrollPane.visibleProperty().bind(
        issueDetails.textProperty()
                .isEqualTo("")
                .not()
        );

        // SAVE BUTTON
        Button saveLocal = new Button("Save to Workspace");
        saveLocal.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white;");

        saveLocal.visibleProperty().bind(
        issueDetails.textProperty()
                .isEqualTo("")
                .not()
        );

        saveLocal.setOnAction(e -> {
            if (addIssue == null || !addIssue.isShowing()){
                addIssue = new Stage();
                String selected = issueList.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                Map<String, String> issue = issuesMap.get(selected);

                VBox dialogVboxZero = new VBox(10);
                VBox dialogVboxOne = new VBox(10);
                HBox dialogVboxTwo = new HBox(10);
                VBox dialogVboxThree = new VBox(10);

                Label projLabel = new Label("Project Title: ");
                projLabel.setStyle("-fx-text-fill: white;");
                TextField projtextField = new TextField(issue.get("project"));

                Label langLabel = new Label("Language: ");
                langLabel.setStyle("-fx-text-fill: white;");
                TextField langtextField = new TextField(issue.get("language"));

                Label titLabel = new Label("Issue Title: ");
                titLabel.setStyle("-fx-text-fill: white;");
                TextField tittextField = new TextField(issue.get("title"));

                Label despLabel = new Label("Issue Description: ");
                despLabel.setStyle("-fx-text-fill: white;");
                TextArea desptextArea = new TextArea(issue.get("description"));
                desptextArea.setWrapText(true); 
                desptextArea.setPrefRowCount(9);

                dialogVboxZero.getChildren().addAll(projLabel,projtextField,langLabel,langtextField,titLabel,tittextField,despLabel,desptextArea);

                Label sniplabel = new Label("Enter Code Snippet (Optional): ");
                sniplabel.setStyle("-fx-text-fill: white;");
                TextArea sniptextArea = new TextArea(issue.get("codeSnippet"));
                sniptextArea.setWrapText(true); 
                sniptextArea.setPrefRowCount(9);

                Label sevLabel = new Label("Choose Your Severity: ");
                sevLabel.setStyle("-fx-text-fill: white;");
                String bugCategories [] = { "Low", "Medium", "High", "Critical"};
                ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(bugCategories));
                combo_box.setValue(issue.get("severity"));
                VBox severbox = new VBox(sevLabel,combo_box);

                Label progLabel = new Label("Choose Progress: ");
                progLabel.setStyle("-fx-text-fill: white;");
                String progCategories [] = { "To Do", "In Progress", "Code Review"};
                ComboBox<String> progcombo_box = new ComboBox<>(FXCollections.observableArrayList(progCategories));
                VBox progbox = new VBox(progLabel,progcombo_box);

                HBox chooseTime = new HBox(severbox,progbox);
                chooseTime.setSpacing(10);

                dialogVboxOne.getChildren().addAll(sniplabel,sniptextArea,chooseTime);

                Button projbtn = new Button("Add New Issue");
                projbtn.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

                projbtn.setOnAction(ev -> {
                    String projName = projtextField.getText();
                    String langName = langtextField.getText();
                    String bugtName = tittextField.getText();
                    String despName = desptextArea.getText();
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
                    saveToLocalDB(projName,langName,bugtName,despName,sevName,progname,codsnip);
                    patchflow.refreshIssues();
                    addIssue.close();
                });

                //saveToLocalDB(issue);
                dialogVboxTwo.getChildren().addAll(dialogVboxZero,dialogVboxOne);
                dialogVboxThree.getChildren().addAll(dialogVboxTwo,projbtn);

                Scene dialogScene = new Scene(dialogVboxThree, 700, 400);
                dialogVboxTwo.setStyle("-fx-background-color: #454648;");
                dialogVboxThree.setStyle("-fx-background-color: #454648;");
                dialogVboxThree.setPadding(new Insets(10));
                addIssue.setScene(dialogScene);
                addIssue.getIcons().add(new Image("/icons/patchflowtrim.png"));
                addIssue.show();
            } else {
                addIssue.toFront();
            }
        });

        VBox rightPanel = new VBox(10, scrollPane, saveLocal);
        HBox mainLayout = new HBox(10, issueList, rightPanel);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setSpacing(10);

        VBox layout = new VBox(mainLayout);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #2e2f31;");

        return layout;
    }
    
}
