package com.example.patchflow;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;


@RestController
public class AIController {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String MODELNEW = "gemini-3.5-flash";
    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    //private static final String ENDPOINTTHREE = "https://generativelanguage.googleapis.com/v1beta/models/"+ MODELNEW + ":generateContent?key=" + "${app.frontend.gemini}";

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.frontend.gemini}")
    private String geminiApiKey;

    public String sendPromptTwoFlash(String prompt) throws Exception {

        String ENDPOINTTWO =
        "https://generativelanguage.googleapis.com/v1beta/models/"
        + MODEL
        + ":generateContent?key="
        + geminiApiKey;

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

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ENDPOINTTWO)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();

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


    @PostMapping("/api/message")
    public String receiveMessage(
            @RequestBody MessageRequest request) throws Exception{
        
                try{
                    String prompt = """
                    Just Give me a simple 5 line description of the following issue:

                    Issue Title: %s

                    Please do not regugitate what I have told and just give me the description
                    """.formatted(request.getMessage());

                    return sendPromptTwoFlash(prompt);

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
    }
}