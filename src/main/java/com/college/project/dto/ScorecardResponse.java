package com.college.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated university grade scorecard for a project across all review stages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScorecardResponse {

    private Long projectId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String studentRollNo;
    private String guideName;

    private Double review1Total;
    private Double review2Total;
    private Double finalVivaTotal;
    private Double overallAverage;
    private String academicGrade;

    private List<EvaluationResponse> evaluationDetails;
}
