package com.example.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SeatUpdateMessage {
    private Long tripId;
    private Map<Integer, Boolean> seatUpdates; // seatNumber -> isAvailable
}
