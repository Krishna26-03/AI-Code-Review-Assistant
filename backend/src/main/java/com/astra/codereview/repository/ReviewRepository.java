package com.astra.codereview.repository;

import com.astra.codereview.entity.Project;
import com.astra.codereview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProjectOrderByCreatedAtDesc(Project project);
    List<Review> findByProjectUserId(Long userId);
}
