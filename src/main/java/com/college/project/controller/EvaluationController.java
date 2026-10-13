package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.EvaluationRequest;
import com.college.project.dto.EvaluationResponse;
import com.college.project.dto.ScorecardResponse;
import com.college.project.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for academic evaluation, rubric scores, and university scorecards.
 */
@RestController
@RequestMapping("/api/projects/{projectId}")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/evaluations")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    public ResponseEntity<ApiResponse<EvaluationResponse>> submitEvaluation(
            @PathVariable Long projectId,
            @Valid @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        EvaluationResponse response = evaluationService.submitEvaluation(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evaluation saved successfully", response));
    }

    @GetMapping("/evaluations")
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> getEvaluations(@PathVariable Long projectId) {
        List<EvaluationResponse> list = evaluationService.getEvaluationsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project evaluations fetched", list));
    }

    @GetMapping("/scorecard")
    public ResponseEntity<ApiResponse<ScorecardResponse>> getProjectScorecard(@PathVariable Long projectId) {
        ScorecardResponse scorecard = evaluationService.getProjectScorecard(projectId);
        return ResponseEntity.ok(ApiResponse.success("Academic scorecard generated", scorecard));
    }
}
