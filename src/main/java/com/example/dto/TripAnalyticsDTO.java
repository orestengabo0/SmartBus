package com.example.dto;

import java.time.LocalDateTime;

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
