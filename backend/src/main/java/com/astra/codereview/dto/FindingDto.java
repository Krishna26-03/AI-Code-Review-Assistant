package com.astra.codereview.dto;

import com.astra.codereview.entity.ReviewFinding;
import com.astra.codereview.entity.Severity;

public record FindingDto(
        Long id,
        Severity severity,
        String issue,
        String explanation,
        String suggestion,
        String fileName,
        Integer lineNumber,
        String sourceTool
) {
    public static FindingDto from(ReviewFinding f) {
        return new FindingDto(f.getId(), f.getSeverity(), f.getIssue(), f.getExplanation(),
                f.getSuggestion(), f.getFileName(), f.getLineNumber(), f.getSourceTool());
    }
}
