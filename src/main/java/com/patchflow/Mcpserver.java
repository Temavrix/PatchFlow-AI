package com.patchflow;

import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Mcpserver extends Application {

    public static String bugdesc;
    public static String snippet;

    public Mcpserver(String bugdesc, String snippet) {
        this.bugdesc = bugdesc;
        this.snippet = snippet;
    }

    public static String buildPrompt() {

        return """
        You are a senior software engineer.

        The user has the following issue:

        ISSUE DESCRIPTION:
        %s

        CODE SNIPPET:
        %s

        Provide:
        1. Root cause analysis
        2. Step-by-step solution
        3. Suggested code improvement
        4. Best practices to avoid this in future
        """.formatted(bugdesc,snippet);
    }

    @Override
    public void start(Stage stage) {
    
        Label rootlabel = new Label("Patcher AI ✦ (Beta)");
        rootlabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
    
        TextArea AIWindow = new TextArea();
        AIWindow.setWrapText(true);
        AIWindow.setPrefRowCount(20);
        AIWindow.setText("Click button to get AI response...");
        AIWindow.getStyleClass().add("dark-text-area");
        AIWindow.setEditable(false);

        Button startMCP = new Button("Ask Patcher ✦");
        startMCP.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        startMCP.setOnAction(e -> {
            AIWindow.setText("Generating AI response...");
            new Thread(() -> {
            try {
                String prompt = buildPrompt();
                String response = GeminiService.generateResponse(prompt);
            
                // Update UI safely
                javafx.application.Platform.runLater(() ->
                        AIWindow.setText(response)
                );
            
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        AIWindow.setText("Error: " + ex.getMessage())
                );
            }
        }).start();

        AIWindow.setEditable(false);
        });
    
        VBox root = new VBox(rootlabel, AIWindow, startMCP);
        root.setPadding(new Insets(20));
        root.setSpacing(10);
        root.setStyle("-fx-background-color: #2e2f31;");
    
        Scene scene = new Scene(root, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Patcher AI (Beta)");
        stage.getIcons().add(new Image("/icons/patchflowtrim.png"));
        stage.setScene(scene);
        stage.show();
    
    }

    public static void main(String[] args){
        launch(args);
    }

    
}