package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.ProjectCreateRequest;
import com.college.project.dto.ProjectResponse;
import com.college.project.dto.ProjectUpdateRequest;
import com.college.project.model.ProjectStatus;
import com.college.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller managing student project lifecycle, proposals, guide allocation,
 * and paginated queries.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectResponse> projects = projectService.getAllProjects(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully", projects));
    }

    @GetMapping({"/my-projects", "/guide-projects"})
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(@AuthenticationPrincipal UserDetails userDetails) {
        // Automatically checks if user is student or guide
        List<ProjectResponse> projects;
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUIDE"))) {
            projects = projectService.getProjectsByGuide(userDetails.getUsername());
        } else {
            projects = projectService.getProjectsByStudent(userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.success("Your projects fetched", projects));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project details retrieved", projectService.getProjectResponseById(id)));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProjectJson(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response = projectService.createProject(request, userDetails.getUsername(), null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project proposal submitted successfully", response));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProjectMultipart(
            @RequestPart("project") @Valid ProjectCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response = projectService.createProject(request, userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project proposal with document submitted successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response = projectService.updateProject(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GUIDE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusBody,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectStatus status = ProjectStatus.valueOf(statusBody.get("status"));
        ProjectResponse response = projectService.updateProjectStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project status updated to " + status, response));
    }

    @PatchMapping("/{id}/assign-guide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignGuide(
            @PathVariable Long id,
            @RequestBody Map<String, Long> guideBody) {

        Long guideId = guideBody.get("guideId");
        ProjectResponse response = projectService.assignGuide(id, guideId);
        return ResponseEntity.ok(ApiResponse.success("Guide allocated successfully", response));
    }

    @PostMapping("/{id}/document")
    public ResponseEntity<ApiResponse<ProjectResponse>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response = projectService.uploadDocument(id, file, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Proposal document uploaded successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }
}
