package com.college.project.service;

import com.college.project.dto.MilestoneRequest;
import com.college.project.dto.MilestoneResponse;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.*;
import com.college.project.repository.MilestoneRepository;
import com.college.project.repository.ProjectRepository;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing project milestones, sprint tasks, and deadlines.
 */
@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public MilestoneResponse createMilestone(Long projectId, MilestoneRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        boolean isGuide = project.getGuide() != null && project.getGuide().getId().equals(user.getId());
        boolean isOwner = project.getStudent().getId().equals(user.getId());

        if (!isAdmin && !isGuide && !isOwner) {
            throw new AccessDeniedException("You are not authorized to create milestones for this project.");
        }

        Milestone milestone = Milestone.builder()
                .project(project)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(request.getStatus() != null ? request.getStatus() : MilestoneStatus.PENDING)
                .completionDate(request.getStatus() == MilestoneStatus.COMPLETED ? LocalDate.now() : null)
                .build();

        return mapToMilestoneResponse(milestoneRepository.save(milestone));
    }

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getMilestonesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId).stream()
                .map(this::mapToMilestoneResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long milestoneId, MilestoneRequest request, String userEmail) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));

        validateProjectAccess(milestone.getProject(), userEmail);

        milestone.setTitle(request.getTitle().trim());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        if (request.getStatus() != null) {
            milestone.setStatus(request.getStatus());
            if (request.getStatus() == MilestoneStatus.COMPLETED && milestone.getCompletionDate() == null) {
                milestone.setCompletionDate(LocalDate.now());
            } else if (request.getStatus() != MilestoneStatus.COMPLETED) {
                milestone.setCompletionDate(null);
            }
        }

        return mapToMilestoneResponse(milestoneRepository.save(milestone));
    }

    @Transactional
    public MilestoneResponse updateMilestoneStatus(Long milestoneId, MilestoneStatus status, String userEmail) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));

        validateProjectAccess(milestone.getProject(), userEmail);

        milestone.setStatus(status);
        if (status == MilestoneStatus.COMPLETED) {
            milestone.setCompletionDate(LocalDate.now());
        } else {
            milestone.setCompletionDate(null);
        }

        return mapToMilestoneResponse(milestoneRepository.save(milestone));
    }

    @Transactional
    public void deleteMilestone(Long milestoneId, String userEmail) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));

        validateProjectAccess(milestone.getProject(), userEmail);
        milestoneRepository.delete(milestone);
    }

    private void validateProjectAccess(Project project, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        boolean isGuide = project.getGuide() != null && project.getGuide().getId().equals(user.getId());
        boolean isOwner = project.getStudent().getId().equals(user.getId());

        if (!isAdmin && !isGuide && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this milestone.");
        }
    }

    private MilestoneResponse mapToMilestoneResponse(Milestone milestone) {
        boolean isOverdue = milestone.getStatus() != MilestoneStatus.COMPLETED
                && milestone.getDueDate().isBefore(LocalDate.now());

        return MilestoneResponse.builder()
                .id(milestone.getId())
                .projectId(milestone.getProject().getId())
                .projectTitle(milestone.getProject().getTitle())
                .title(milestone.getTitle())
                .description(milestone.getDescription())
                .dueDate(milestone.getDueDate())
                .status(isOverdue && milestone.getStatus() == MilestoneStatus.PENDING ? MilestoneStatus.OVERDUE : milestone.getStatus())
                .completionDate(milestone.getCompletionDate())
                .isOverdue(isOverdue)
                .createdAt(milestone.getCreatedAt())
                .build();
    }
}
