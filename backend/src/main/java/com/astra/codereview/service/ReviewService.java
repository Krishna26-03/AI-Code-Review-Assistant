package com.astra.codereview.service;

import com.astra.codereview.dto.RawFinding;
import com.astra.codereview.dto.ReviewDto;
import com.astra.codereview.entity.Project;
import com.astra.codereview.entity.Review;
import com.astra.codereview.entity.ReviewFinding;
import com.astra.codereview.exception.ApiExceptions;
import com.astra.codereview.repository.ReviewRepository;
import com.astra.codereview.service.analysis.CompilationResult;
import com.astra.codereview.service.analysis.StaticAnalysisAggregatorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ProjectService projectService;
    private final ReviewRepository reviewRepository;
    private final StaticAnalysisAggregatorService staticAnalysisAggregatorService;
    private final ScoringService scoringService;
    private final AiReviewService aiReviewService;

    @Transactional
    public ReviewDto runReview(Long projectId, Long userId) {
        Project project = projectService.getOwned(projectId, userId);

        Review review = Review.builder()
                .project(project)
                .status(Review.ReviewStatus.RUNNING)
                .build();
        review = reviewRepository.save(review);

        try {
            // ✓ NEW: Validate compilation FIRST before any analysis
            CompilationResult compilationResult =
                    staticAnalysisAggregatorService.validateCompilation(project.getStoragePath());

            if (!compilationResult.isSuccessful()) {
                log.error("Compilation failed for project {}: {}", projectId, compilationResult.getErrorsAsString());

                // ✓ REJECT: Set score to 0.0 for any compilation errors
                review.setReviewScore(0.0);
                review.setStatus(Review.ReviewStatus.FAILED);
                review.setSummary("❌ CODE COMPILATION FAILED\n\n" + compilationResult.getErrorsAsString() +
                        "\n\nFix syntax errors before submitting for review.");
                reviewRepository.save(review);

                throw new ApiExceptions.AnalysisFailedException(
                        "Code compilation failed. Cannot proceed with review. Errors: " +
                                compilationResult.getErrorsAsString());
            }

            // Only proceed to static analysis if compilation succeeds
            StaticAnalysisAggregatorService.AggregatedResult result =
                    staticAnalysisAggregatorService.analyze(project.getStoragePath());

            List<RawFinding> rawFindings = result.findings();
            double score = scoringService.computeScore(rawFindings);
            String summary = aiReviewService.summarize(project.getProjectName(), score, rawFindings);

            review.setReviewScore(score);
            review.setSummary(summary);
            review.setStatus(Review.ReviewStatus.COMPLETED);

            for (RawFinding rf : rawFindings) {
                review.getFindings().add(
                        ReviewFinding.builder()
                                .review(review)
                                .severity(rf.severity())
                                .issue(rf.issue())
                                .explanation(rf.explanation())
                                .suggestion(rf.suggestion())
                                .fileName(rf.fileName())
                                .lineNumber(rf.lineNumber())
                                .sourceTool(rf.sourceTool())
                                .build()
                );
            }

            reviewRepository.save(review);
        } catch (Exception e) {
            log.error("Review failed for project {}", projectId, e);
            review.setStatus(Review.ReviewStatus.FAILED);
            review.setSummary("Review could not be completed: " + e.getMessage());
            reviewRepository.save(review);
            throw new ApiExceptions.AnalysisFailedException("Review failed: " + e.getMessage(), e);
        }

        return ReviewDto.from(review);
    }

    @Transactional(readOnly = true)
    public ReviewDto getOwned(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Review not found"));
        if (!review.getProject().getUser().getId().equals(userId)) {
            throw new ApiExceptions.ResourceNotFoundException("Review not found");
        }
        return ReviewDto.from(review);
    }

    @Transactional(readOnly = true)
    public Review getOwnedEntity(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Review not found"));
        if (!review.getProject().getUser().getId().equals(userId)) {
            throw new ApiExceptions.ResourceNotFoundException("Review not found");
        }
        // Touch the lazy collection now, while the transaction/session is still open,
        // so PdfReportService can safely iterate it after this method returns.
        review.getFindings().size();
        return review;
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listForProject(Long projectId, Long userId) {
        Project project = projectService.getOwned(projectId, userId);
        return reviewRepository.findByProjectOrderByCreatedAtDesc(project).stream().map(ReviewDto::from).toList();
    }
}