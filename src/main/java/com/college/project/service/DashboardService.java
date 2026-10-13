package com.college.project.service;

import com.college.project.dto.ChartDataDto;
import com.college.project.dto.DashboardStatsResponse;
import com.college.project.dto.ProjectResponse;
import com.college.project.dto.SubmissionResponse;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.Domain;
import com.college.project.model.Project;
import com.college.project.model.ProjectStatus;
import com.college.project.model.Role;
import com.college.project.model.User;
import com.college.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service aggregating high-level metrics, chart distributions, and recent feeds
 * for the interactive dashboard.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final SubmissionRepository submissionRepository;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        long totalProjects = projectRepository.count();
        long totalStudents = userRepository.findByRole(Role.ROLE_STUDENT).size();
        long totalGuides = userRepository.findByRole(Role.ROLE_GUIDE).size();
        long totalDomains = domainRepository.count();
        long completedProjects = projectRepository.countByStatus(ProjectStatus.COMPLETED);
        long pendingSubmissions = submissionRepository.findAll().stream()
                .filter(s -> s.getStatus().name().equals("SUBMITTED"))
                .count();

        // 1. Status Distribution (Pie / Donut Chart)
        List<ChartDataDto> statusDistribution = new ArrayList<>();
        for (ProjectStatus st : ProjectStatus.values()) {
            long count = projectRepository.countByStatus(st);
            if (count > 0) {
                statusDistribution.add(new ChartDataDto(st.name().replace("_", " "), count));
            }
        }

        // 2. Domain Distribution (Bar Chart)
        List<ChartDataDto> domainDistribution = new ArrayList<>();
        List<Domain> domains = domainRepository.findAll();
        for (Domain d : domains) {
            long count = projectRepository.countByDomainId(d.getId());
            domainDistribution.add(new ChartDataDto(d.getName(), count));
        }

        // 3. Faculty Guide Workload
        List<ChartDataDto> guideWorkload = new ArrayList<>();
        List<User> guides = userRepository.findByRole(Role.ROLE_GUIDE);
        for (User g : guides) {
            long count = projectRepository.countByGuideId(g.getId());
            guideWorkload.add(new ChartDataDto(g.getFullName(), count));
        }

        // 4. Recent Projects (Limit 5)
        List<ProjectResponse> recentProjects = projectRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent()
                .stream()
                .map(projectService::mapToProjectResponse)
                .collect(Collectors.toList());

        // 5. Recent Submissions (Limit 5)
        List<SubmissionResponse> recentSubmissions = submissionRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "submittedAt")))
                .getContent()
                .stream()
                .map(sub -> SubmissionResponse.builder()
                        .id(sub.getId())
                        .milestoneId(sub.getMilestone().getId())
                        .milestoneTitle(sub.getMilestone().getTitle())
                        .projectId(sub.getProject().getId())
                        .projectTitle(sub.getProject().getTitle())
                        .studentId(sub.getStudent().getId())
                        .studentName(sub.getStudent().getFullName())
                        .fileName(sub.getFileName())
                        .status(sub.getStatus())
                        .submittedAt(sub.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalProjects(totalProjects)
                .totalStudents(totalStudents)
                .totalGuides(totalGuides)
                .totalDomains(totalDomains)
                .completedProjects(completedProjects)
                .pendingSubmissions(pendingSubmissions)
                .statusDistribution(statusDistribution)
                .domainDistribution(domainDistribution)
                .guideWorkload(guideWorkload)
                .recentProjects(recentProjects)
                .recentSubmissions(recentSubmissions)
                .build();
    }
}
