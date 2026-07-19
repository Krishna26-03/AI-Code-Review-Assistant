package com.astra.codereview.dto;

import com.astra.codereview.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
        Long id,
        Long projectId,
        String projectName,
        Double reviewScore,
        Review.ReviewStatus status,
        String summary,
        LocalDateTime createdAt,
        List<FindingDto> findings
) {
    public static ReviewDto from(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getProject().getId(),
                r.getProject().getProjectName(),
                r.getReviewScore(),
                r.getStatus(),
                r.getSummary(),
                r.getCreatedAt(),
                r.getFindings().stream().map(FindingDto::from).toList()
        );
    }
}
