package com.college.project.dto;

import com.college.project.model.ReviewStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representation of an evaluation score entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResponse {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private Long evaluatorId;
    private String evaluatorName;
    private ReviewStage reviewStage;
    private Double technicalScore;
    private Double documentationScore;
    private Double presentationScore;
    private Double totalScore;
    private String remarks;
    private LocalDateTime evaluatedAt;
}
