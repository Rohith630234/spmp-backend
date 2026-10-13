package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.MilestoneRequest;
import com.college.project.dto.MilestoneResponse;
import com.college.project.model.MilestoneStatus;
import com.college.project.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing project milestones and task sprint deadlines.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @GetMapping({"/projects/{projectId}/milestones", "/milestones/project/{projectId}"})
    public ResponseEntity<ApiResponse<List<MilestoneResponse>>> getProjectMilestones(@PathVariable Long projectId) {
        List<MilestoneResponse> list = milestoneService.getMilestonesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project milestones retrieved", list));
    }

    @PostMapping("/projects/{projectId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneResponse>> createMilestone(
            @PathVariable Long projectId,
            @Valid @RequestBody MilestoneRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MilestoneResponse created = milestoneService.createMilestone(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Milestone created successfully", created));
    }

    @PutMapping("/milestones/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @PathVariable Long id,
            @Valid @RequestBody MilestoneRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MilestoneResponse updated = milestoneService.updateMilestone(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Milestone updated successfully", updated));
    }

    @PatchMapping("/milestones/{id}/status")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            @AuthenticationPrincipal UserDetails userDetails) {

        MilestoneStatus status = MilestoneStatus.valueOf(statusBody.get("status"));
        MilestoneResponse response = milestoneService.updateMilestoneStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Milestone marked as " + status, response));
    }

    @DeleteMapping("/milestones/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        milestoneService.deleteMilestone(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Milestone removed successfully", null));
    }
}
