package com.example.dto.requests;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
} 