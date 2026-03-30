package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.model.Role;
import com.sadop.energymanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerUser(String fullName, String email, String phone, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return "Email already registered";
        }

        // ✅ Get the logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        // --- Role-based validation ---
        Role creatorRole = currentUser.getRole();

        if (creatorRole == Role.ROLE_MANAGER) {
            // Managers cannot create CEOs or other managers
            if (role == Role.ROLE_MANAGER || role == Role.ROLE_CEO) {
                throw new RuntimeException("Managers can only create Attendants or Assistant Managers");
            }
        } else if (creatorRole == Role.ROLE_ASSISTANT_MANAGER) {
            throw new RuntimeException("Assistant Managers cannot create users");
        }
        // CEOs can create anyone → no restriction needed

        // Assign the same branch as the creator
        User newUser = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .branch(currentUser.getBranch())
                .build();

        userRepository.save(newUser);

        return "User registered successfully";
    }
}