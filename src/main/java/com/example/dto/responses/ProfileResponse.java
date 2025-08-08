package com.example.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}