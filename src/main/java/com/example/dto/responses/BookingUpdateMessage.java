package com.example.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingUpdateMessage {
    private Long bookingId;
    private String status;
    private String message;
}
