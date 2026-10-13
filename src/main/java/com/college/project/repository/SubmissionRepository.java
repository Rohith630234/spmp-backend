package com.college.project.repository;

import com.college.project.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for student file submissions.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByProjectId(Long projectId);

    List<Submission> findByMilestoneId(Long milestoneId);

    List<Submission> findByStudentId(Long studentId);
}
