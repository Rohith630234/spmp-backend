package com.college.project.service;

import com.college.project.dto.ProjectCreateRequest;
import com.college.project.dto.ProjectResponse;
import com.college.project.dto.ProjectUpdateRequest;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.*;
import com.college.project.repository.DomainRepository;
import com.college.project.repository.MilestoneRepository;
import com.college.project.repository.ProjectRepository;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing academic project proposals, guide allocation, status changes,
 * and project discovery queries.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final MilestoneRepository milestoneRepository;
    private final FileStorageService fileStorageService;

    /**
     * Submits a new project proposal by the authenticated student.
     */
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, String studentEmail, MultipartFile documentFile) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found: " + studentEmail));

        Domain domain = domainRepository.findById(request.getDomainId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with ID: " + request.getDomainId()));

        User guide = null;
        if (request.getGuideId() != null) {
            guide = userRepository.findById(request.getGuideId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guide not found with ID: " + request.getGuideId()));
            if (guide.getRole() != Role.ROLE_GUIDE && guide.getRole() != Role.ROLE_ADMIN) {
                throw new BadRequestException("Allocated mentor must have ROLE_GUIDE or ROLE_ADMIN");
            }
        }

        String documentPath = null;
        if (documentFile != null && !documentFile.isEmpty()) {
            documentPath = fileStorageService.storeFile(documentFile, "proposals");
        }

        Project project = Project.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .domain(domain)
                .student(student)
                .guide(guide)
                .academicYear(request.getAcademicYear() != null ? request.getAcademicYear() : "2025-2026")
                .status(ProjectStatus.SUBMITTED)
                .githubUrl(request.getGithubUrl())
                .liveDemoUrl(request.getLiveDemoUrl())
                .documentPath(documentPath)
                .build();

        Project savedProject = projectRepository.save(project);
        return mapToProjectResponse(savedProject);
    }

    /**
     * Updates project details, repository URLs, or guide.
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, String userEmail) {
        Project project = getProjectEntity(projectId);
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Authorization check: Only owning student, assigned guide, or admin can update
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isGuide = project.getGuide() != null && project.getGuide().getId().equals(currentUser.getId());
        boolean isOwner = project.getStudent().getId().equals(currentUser.getId());

        if (!isAdmin && !isGuide && !isOwner) {
            throw new AccessDeniedException("You are not authorized to update this project.");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            project.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            project.setDescription(request.getDescription().trim());
        }
        if (request.getDomainId() != null) {
            Domain domain = domainRepository.findById(request.getDomainId())
                    .orElseThrow(() -> new ResourceNotFoundException("Domain", "id", request.getDomainId()));
            project.setDomain(domain);
        }
        if (request.getGithubUrl() != null) {
            project.setGithubUrl(request.getGithubUrl().trim());
        }
        if (request.getLiveDemoUrl() != null) {
            project.setLiveDemoUrl(request.getLiveDemoUrl().trim());
        }
        if (request.getStatus() != null && (isAdmin || isGuide)) {
            project.setStatus(request.getStatus());
        }
        if (request.getGuideId() != null && isAdmin) {
            User newGuide = userRepository.findById(request.getGuideId())
                    .orElseThrow(() -> new ResourceNotFoundException("Guide", "id", request.getGuideId()));
            project.setGuide(newGuide);
        }

        return mapToProjectResponse(projectRepository.save(project));
    }

    /**
     * Admin allocates a faculty guide to a project proposal.
     */
    @Transactional
    public ProjectResponse assignGuide(Long projectId, Long guideId) {
        Project project = getProjectEntity(projectId);
        User guide = userRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty Guide", "id", guideId));

        if (guide.getRole() != Role.ROLE_GUIDE && guide.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Allocated user must have GUIDE or ADMIN role.");
        }

        project.setGuide(guide);
        if (project.getStatus() == ProjectStatus.SUBMITTED) {
            project.setStatus(ProjectStatus.APPROVED);
        }

        return mapToProjectResponse(projectRepository.save(project));
    }

    /**
     * Updates lifecycle status of a project (e.g. APPROVED, IN_PROGRESS, COMPLETED).
     */
    @Transactional
    public ProjectResponse updateProjectStatus(Long projectId, ProjectStatus status, String userEmail) {
        Project project = getProjectEntity(projectId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        boolean isGuide = project.getGuide() != null && project.getGuide().getId().equals(user.getId());

        if (!isAdmin && !isGuide) {
            throw new AccessDeniedException("Only the assigned guide or admin can update project status.");
        }

        project.setStatus(status);
        return mapToProjectResponse(projectRepository.save(project));
    }

    /**
     * Uploads or replaces synopsis/proposal document PDF.
     */
    @Transactional
    public ProjectResponse uploadDocument(Long projectId, MultipartFile file, String userEmail) {
        Project project = getProjectEntity(projectId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (!project.getStudent().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only the project owner student or admin can upload proposal documents.");
        }

        String path = fileStorageService.storeFile(file, "proposals");
        project.setDocumentPath(path);
        return mapToProjectResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectResponseById(Long projectId) {
        return mapToProjectResponse(getProjectEntity(projectId));
    }

    @Transactional(readOnly = true)
    public Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));
        return projectRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByGuide(String guideEmail) {
        User guide = userRepository.findByEmail(guideEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found: " + guideEmail));
        return projectRepository.findByGuideId(guide.getId()).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(String search, Pageable pageable) {
        Page<Project> projectsPage;
        if (search != null && !search.trim().isEmpty()) {
            projectsPage = projectRepository.searchProjects(search.trim(), pageable);
        } else {
            projectsPage = projectRepository.findAll(pageable);
        }

        List<ProjectResponse> dtoList = projectsPage.getContent().stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, projectsPage.getTotalElements());
    }

    @Transactional
    public void deleteProject(Long projectId, String userEmail) {
        Project project = getProjectEntity(projectId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (user.getRole() != Role.ROLE_ADMIN && !project.getStudent().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only an admin or the project owner can delete this project.");
        }

        projectRepository.delete(project);
    }

    /**
     * Converts Project entity to ProjectResponse DTO and computes milestone progress metrics.
     */
    public ProjectResponse mapToProjectResponse(Project project) {
        long totalMilestones = milestoneRepository.countByProjectId(project.getId());
        long completedMilestones = milestoneRepository.countByProjectIdAndStatus(project.getId(), MilestoneStatus.COMPLETED);
        double progressPercentage = totalMilestones > 0
                ? Math.round(((double) completedMilestones / totalMilestones) * 100.0 * 10.0) / 10.0
                : 0.0;

        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .domainId(project.getDomain().getId())
                .domainName(project.getDomain().getName())
                .studentId(project.getStudent().getId())
                .studentName(project.getStudent().getFullName())
                .studentRollNo(project.getStudent().getRollOrEmpId())
                .studentEmail(project.getStudent().getEmail())
                .studentDepartment(project.getStudent().getDepartment())
                .guideId(project.getGuide() != null ? project.getGuide().getId() : null)
                .guideName(project.getGuide() != null ? project.getGuide().getFullName() : null)
                .guideEmail(project.getGuide() != null ? project.getGuide().getEmail() : null)
                .academicYear(project.getAcademicYear())
                .status(project.getStatus())
                .githubUrl(project.getGithubUrl())
                .liveDemoUrl(project.getLiveDemoUrl())
                .documentPath(project.getDocumentPath())
                .totalMilestones(totalMilestones)
                .completedMilestones(completedMilestones)
                .progressPercentage(progressPercentage)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
