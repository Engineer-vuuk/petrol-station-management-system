package com.sadop.energymanagement.dto;

import com.sadop.energymanagement.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private Long branchId; // matches your frontend field
}