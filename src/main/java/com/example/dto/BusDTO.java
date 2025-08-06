package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusDTO {
    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @Min(value = 1, message = "Bus must have at least 1 seat")
    private int totalSeats;

    @NotNull(message = "Bus park ID is required")
    private Long busParkId;

    private Long operatorId; // Required for admins, ignored for operators

    private String busModel;
    private String busType;
    private int yearOfManufacture;
}
