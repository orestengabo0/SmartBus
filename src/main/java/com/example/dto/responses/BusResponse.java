package com.example.dto.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BusResponse {
    private Long id;
    private String plateNumber;
    private int totalSeats;

    // BusPark info without circular references
    private Long busParkId;
    private String busParkName;
    private String busParkLocation;

    // Operator info without circular references
    private Long operatorId;
    private String operatorName;
    private String operatorEmail;

    // Seat summary
    private int availableSeats;
    private int bookedSeats;

    // Bus details
    private String busModel;
    private String busType;
    private int yearOfManufacture;
    private boolean active;
    private LocalDateTime createdAt;
}