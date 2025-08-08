package com.example.dto.requests;

import lombok.Data;

@Data
public class CreateOperatorRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
}