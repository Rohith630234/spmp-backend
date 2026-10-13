package com.college.project.repository;

import com.college.project.model.Milestone;
import com.college.project.model.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Milestone management.
 */
@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<Milestone> findByProjectIdAndStatus(Long projectId, MilestoneStatus status);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, MilestoneStatus status);
}
