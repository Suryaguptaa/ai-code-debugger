package com.debugger.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LlmClient {

    // Using Gemini 1.5 Flash (Free tier, fast)
    // We are adding "-001" to be specific, or you can try "gemini-pro" if this fails
    // We are switching to "gemini-2.0-flash" which is explicitly listed in your access log
//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    // Switching to Gemini 2.5 Flash (found in your access list)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LlmClient() {
        // Retrieve key from environment variable for security
        this.apiKey = System.getenv("GEMINI_KEY");

        // If the key is missing, throw a clear error so you know what's wrong
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("❌ Error: GEMINI_KEY environment variable is not set. Please set it in your terminal.");
        }

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Renamed 'exception' to 'userDescription' since it might be empty now
    public String analyzeBug(String userDescription, String codeContext) throws IOException, InterruptedException {
        String prompt = createPrompt(userDescription, codeContext);
        // ... rest stays the same ...
        String jsonPayload = createJsonPayload(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API Error: " + response.statusCode() + " -> " + response.body());
        }

        return extractResponseText(response.body());
    }

    private String createPrompt(String input, String code) {
        String taskDescription = (input == null || input.isEmpty())
                ? "Scan this code for ALL syntax errors, logical bugs, and runtime crashes."
                : "Fix this specific issue: " + input;

        return """
            You are a Senior Java Engineer.
            Task: %s
            
            Source Code:
            %s
            
            Return a JSON ARRAY of errors. Format:
            [
              {
                "explanation": "Brief explanation of bug #1",
                "lineNumber": 10, 
                "fixedCode": "Corrected code for line 10"
              },
              {
                "explanation": "Brief explanation of bug #2",
                "lineNumber": 15, 
                "fixedCode": "Corrected code for line 15"
              }
            ]
            """.formatted(taskDescription, code);
    }

    private String createJsonPayload(String prompt) {
        // Escaping newlines and quotes for valid JSON
        String safePrompt = prompt.replace("\n", "\\n").replace("\"", "\\\"");
        return """
            {
              "contents": [{
                "parts": [{
                  "text": "%s"
                }]
              }]
            }
            """.formatted(safePrompt);
    }

    private String extractResponseText(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);
        // Navigate the JSON: candidates -> [0] -> content -> parts -> [0] -> text
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    // 🛠️ DIAGNOSTIC METHOD
    public void listAvailableModels() {
        try {
            String listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(listUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 API Key Access Check:");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Available Models (Raw Output): " + response.body());
        } catch (Exception e) {
            System.err.println("Failed to list models: " + e.getMessage());
        }
    }

}