package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.AuditLog;
import com.sadop.energymanagement.model.Role;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.AuditLogRepository;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    public UserController(UserService userService,
                          AuditLogRepository auditLogRepository,
                          UserRepository userRepository,
                          HttpServletRequest request) {
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.request = request;
    }

    @PostMapping("/register")
    public Map<String, Object> registerUser(@RequestBody Map<String, String> payload) {
        String fullName = payload.get("fullName");
        String email = payload.get("email");
        String phone = payload.get("phone");
        String password = payload.get("password");
        String roleStr = payload.get("role");

        // Get logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElse(null);
        String username = currentUser != null ? currentUser.getFullName() : "Anonymous";

        try {
            Role role = Role.valueOf(roleStr);

            String message = userService.registerUser(fullName, email, phone, password, role);
            boolean success = message.equals("User registered successfully");

            // --- Audit log for successful creation ---
            if (success) {
                AuditLog log = new AuditLog();
                log.setUsername(username);
                log.setAction("CREATE_USER");
                log.setMethod("UserController.registerUser");
                log.setPath(request.getRequestURI());
                log.setIpAddress(request.getRemoteAddr());
                log.setEntity("User");
                log.setEntityId(email); // use email since id is only after save
                log.setDetails(String.format("Created user %s with role %s", fullName, role.name()));
                log.setTimestamp(LocalDateTime.now());
                auditLogRepository.save(log);
            }

            return Map.of(
                    "success", success,
                    "message", message
            );

        } catch (IllegalArgumentException e) {
            // Invalid role selected
            logFailedAttempt(username, fullName, roleStr, "Invalid role selected");
            return Map.of(
                    "success", false,
                    "message", "Invalid role selected"
            );
        } catch (RuntimeException e) {
            // Role restrictions or other runtime errors
            logFailedAttempt(username, fullName, roleStr, e.getMessage());
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        } catch (Exception e) {
            // Any other unexpected errors
            logFailedAttempt(username, fullName, roleStr, "Server error");
            return Map.of(
                    "success", false,
                    "message", "Server error"
            );
        }
    }

    // Helper method to log failed registration attempts
    private void logFailedAttempt(String username, String fullName, String attemptedRole, String reason) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction("FAILED_CREATE_USER");
        log.setMethod("UserController.registerUser");
        log.setPath(request.getRequestURI());
        log.setIpAddress(request.getRemoteAddr());
        log.setEntity("User");
        log.setEntityId(fullName); // can use fullName if id not created
        log.setDetails(String.format("Attempted to create user with role %s. Reason: %s", attemptedRole, reason));
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}