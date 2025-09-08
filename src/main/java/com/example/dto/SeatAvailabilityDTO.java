package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
public class SeatAvailabilityDTO {
    private Long tripId;
    private int totalSeats;
    private int availableSeats;
    private Map<Integer, Boolean> seatStatus; // seatNumber -> isAvailable
}

