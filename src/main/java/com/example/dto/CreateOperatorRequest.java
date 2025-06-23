package com.example.dto;

import lombok.Data;

@Data
public class CreateOperatorRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
}