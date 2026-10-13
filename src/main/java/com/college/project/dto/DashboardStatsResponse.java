package com.college.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated statistics and analytics metrics for role-based dashboards.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    // Metric Cards
    private long totalProjects;
    private long totalStudents;
    private long totalGuides;
    private long totalDomains;
    private long completedProjects;
    private long pendingSubmissions;

    // Charts Data
    private List<ChartDataDto> statusDistribution;
    private List<ChartDataDto> domainDistribution;
    private List<ChartDataDto> guideWorkload;

    // Recent Activity feeds
    private List<ProjectResponse> recentProjects;
    private List<SubmissionResponse> recentSubmissions;
}
