package com.college.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for posting a feedback remark or comment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {

    @NotBlank(message = "Feedback comment cannot be empty")
    private String feedbackText;
}
