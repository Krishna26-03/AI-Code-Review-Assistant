package com.astra.codereview.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class HuggingFaceLLMService {

    // Some HF-hosted models (e.g. DeepSeek-R1 style reasoning models) emit their
    // chain-of-thought wrapped in <think>...</think> before the real answer.
    // That reasoning is not meant for end users and must be stripped before the
    // content is used anywhere downstream (reports, summaries, etc.).
    private static final Pattern THINK_BLOCK = Pattern.compile("(?s)<think>.*?</think>");

    private final WebClient webClient;

    @Value("${huggingface.model}")
    private String model;

    public HuggingFaceLLMService(
            @Value("${huggingface.api-key}") String apiKey,
            @Value("${huggingface.base-url}") String baseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateCodeReview(String prompt) {

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1500,
                "temperature", 0.3
        );

        JsonNode response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null) {
            return "No response from Hugging Face.";
        }

        JsonNode content = response.path("choices")
                .path(0)
                .path("message")
                .path("content");

        String raw = content.asText("No review generated.");
        return stripReasoning(raw);
    }

    /**
     * Removes any <think>...</think> reasoning block (and the whitespace it
     * leaves behind) that some models include alongside their real answer.
     */
    private String stripReasoning(String text) {
        if (text == null) return text;

        // Remove complete <think>...</think> blocks
        String cleaned = THINK_BLOCK.matcher(text).replaceAll("");

        // If there's an unclosed <think> tag, strip from <think> to the first double newline
        // (or to the end if no clean break)
        if (cleaned.contains("<think>")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("<think>"));
        }

        return cleaned.strip();
    }
}