package com.patchflow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AIService {

    private static final String API_KEY = "OPENROUTER_API_KEY";
    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";


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
                    .header("Authorization", "Bearer " + API_KEY)
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
                .header("Authorization", "Bearer " + API_KEY)
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
                .header("Authorization", "Bearer " + API_KEY)
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
