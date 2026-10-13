package com.college.project.dto;

import com.college.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representation of a project feedback discussion entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private Long id;
    private Long projectId;
    private Long authorId;
    private String authorName;
    private Role authorRole;
    private String authorAvatar;
    private String feedbackText;
    private LocalDateTime createdAt;
}
