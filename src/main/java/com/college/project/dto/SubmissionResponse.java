package com.college.project.dto;

import com.college.project.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representation of a student document or code submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private Long id;
    private Long milestoneId;
    private String milestoneTitle;
    private Long projectId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String studentRollNo;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String comments;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
    private String downloadUrl;
}
