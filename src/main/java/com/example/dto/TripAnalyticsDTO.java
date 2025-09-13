package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TripAnalyticsDTO {
    private Long tripId;
    private String busPlateNumber;
    private String routeName; // e.g. "Kigali → Butare"
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int totalSeats;
    private int bookedSeats;
    private double occupancyRate; // bookedSeats / totalSeats
    private double revenue;
}
