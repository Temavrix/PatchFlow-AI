package com.patchflow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.scene.control.Alert;

public class AIService {

    private static String OPEN_API_KEY;
    private static String GEMINI_API_KEY;

    static {
        loadApiKeys();
    }

    private static void loadApiKeys() {

        String sql = "SELECT apiname, apikey FROM apikeys";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:Patchflow.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                String name = rs.getString("apiname");
                String key = rs.getString("apikey");

                if ("openrouter".equals(name)) {
                    OPEN_API_KEY = key;
                } 
                else if ("gemini".equals(name)) {
                    GEMINI_API_KEY = key;
                }
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 014");
            alert.setHeaderText(null);
            alert.setContentText("Error 014: Please ensure API Keys is entered in settings!");             
            alert.showAndWait();
        }
    }
    
    private static final String MODEL = "gemini-2.5-flash";
    private static final String MODELNEW = "gemini-3.5-flash";
    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String ENDPOINTTWO = "https://generativelanguage.googleapis.com/v1beta/models/"+ MODEL + ":generateContent?key=" + GEMINI_API_KEY;
    private static final String ENDPOINTTHREE = "https://generativelanguage.googleapis.com/v1beta/models/"+ MODELNEW + ":generateContent?key=" + GEMINI_API_KEY;



    public static String sendPromptTwoFlash(String prompt) throws Exception {

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject body = new JsonObject();
        body.add("contents", contents);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINTTWO))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!jsonResponse.has("candidates")) {
            return "API Error: " + jsonResponse;
        }

        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

        return candidates.get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }

    

    public static String sendPromptThreeFlash(String prompt) throws Exception {

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject body = new JsonObject();
        body.add("contents", contents);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINTTHREE))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!jsonResponse.has("candidates")) {
            return "API Error: " + jsonResponse;
        }

        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

        return candidates.get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }



    public static String sendPromptGemmaB(String prompt) throws Exception {
        
            // Create simple user message (OpenRouter format)
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
        
            JsonArray messages = new JsonArray();
            messages.add(message);
        
            // Create request body
            JsonObject body = new JsonObject();
            body.addProperty("model", "google/gemma-3n-e2b-it:free");
            body.add("messages", messages);
        
            // Enable reasoning
            JsonObject reasoning = new JsonObject();
            reasoning.addProperty("enabled", true);
            body.add("reasoning", reasoning);
        
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
        
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + OPEN_API_KEY)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost") // Optional
                    .header("X-Title", "MyJavaApp") // Optional
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
        
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
        
            JsonObject jsonResponse =
                    JsonParser.parseString(response.body()).getAsJsonObject();
        
            if (!jsonResponse.has("choices")) {
                return "API Error:\n" + jsonResponse;
            }
    
            return jsonResponse
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
    }



    public static String generateResponseGPTone(String prompt) throws Exception {
        
        // Create message content
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", prompt);

        JsonArray contentArray = new JsonArray();
        contentArray.add(textContent);

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.add("content", contentArray);

        JsonArray messages = new JsonArray();
        messages.add(message);

        // Create request body
        JsonObject body = new JsonObject();
        body.addProperty("model", "openai/gpt-oss-120b:free");
        body.add("messages", messages);

        // Enable reasoning (optional — remove if not needed)
        JsonObject reasoning = new JsonObject();
        reasoning.addProperty("enabled", true);
        body.add("reasoning", reasoning);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Authorization", "Bearer " + OPEN_API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost") // Optional
                .header("X-Title", "MyJavaApp") // Optional
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse =
                JsonParser.parseString(response.body()).getAsJsonObject();

        if (!jsonResponse.has("choices")) {
            return "API Error:\n" + jsonResponse;
        }

        return jsonResponse
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
   }



   public static String generateResponseGPTtwenty(String prompt) throws Exception {
        
        // Create message content
        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", prompt);

        JsonArray contentArray = new JsonArray();
        contentArray.add(textContent);

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.add("content", contentArray);

        JsonArray messages = new JsonArray();
        messages.add(message);

        // Create request body
        JsonObject body = new JsonObject();
        body.addProperty("model", "openai/gpt-oss-20b:free");
        body.add("messages", messages);

        // Enable reasoning (optional — remove if not needed)
        JsonObject reasoning = new JsonObject();
        reasoning.addProperty("enabled", true);
        body.add("reasoning", reasoning);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Authorization", "Bearer " + OPEN_API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost") // Optional
                .header("X-Title", "MyJavaApp") // Optional
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse =
                JsonParser.parseString(response.body()).getAsJsonObject();

        if (!jsonResponse.has("choices")) {
            return "API Error:\n" + jsonResponse;
        }

        return jsonResponse
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
   }
}
