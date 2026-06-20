package com.example.gptdemo.service;

import com.example.gptdemo.config.OpenAiProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiChatService {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiChatService(RestClient openAiRestClient, OpenAiProperties properties) {
        this.restClient = openAiRestClient;
        this.properties = properties;
    }

    public String ask(String prompt) {
        Map<String, Object> payload = Map.of(
                "model", properties.model(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        OpenAiResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.apiKey())
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .body(OpenAiResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "No response from OpenAI.";
        }

        OpenAiMessage message = response.choices().get(0).message();
        if (message == null || message.content() == null) {
            return "Empty response content.";
        }
        return message.content();
    }

    private record OpenAiResponse(List<OpenAiChoice> choices) {
    }

    private record OpenAiChoice(OpenAiMessage message) {
    }

    private record OpenAiMessage(String role, String content) {
    }
}
