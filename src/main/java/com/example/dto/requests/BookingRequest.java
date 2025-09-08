package com.example.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Integer> seatNumbers;
}
