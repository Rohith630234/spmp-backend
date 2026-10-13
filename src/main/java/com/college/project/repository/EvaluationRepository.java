package com.college.project.repository;

import com.college.project.model.Evaluation;
import com.college.project.model.ReviewStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for academic evaluation rubrics.
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByProjectId(Long projectId);

    List<Evaluation> findByEvaluatorId(Long evaluatorId);

    Optional<Evaluation> findByProjectIdAndReviewStage(Long projectId, ReviewStage reviewStage);

    boolean existsByProjectIdAndReviewStage(Long projectId, ReviewStage reviewStage);
}
