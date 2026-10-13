package com.college.project.repository;

import com.college.project.model.Project;
import com.college.project.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStudentId(Long studentId);

    List<Project> findByGuideId(Long guideId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByDomainId(Long domainId);

    Page<Project> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.student.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Project> searchProjects(@Param("query") String query, Pageable pageable);

    long countByStatus(ProjectStatus status);

    long countByDomainId(Long domainId);

    long countByGuideId(Long guideId);
}
