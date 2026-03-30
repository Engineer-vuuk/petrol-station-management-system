package com.sadop.energymanagement.dto;

import com.sadop.energymanagement.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Role role;
    private String fullName;
    private String message;
    private Long branchId;       // ✅ Add this
    private String branchName;   // ✅ Optional (if you want branch name too)
}
