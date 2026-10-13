package com.college.project.dto;

import com.college.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned upon successful login or registration,
 * containing the JWT token and basic user identity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String department;
    private String rollOrEmpId;
    private String avatarUrl;
}
