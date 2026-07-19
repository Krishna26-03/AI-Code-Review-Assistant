package com.astra.codereview.service;

import com.astra.codereview.dto.RawFinding;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The review score is computed deterministically from the static-analysis
 * findings (not by the LLM) so that re-running a review on unchanged code
 * always yields the same score. The AI layer is used only for the natural
 * -language summary/explanations, never for the number itself.
 */
@Service
public class ScoringService {

    private static final Map<com.astra.codereview.entity.Severity, Integer> PENALTY = Map.of(
            com.astra.codereview.entity.Severity.CRITICAL, 15,
            com.astra.codereview.entity.Severity.HIGH, 8,
            com.astra.codereview.entity.Severity.MEDIUM, 3,
            com.astra.codereview.entity.Severity.LOW, 1,
            com.astra.codereview.entity.Severity.INFO, 0
    );

    public double computeScore(List<RawFinding> findings) {
        int totalPenalty = findings.stream()
                .mapToInt(f -> PENALTY.getOrDefault(f.severity(), 0))
                .sum();

        double score = 100.0 - totalPenalty;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
