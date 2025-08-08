package com.example.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BusParkResponse {
    private Long id;
    private String name;
    private String location;
    private String address;
    private String contactNumber;

    // Summary information instead of full collections
    private int busCount;
    private int departureTripCount;
    private int arrivalTripCount;

    private double latitude;
    private double longitude;
    private boolean active;
    private LocalDateTime createdAt;
}