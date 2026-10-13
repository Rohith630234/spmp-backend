package com.college.project.dto;

import com.college.project.model.ProjectStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for modifying project details, URLs, or status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUpdateRequest {

    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    private String description;
    private Long domainId;
    private Long guideId;
    private ProjectStatus status;
    private String githubUrl;
    private String liveDemoUrl;
}
