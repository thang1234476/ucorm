package com.example.ucorm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class AiReplyService {

        @Value("${gemini.api.key}")
        private String apiKey;

        @Value("${gemini.model}")
        private String model;

        @Value("${gemini.url}")
        private String baseUrl;

        private final ObjectMapper objectMapper = new ObjectMapper();

        public Map<String, String> generateReplies(String reviewText) {

                String prompt = "You are a professional customer service manager for a local business.\n" +
                                "A customer left this review for your location: \"" + reviewText + "\"\n\n" +
                                "Analyze the context of the review to determine the type of business, then generate 3 DIFFERENT, SHORT, and CONCISE reply options tailored to that context.\n"
                                +
                                "Return ONLY strictly valid JSON, no markdown, no code block, no extra text.\n\n" +
                                "{\n" +
                                "  \"standardReply\": \"short professional and polite reply addressing their specific feedback\",\n"
                                +
                                "  \"friendlyReply\": \"short warm and engaging reply with emoji, matching the tone of their review\",\n"
                                +
                                "  \"recoveryReply\": \"short apologetic and proactive reply if there is a complaint, or a gracious thank you if completely positive\"\n"
                                +
                                "}";

                try {
                        // Build request body theo format OpenAI (Gemini hỗ trợ)
                        String requestBody = objectMapper.writeValueAsString(Map.of(
                                        "model", model,
                                        "messages", new Object[] {
                                                        Map.of("role", "user", "content", prompt)
                                        },
                                        "max_tokens", 1000, // Tăng hẳn lên 3000 cho dư dả
                                        "temperature", 0.7,
                                        "response_format", Map.of("type", "json_object") // Vũ khí bí mật: Ép Gemini trả
                                                                                         // JSON chuẩn
                        ));

                        HttpClient client = HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(30))
                                        .build();

                        // Endpoint: baseUrl + "chat/completions"
                        String url = baseUrl.endsWith("/")
                                        ? baseUrl + "chat/completions"
                                        : baseUrl + "/chat/completions";

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .timeout(Duration.ofSeconds(60))
                                        .header("Content-Type", "application/json")
                                        .header("Authorization", "Bearer " + apiKey)
                                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                        .build();

                        HttpResponse<String> response = client.send(
                                        request, HttpResponse.BodyHandlers.ofString());

                        System.out.println("Gemini status: " + response.statusCode());
                        System.out.println("Gemini body: " + response.body());

                        if (response.statusCode() != 200) {
                                throw new RuntimeException("Gemini API error: " + response.body());
                        }

                        // Parse response theo format OpenAI
                        JsonNode root = objectMapper.readTree(response.body());
                        String content = root
                                        .path("choices").get(0)
                                        .path("message")
                                        .path("content")
                                        .asText();

                        // Clean markdown nếu có
                        content = content
                                        .replace("```json", "")
                                        .replace("```", "")
                                        .trim();

                        System.out.println("Parsed content: " + content);

                        return objectMapper.readValue(
                                        content,
                                        new TypeReference<Map<String, String>>() {
                                        });

                } catch (Exception e) {
                        System.err.println("AiReplyService error: " + e.getMessage());
                        // Fallback replies
                        return Map.of(
                                        "standardReply",
                                        "Thank you for your feedback. We appreciate you sharing your experience with us.",
                                        "friendlyReply",
                                        "Thank you so much for your review! 😊 We hope to welcome you back very soon!",
                                        "recoveryReply",
                                        "We sincerely apologize for the inconvenience. Please contact us so we can make this right for you.");
                }
        }
}