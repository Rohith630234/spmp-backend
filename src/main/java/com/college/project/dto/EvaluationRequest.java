package com.college.project.dto;

import com.college.project.model.ReviewStage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for entering evaluation marks and remarks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationRequest {

    @NotNull(message = "Review stage is required (REVIEW_1, REVIEW_2, or FINAL_VIVA)")
    private ReviewStage reviewStage;

    @NotNull(message = "Technical score is required")
    @DecimalMin(value = "0.0", message = "Technical score cannot be negative")
    @DecimalMax(value = "30.0", message = "Technical score cannot exceed 30.0")
    private Double technicalScore;

    @NotNull(message = "Documentation score is required")
    @DecimalMin(value = "0.0", message = "Documentation score cannot be negative")
    @DecimalMax(value = "30.0", message = "Documentation score cannot exceed 30.0")
    private Double documentationScore;

    @NotNull(message = "Presentation score is required")
    @DecimalMin(value = "0.0", message = "Presentation score cannot be negative")
    @DecimalMax(value = "40.0", message = "Presentation score cannot exceed 40.0")
    private Double presentationScore;

    private String remarks;
}
