package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.DashboardStatsResponse;
import com.college.project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing dashboard analytics, metrics cards, and chart feeds.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(@AuthenticationPrincipal UserDetails userDetails) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved", stats));
    }
}
