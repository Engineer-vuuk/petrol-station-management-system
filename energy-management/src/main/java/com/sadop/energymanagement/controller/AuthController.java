package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.dto.LoginRequest;
import com.sadop.energymanagement.dto.LoginResponse;
import com.sadop.energymanagement.dto.RegisterRequest;
import com.sadop.energymanagement.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String response = authenticationService.register(request);

        if (response.equals("User registered successfully.")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .token(null)
                            .role(null)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
