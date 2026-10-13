package com.college.project.dto;

import com.college.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Profile information returned for currently authenticated user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private String rollOrEmpId;
    private String phone;
    private String avatarUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
}
