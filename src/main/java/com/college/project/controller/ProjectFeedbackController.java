package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.FeedbackRequest;
import com.college.project.dto.FeedbackResponse;
import com.college.project.service.ProjectFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for collaborative project discussion and guide remarks.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/feedbacks")
@RequiredArgsConstructor
public class ProjectFeedbackController {

    private final ProjectFeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getProjectFeedbacks(@PathVariable Long projectId) {
        List<FeedbackResponse> list = feedbackService.getFeedbacksByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project remarks thread fetched", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> addFeedback(
            @PathVariable Long projectId,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        FeedbackResponse response = feedbackService.addFeedback(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback posted successfully", response));
    }
}
