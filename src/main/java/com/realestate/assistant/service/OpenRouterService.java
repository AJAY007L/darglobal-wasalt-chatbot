package com.realestate.assistant.service;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.List;
import java.util.Map;
@Service
public class OpenRouterService {
    private final RestClient client;
    private final String apiKey;
    private final String model;
    public OpenRouterService(RestClient.Builder builder, Environment env) {
        client = builder.baseUrl("https://openrouter.ai/api/v1").build();
        apiKey = env.getProperty("OPENROUTER_API_KEY", "");
        model = env.getProperty("OPENROUTER_MODEL", "openrouter/free");
    }
    public boolean configured() { return apiKey != null && !apiKey.isBlank(); }
    public String complete(String systemPrompt, String userPrompt) {
        if (!configured()) throw new IllegalStateException("OpenRouter API key is not configured");
        Map<String, Object> body = Map.of("model", model, "temperature", 0.2, "messages", List.of(
                Map.of("role", "system", "content", systemPrompt), Map.of("role", "user", "content", userPrompt)));
        try {
            ChatCompletion result = client.post().uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("X-Title", "Property Assistant")
                    .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(ChatCompletion.class);
            if (result == null || result.choices() == null || result.choices().isEmpty()
                    || result.choices().getFirst().message() == null || result.choices().getFirst().message().content() == null) {
                throw new IllegalStateException("OpenRouter returned an empty answer");
            }
            return result.choices().getFirst().message().content().trim();
        } catch (RestClientException ex) {
            throw new IllegalStateException("OpenRouter request failed", ex);
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true) public record ChatCompletion(List<Choice> choices) {}
    @JsonIgnoreProperties(ignoreUnknown = true) public record Choice(Message message) {}
    @JsonIgnoreProperties(ignoreUnknown = true) public record Message(String role, String content) {}
}
