package com.college.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for student project proposal submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequest {

    @NotBlank(message = "Project title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Project description / synopsis is required")
    private String description;

    @NotNull(message = "Domain ID is required")
    private Long domainId;

    private Long guideId; // Optional preferred guide

    @Builder.Default
    private String academicYear = "2025-2026";

    private String githubUrl;
    private String liveDemoUrl;
}
