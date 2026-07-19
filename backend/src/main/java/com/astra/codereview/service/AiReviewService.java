package com.astra.codereview.service;

import com.astra.codereview.dto.RawFinding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sends the aggregated static-analysis findings (never the AI, the numeric
 * score is computed by ScoringService) to an OpenAI-compatible chat
 * completions endpoint, and asks it to produce a short, readable summary
 * plus prioritized recommendations a developer can act on.
 */
@Service
public class AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewService.class);

    private final HuggingFaceLLMService huggingFaceLLMService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiReviewService(HuggingFaceLLMService huggingFaceLLMService) {
        this.huggingFaceLLMService = huggingFaceLLMService;
    }

    public String summarize(String projectName, double score, List<RawFinding> findings) {
        try {
            String prompt = buildPrompt(projectName, score, findings);
            return huggingFaceLLMService.generateCodeReview(prompt);

        } catch (Exception e) {
            log.warn("HuggingFace summarization failed: {}", e.getMessage());
            return fallbackSummary(projectName, score, findings);
        }
    }

    private String buildPrompt(String projectName, double score, List<RawFinding> findings) {
        String findingsBlock = findings.stream()
                .map(f -> String.format("- [%s][%s] %s (%s%s): %s",
                        f.severity(), f.sourceTool(), f.issue(),
                        f.fileName() == null ? "unknown file" : f.fileName(),
                        f.lineNumber() == null ? "" : ":" + f.lineNumber(),
                        f.explanation() == null ? "" : f.explanation()))
                .collect(Collectors.joining("\n"));

        return """
                Project: %s
                Computed quality score: %.1f / 100 (already calculated, do not recompute it)

                Static analysis findings from Checkstyle, PMD, and SpotBugs:
                %s

                Write a short code review summary (150-250 words) for the developer covering:
                1. Overall assessment in 1-2 sentences.
                2. The 2-4 most important issues to fix first, and why.
                3. One or two positive observations if the findings support it.
                Keep it direct and practical, no fluff, no repeating the raw list verbatim.
                """.formatted(projectName, score, findingsBlock.isBlank() ? "(none reported)" : findingsBlock);
    }

    private String fallbackSummary(String projectName, double score, List<RawFinding> findings) {
        long critical = findings.stream().filter(f -> f.severity().name().equals("CRITICAL")).count();
        long high = findings.stream().filter(f -> f.severity().name().equals("HIGH")).count();
        long medium = findings.stream().filter(f -> f.severity().name().equals("MEDIUM")).count();
        long low = findings.stream().filter(f -> f.severity().name().equals("LOW")).count();

        StringBuilder sb = new StringBuilder();
        sb.append("Automated summary for ").append(projectName).append(" (score: ")
                .append(String.format("%.1f", score)).append("/100).\n\n");
        sb.append("Findings by severity — Critical: ").append(critical)
                .append(", High: ").append(high)
                .append(", Medium: ").append(medium)
                .append(", Low: ").append(low).append(".\n\n");

        if (critical + high == 0) {
            sb.append("No critical or high-severity issues were reported by Checkstyle, PMD, or SpotBugs. ");
            sb.append("Review the medium/low items below to keep the codebase tidy.");
        } else {
            sb.append("Address the critical and high-severity items first — they most often indicate correctness, ");
            sb.append("security, or reliability risks rather than pure style concerns.");
        }
        sb.append("\n\n(Note: this is a templated summary because the Hugging Face service was unavailable.)");
        return sb.toString();
    }
}
