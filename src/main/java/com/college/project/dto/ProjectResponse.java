package com.college.project.dto;

import com.college.project.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed representation of a project returned to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private Long id;
    private String title;
    private String description;

    // Domain Info
    private Long domainId;
    private String domainName;

    // Student Info
    private Long studentId;
    private String studentName;
    private String studentRollNo;
    private String studentEmail;
    private String studentDepartment;

    // Guide Info (if allocated)
    private Long guideId;
    private String guideName;
    private String guideEmail;

    private String academicYear;
    private ProjectStatus status;
    private String githubUrl;
    private String liveDemoUrl;
    private String documentPath;

    // Progress metrics
    private long totalMilestones;
    private long completedMilestones;
    private double progressPercentage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
