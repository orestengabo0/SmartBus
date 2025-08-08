package com.example.dto.requests;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
}