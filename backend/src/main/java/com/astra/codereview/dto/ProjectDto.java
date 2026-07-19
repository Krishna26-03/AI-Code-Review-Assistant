package com.astra.codereview.dto;

import com.astra.codereview.entity.Project;
import com.astra.codereview.entity.UploadType;

import java.time.LocalDateTime;

public record ProjectDto(
        Long id,
        String projectName,
        UploadType uploadType,
        LocalDateTime createdAt,
        Long latestReviewId,
        Double latestReviewScore
) {
    public static ProjectDto from(Project p) {
        Long latestId = null;
        Double latestScore = null;
        if (p.getReviews() != null && !p.getReviews().isEmpty()) {
            var latest = p.getReviews().get(p.getReviews().size() - 1);
            latestId = latest.getId();
            latestScore = latest.getReviewScore();
        }
        return new ProjectDto(p.getId(), p.getProjectName(), p.getUploadType(), p.getCreatedAt(), latestId, latestScore);
    }
}
