package com.patchflow;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
        As a senior software engineer,
        The user has the following issue:

        ISSUE DESCRIPTION:
        %s

        Provide:
        1. Root cause analysis
        2. Step-by-step solution
        3. Suggested code improvement
        4. Best practices to avoid this in future

        CODE SNIPPET:
        %s

        """.formatted(bugdesc,snippet);
    }


    @Override
    public void start(Stage stage) {
    
        Label rootlabel = new Label("✦ AI (Beta)");
        rootlabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
    
        TextArea AIWindow = new TextArea();
        AIWindow.setWrapText(true);
        AIWindow.setPrefRowCount(20);
        AIWindow.setText("Click button to get AI response...");
        AIWindow.getStyleClass().add("dark-text-area");
        AIWindow.setEditable(false);


        ComboBox<String> AImenu = new ComboBox<>();
        AImenu.getItems().addAll("GPT-OSS-120B", "GPT-OSS-20B", "Gemini-2.5-flash", "Gemini-3.5-flash", "Gemma-3n-e2b-it");

        AImenu.setPromptText("Choose AI Model");

        Map<String, Image> iconMap = new HashMap<>();

        iconMap.put("GPT-OSS-120B", new Image(getClass().getResource("/icons/chatgpt.png").toExternalForm()));
        iconMap.put("GPT-OSS-20B", new Image(getClass().getResource("/icons/chatgpt.png").toExternalForm()));
        iconMap.put("Gemini-2.5-flash", new Image(getClass().getResource("/icons/gemini.png").toExternalForm()));
        iconMap.put("Gemini-3.5-flash", new Image(getClass().getResource("/icons/gemini.png").toExternalForm()));
        iconMap.put("Gemma-3n-e2b-it", new Image(getClass().getResource("/icons/gemini.png").toExternalForm()));


        AImenu.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    Image iconImage = iconMap.get(item);
                    ImageView icon = new ImageView(iconImage);
                    icon.setFitWidth(16);
                    icon.setFitHeight(16);
                    setGraphic(icon);
                }
            }
        });

        AImenu.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
            
                if (empty || item == null) {
                    setText("Select AI Model");
                    setGraphic(null);
                } else {
                    setText(item);
                    Image iconImage = iconMap.get(item);
                    ImageView icon = new ImageView(iconImage);
                    icon.setFitWidth(16);
                    icon.setFitHeight(16);
                    setGraphic(icon);
                }
            }
        });

        Button startMCP = new Button("Ask ✦ AI");
        startMCP.setStyle("-fx-background-color: #3c3c3e; -fx-text-fill: white; -fx-control-inner-background: #3c3c3e;");

        startMCP.setOnAction(e -> {
            String selected = AImenu.getValue();

            if (selected == null) return;

            switch (selected) {
                case "GPT-OSS-120B":
                    AIWindow.setText("Generating AI response...");
                    new Thread(() -> {
                        try {
                            String prompt = buildPrompt();
                            String response = AIService.generateResponseGPTone(prompt);
                        
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
                    break;
                case "GPT-OSS-20B":
                    AIWindow.setText("Generating AI response...");
                    new Thread(() -> {
                        try {
                            String prompt = buildPrompt();
                            String response = AIService.generateResponseGPTtwenty(prompt);
                        
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
                    break;
                case "Gemini-2.5-flash":
                    AIWindow.setText("Generating AI response...");
                    new Thread(() -> {
                        try {
                            String prompt = buildPrompt();
                            String response = GeminiService.sendPromptTwoFlash(prompt);
                        
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
                    break;
                case "Gemini-3.5-flash":
                    AIWindow.setText("Generating AI response...");
                    new Thread(() -> {
                        try {
                            String prompt = buildPrompt();
                            String response = GeminiService.sendPromptThreeFlash(prompt);
                        
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
                    break;
                case "Gemma-3n-e2b-it":
                    AIWindow.setText("Generating AI response...");
                    new Thread(() -> {
                        try {
                            String prompt = buildPrompt();
                            String response = AIService.sendPromptGemmaB(prompt);
                        
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
                    break;
            }
            

        AIWindow.setEditable(false);
        });
    
        VBox root = new VBox(rootlabel, AIWindow, AImenu, startMCP);
        root.setPadding(new Insets(20));
        root.setSpacing(10);
        root.setStyle("-fx-background-color: #2e2f31;");
    
        Scene scene = new Scene(root, 500, 550);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("✦ AI (Beta)");
        stage.getIcons().add(new Image("/icons/patchflowtrim.png"));
        stage.setScene(scene);
        stage.show();
    
    }

    public static void main(String[] args){
        launch(args);
    }

    
}