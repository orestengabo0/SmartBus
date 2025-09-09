package com.example.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RouteRequest {
    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

//    @Min(value = 0, message = "Distance must be positive")
    private double distanceKm;

//    @Min(value = 0, message = "Price must be positive")
    private double price;

//    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int estimatedDurationMinutes;

    private boolean autoCalculateDistance = true;
}
