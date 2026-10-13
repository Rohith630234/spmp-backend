package com.college.project.repository;

import com.college.project.model.ProjectFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for project feedback messages.
 */
@Repository
public interface ProjectFeedbackRepository extends JpaRepository<ProjectFeedback, Long> {

    List<ProjectFeedback> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
