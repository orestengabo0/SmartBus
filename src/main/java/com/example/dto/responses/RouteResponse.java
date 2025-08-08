package com.example.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RouteResponse {
    private Long id;
    private String origin;
    private String destination;
    private double distanceKm;
    private double price;

    // Summary information instead of full collection
    private int tripCount;

    private int estimatedDurationMinutes;
    private boolean active;
    private LocalDateTime createdAt;

    // Computed fields
    private String formattedDuration; // e.g., "3h 45m"
}