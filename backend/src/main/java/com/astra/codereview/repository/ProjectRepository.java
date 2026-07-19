package com.astra.codereview.repository;

import com.astra.codereview.entity.Project;
import com.astra.codereview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByCreatedAtDesc(User user);
    List<Project> findByUserId(Long userId);
}
