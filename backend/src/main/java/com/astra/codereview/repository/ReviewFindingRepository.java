package com.astra.codereview.repository;

import com.astra.codereview.entity.Review;
import com.astra.codereview.entity.ReviewFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewFindingRepository extends JpaRepository<ReviewFinding, Long> {
    List<ReviewFinding> findByReview(Review review);
    List<ReviewFinding> findByReviewOrderBySeverityAsc(Review review);
}
