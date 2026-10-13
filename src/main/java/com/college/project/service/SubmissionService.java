package com.college.project.service;

import com.college.project.dto.SubmissionResponse;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.*;
import com.college.project.repository.MilestoneRepository;
import com.college.project.repository.SubmissionRepository;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing student deliverable file uploads and faculty review approvals.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public SubmissionResponse createSubmission(Long milestoneId, MultipartFile file, String comments, String studentEmail) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to submit.");
        }

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", milestoneId));

        Project project = milestone.getProject();
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", studentEmail));

        if (!project.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("Only the project owner can submit deliverables for this milestone.");
        }

        String storedPath = fileStorageService.storeFile(file, "submissions");

        Submission submission = Submission.builder()
                .milestone(milestone)
                .project(project)
                .student(student)
                .fileName(file.getOriginalFilename())
                .filePath(storedPath)
                .fileSize(file.getSize())
                .comments(comments)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        // Automatically update milestone status to IN_PROGRESS if PENDING
        if (milestone.getStatus() == MilestoneStatus.PENDING) {
            milestone.setStatus(MilestoneStatus.IN_PROGRESS);
            milestoneRepository.save(milestone);
        }

        return mapToSubmissionResponse(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByMilestone(Long milestoneId) {
        return submissionRepository.findByMilestoneId(milestoneId).stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByProject(Long projectId) {
        return submissionRepository.findByProjectId(projectId).stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubmissionResponse reviewSubmission(Long submissionId, SubmissionStatus status, String guideEmail) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));

        User user = userRepository.findByEmail(guideEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", guideEmail));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        boolean isGuide = submission.getProject().getGuide() != null
                && submission.getProject().getGuide().getId().equals(user.getId());

        if (!isAdmin && !isGuide) {
            throw new AccessDeniedException("Only the assigned guide or administrator can review submissions.");
        }

        submission.setStatus(status);

        // If approved, mark the milestone as completed
        if (status == SubmissionStatus.APPROVED) {
            Milestone milestone = submission.getMilestone();
            milestone.setStatus(MilestoneStatus.COMPLETED);
            milestoneRepository.save(milestone);
        }

        return mapToSubmissionResponse(submissionRepository.save(submission));
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .milestoneId(submission.getMilestone().getId())
                .milestoneTitle(submission.getMilestone().getTitle())
                .projectId(submission.getProject().getId())
                .projectTitle(submission.getProject().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getFullName())
                .studentRollNo(submission.getStudent().getRollOrEmpId())
                .fileName(submission.getFileName())
                .filePath(submission.getFilePath())
                .fileSize(submission.getFileSize())
                .comments(submission.getComments())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .downloadUrl("/api/files/download/" + submission.getFilePath())
                .build();
    }
}
