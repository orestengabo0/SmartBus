package com.example.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusParkRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    private String address;
    private String contactNumber;
    private double latitude;
    private double longitude;
}