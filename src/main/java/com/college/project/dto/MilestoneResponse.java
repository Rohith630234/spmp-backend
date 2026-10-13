package com.college.project.dto;

import com.college.project.model.MilestoneStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representation of a milestone returned to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneResponse {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private String title;
    private String description;
    private LocalDate dueDate;
    private MilestoneStatus status;
    private LocalDate completionDate;
    private boolean isOverdue;
    private LocalDateTime createdAt;
}
