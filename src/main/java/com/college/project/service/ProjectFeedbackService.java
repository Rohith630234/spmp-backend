package com.college.project.service;

import com.college.project.dto.FeedbackRequest;
import com.college.project.dto.FeedbackResponse;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.Project;
import com.college.project.model.ProjectFeedback;
import com.college.project.model.User;
import com.college.project.repository.ProjectFeedbackRepository;
import com.college.project.repository.ProjectRepository;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing collaborative feedback remarks and discussion comments on projects.
 */
@Service
@RequiredArgsConstructor
public class ProjectFeedbackService {

    private final ProjectFeedbackRepository feedbackRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public FeedbackResponse addFeedback(Long projectId, FeedbackRequest request, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        ProjectFeedback feedback = ProjectFeedback.builder()
                .project(project)
                .author(author)
                .feedbackText(request.getFeedbackText().trim())
                .build();

        return mapToFeedbackResponse(feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbacksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return feedbackRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    private FeedbackResponse mapToFeedbackResponse(ProjectFeedback fb) {
        return FeedbackResponse.builder()
                .id(fb.getId())
                .projectId(fb.getProject().getId())
                .authorId(fb.getAuthor().getId())
                .authorName(fb.getAuthor().getFullName())
                .authorRole(fb.getAuthor().getRole())
                .authorAvatar(fb.getAuthor().getAvatarUrl())
                .feedbackText(fb.getFeedbackText())
                .createdAt(fb.getCreatedAt())
                .build();
    }
}
