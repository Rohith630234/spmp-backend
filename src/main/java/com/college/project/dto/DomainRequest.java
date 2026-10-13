package com.college.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating technical domain categories.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainRequest {

    @NotBlank(message = "Domain name is required")
    @Size(min = 2, max = 100, message = "Domain name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Builder.Default
    private boolean active = true;
}
