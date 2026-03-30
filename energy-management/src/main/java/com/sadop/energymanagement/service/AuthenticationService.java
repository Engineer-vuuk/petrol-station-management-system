package com.sadop.energymanagement.service;

import com.sadop.energymanagement.dto.LoginRequest;
import com.sadop.energymanagement.dto.LoginResponse;
import com.sadop.energymanagement.dto.RegisterRequest;
import com.sadop.energymanagement.model.Branch;
import com.sadop.energymanagement.model.Role;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.BranchRepository;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BranchRepository branchRepository;
    private final AuditService auditService;

    public String register(RegisterRequest request) {
        boolean exists = userRepository.findByEmail(request.getEmail()).isPresent();
        if (exists) {
            auditService.logActivity(
                    "SYSTEM",
                    "REGISTER_FAILED",
                    "Attempted to register duplicate email: " + request.getEmail(),
                    "User",
                    request.getEmail()
            );
            return "Email already registered.";
        }

        Branch branch = null;

        // ✅ CEO does not require a branch
        if (request.getRole() != Role.ROLE_CEO) {
            if (request.getBranchId() == null) {
                throw new RuntimeException("Branch ID is required for non-CEO roles.");
            }
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found."));
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .branch(branch)
                .build();

        userRepository.save(user);

        auditService.logActivity(
                "SYSTEM",
                "REGISTER_USER",
                "Registered new user: " + request.getEmail() + " with role " + request.getRole(),
                "User",
                request.getEmail()
        );

        return "User registered successfully.";
    }

    public LoginResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        auditService.logActivity(
                                request.getEmail(),
                                "LOGIN_FAILED",
                                "Login failed: User not found",
                                "Auth",
                                request.getEmail()
                        );
                        return new RuntimeException("User not found");
                    });

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                auditService.logActivity(
                        request.getEmail(),
                        "LOGIN_FAILED",
                        "Login failed: Invalid credentials",
                        "Auth",
                        request.getEmail()
                );
                throw new RuntimeException("Invalid credentials");
            }

            String token = jwtService.generateToken(user);

            Long branchId = user.getBranch() != null ? user.getBranch().getId() : null;
            String branchName = user.getBranch() != null ? user.getBranch().getBranchName() : null;

            auditService.logActivity(
                    request.getEmail(),
                    "LOGIN_SUCCESS",
                    "User logged in successfully",
                    "Auth",
                    request.getEmail()
            );

            return LoginResponse.builder()
                    .token(token)
                    .role(user.getRole())
                    .fullName(user.getFullName())
                    .message("Login successful")
                    .branchId(branchId)
                    .branchName(branchName)
                    .build();

        } catch (RuntimeException e) {
            // Already logged in failure block above
            throw e;
        }
    }
}
