package com.example.dto.responses;

import com.example.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingUpdateMessage {
    private Long bookingId;
    private BookingStatus status;
    private String message;
}
