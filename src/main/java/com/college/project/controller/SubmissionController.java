package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.SubmissionResponse;
import com.college.project.model.SubmissionStatus;
import com.college.project.service.FileStorageService;
import com.college.project.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for deliverable file uploads, reviews, and secure file downloads.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final FileStorageService fileStorageService;

    @PostMapping("/submissions")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> createSubmission(
            @RequestParam("milestoneId") Long milestoneId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comments", required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        SubmissionResponse response = submissionService.createSubmission(milestoneId, file, comments, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File submitted successfully", response));
    }

    @GetMapping("/milestones/{milestoneId}/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByMilestone(@PathVariable Long milestoneId) {
        List<SubmissionResponse> list = submissionService.getSubmissionsByMilestone(milestoneId);
        return ResponseEntity.ok(ApiResponse.success("Milestone submissions retrieved", list));
    }

    @GetMapping("/projects/{projectId}/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByProject(@PathVariable Long projectId) {
        List<SubmissionResponse> list = submissionService.getSubmissionsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project submissions retrieved", list));
    }

    @PatchMapping("/submissions/{id}/review")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> reviewSubmission(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            @AuthenticationPrincipal UserDetails userDetails) {

        SubmissionStatus status = SubmissionStatus.valueOf(statusBody.get("status"));
        SubmissionResponse response = submissionService.reviewSubmission(id, status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Submission reviewed as " + status, response));
    }

    /**
     * Download or stream uploaded document/code file.
     */
    @GetMapping("/files/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        // Extract relative file path after "/api/files/download/"
        String relativePath = fullPath.substring(fullPath.indexOf("/download/") + 10);

        Resource resource = fileStorageService.loadFileAsResource(relativePath);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Default to octet-stream
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
