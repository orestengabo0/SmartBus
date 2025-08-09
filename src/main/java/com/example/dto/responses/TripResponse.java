package com.example.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripResponse {
    private Long id;

    // Bus information
    private Long busId;
    private String busPlateNumber;
    private String busType;
    private int totalSeats;
    private int availableSeats;

    // Route information
    private Long routeId;
    private String origin;
    private String destination;
    private double distanceKm;

    // Park information
    private Long departureParkId;
    private String departureParkName;
    private Long arrivalParkId;
    private String arrivalParkName;

    // Time information
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String formattedDepartureTime;
    private String formattedArrivalTime;
    private int durationMinutes;
    private String formattedDuration;

    // Price
    private double amount;

    // Status
    private String status;
    private boolean active;
    private LocalDateTime createdAt;
}
