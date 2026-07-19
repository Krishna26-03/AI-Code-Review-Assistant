package com.astra.codereview.dto;

import com.astra.codereview.entity.Severity;

/**
 * Intermediate representation emitted directly by Checkstyle / PMD / SpotBugs
 * before it is normalized into a persisted ReviewFinding. Kept separate from
 * ReviewFinding so the static-analysis layer has zero JPA/entity coupling.
 */
public record RawFinding(
        Severity severity,
        String issue,
        String explanation,
        String suggestion,
        String fileName,
        Integer lineNumber,
        String sourceTool
) {}
