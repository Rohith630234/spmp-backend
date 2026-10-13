package com.college.project.dto;

import com.college.project.model.MilestoneStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request payload for creating or editing project milestones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @Builder.Default
    private MilestoneStatus status = MilestoneStatus.PENDING;
}
