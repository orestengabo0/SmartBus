package com.example.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteDistanceInfo {
    private double distanceKm;
    private long durationMinutes;
    private double suggestedPrice;
}
