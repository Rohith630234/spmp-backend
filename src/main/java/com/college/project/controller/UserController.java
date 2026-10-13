package com.college.project.controller;

import com.college.project.dto.ApiResponse;
import com.college.project.dto.UserProfileResponse;
import com.college.project.model.Role;
import com.college.project.model.User;
import com.college.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller providing user listings for faculty guide allocation dropdowns.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/guides")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getGuides() {
        List<UserProfileResponse> guides = userRepository.findByRole(Role.ROLE_GUIDE).stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Faculty guides list", guides));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.college.project.exception.ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("User profile fetched", mapToProfile(user)));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getStudents() {
        List<UserProfileResponse> students = userRepository.findByRole(Role.ROLE_STUDENT).stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Students list", students));
    }

    private UserProfileResponse mapToProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .rollOrEmpId(user.getRollOrEmpId())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
