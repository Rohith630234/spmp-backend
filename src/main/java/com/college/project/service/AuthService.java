package com.college.project.service;

import com.college.project.dto.AuthResponse;
import com.college.project.dto.LoginRequest;
import com.college.project.dto.RegisterRequest;
import com.college.project.dto.UserProfileResponse;
import com.college.project.exception.BadRequestException;
import com.college.project.exception.ResourceNotFoundException;
import com.college.project.model.User;
import com.college.project.repository.UserRepository;
import com.college.project.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user authentication, registration, and profile queries.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticates user credentials and generates a signed JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        return mapToAuthResponse(user, token);
    }

    /**
     * Registers a new student, guide, or admin account.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists: " + email);
        }

        if (userRepository.existsByRollOrEmpId(request.getRollOrEmpId().trim())) {
            throw new BadRequestException("Roll Number or Employee ID is already registered: " + request.getRollOrEmpId());
        }

        String avatarUrl = request.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + request.getFullName().replaceAll("\\s+", "");
        }

        User newUser = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment().trim())
                .rollOrEmpId(request.getRollOrEmpId().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .avatarUrl(avatarUrl)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // Auto authenticate after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return mapToAuthResponse(savedUser, token);
    }

    /**
     * Fetches current authenticated user's profile details.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

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

    private AuthResponse mapToAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .rollOrEmpId(user.getRollOrEmpId())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
