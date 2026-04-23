package com.patchflow;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;
import com.google.gson.*;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class FirebaseService {
    private static final String API_KEY = "AIzaSyCMwKo2_1m6ZFnt12tu75WPqNWlh8n2gSU";

    // 🔹 INIT
    public static void init() {
        try {
            FileInputStream serviceAccount = new FileInputStream("C:\\\\Users\\\\Mahadhevha\\\\Downloads\\\\serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 1000");
            alert.setHeaderText(null);
            alert.setContentText("Error 1000");             
            alert.showAndWait();
        }
    }

    public static User signUp(String email, String password) {
        try {
            URL url = java.net.URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{"
                    + "\"email\":\"" + email + "\"," 
                    + "\"password\":\"" + password + "\"," 
                    + "\"returnSecureToken\":true"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            InputStream stream;
            int status = conn.getResponseCode();
            if (status >= 400) {
                stream = conn.getErrorStream(); // read error response
            } else {
                stream = conn.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder responseBuilder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            String response = responseBuilder.toString();
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();

            return new User(
                    obj.get("localId").getAsString(),
                    obj.get("email").getAsString(),
                    obj.get("idToken").getAsString()
            );

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 1001");
            alert.setHeaderText(null);
            alert.setContentText("Error 1001: Email already in use!!!");             
            alert.showAndWait();
            return null;
        }
    }

    // 🔹 LOGIN
    public static User login(String email, String password) {
        try {
            URL url = java.net.URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{"
                    + "\"email\":\"" + email + "\","
                    + "\"password\":\"" + password + "\","
                    + "\"returnSecureToken\":true"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            InputStream stream;
            int status = conn.getResponseCode();
            if (status >= 400) {
                stream = conn.getErrorStream(); // read error response
            } else {
                stream = conn.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            String response = responseBuilder.toString();
            if (response == null || response.isEmpty()) {
                System.out.println("Empty response from Firebase");
                return null;
            }

            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            if (obj.has("error")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error 1002");
                alert.setHeaderText(null);
                alert.setContentText("Error 1002: Login failed!!!");             
                alert.showAndWait();
                return null;
            }

            return new User(
                    obj.get("localId").getAsString(),
                    obj.get("email").getAsString(),
                    obj.get("idToken").getAsString()
            );

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 1003");
            alert.setHeaderText(null);
            alert.setContentText("Error 1003");             
            alert.showAndWait();
            return null;
        }
    }

    // 🔹 SAVE USER
    public static void saveUser(User user) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> data = new HashMap<>();
            data.put("uid", user.uid);
            data.put("email", user.email);
            db.collection("users").document(user.uid).set(data);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error 1004");
            alert.setHeaderText(null);
            alert.setContentText("Error 1004");             
            alert.showAndWait();
        }
    }

    // 🔹 GET UID FROM EMAIL
    public static String getUidByEmail(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .whereEqualTo("email", email)
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        if (!docs.isEmpty()) {
            return docs.get(0).getString("uid");
        }
        return null;
    }

    // 🔹 CREATE ISSUE
    public static void createIssue(User assigner, String assignedEmail,String project, String language,String title, String desc, String code,String severity) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        String assignedUid = getUidByEmail(assignedEmail);

        if (assignedUid == null) {
            System.out.println("User not found!");
            return;
        }

        Map<String, Object> issue = new HashMap<>();
        issue.put("project", project);
        issue.put("language", language);
        issue.put("title", title);
        issue.put("description", desc);
        issue.put("severity", severity);
        issue.put("codeSnippet", code);
        issue.put("status", "OPEN");

        issue.put("assignedToUid", assignedUid);
        issue.put("assignedToEmail", assignedEmail);
        issue.put("assignedByUid", assigner.uid);
        issue.put("assignedByEmail", assigner.email);

        issue.put("createdAt", System.currentTimeMillis());
        ApiFuture<DocumentReference> future = db.collection("issues").add(issue);
        DocumentReference docRef = future.get(); // waits for completion

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucess");
        alert.setHeaderText(null);
        alert.setContentText("Issue created with ID: " + docRef.getId());             
        alert.showAndWait();
    }

    // 🔹 FETCH ISSUES
    public static void fetchIssues(User user) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection("issues")
                .whereEqualTo("assignedToUid", user.uid)
                .whereEqualTo("status", "OPEN")
                .get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        for (QueryDocumentSnapshot doc : docs) {
            System.out.println("ID: " + doc.getId());
            System.out.println(doc.getData());
            System.out.println("-------------------");
        }
    }

    // 🔹 MARK DONE
    public static void markDone(String issueId) {
        Objects.requireNonNull(issueId, "issueId must not be null");
        Firestore db = FirestoreClient.getFirestore();

        db.collection("issues")
                .document(issueId)
                .update("status", "DONE");
    }
}