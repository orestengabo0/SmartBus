package com.example.service;

import com.example.dto.responses.BookingUpdateMessage;
import com.example.dto.responses.SeatUpdateMessage;
import com.example.model.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    // Send seat updates to all clients viewing a trip
    public void sendSeatUpdate(Long tripId, Map<Integer, Boolean> seatUpdates) {
        messagingTemplate.convertAndSend(
                "/topic/trips/" + tripId + "/seats",
                new SeatUpdateMessage(tripId, seatUpdates)
        );
    }

    // Send booking status updates to specific user
    public void sendBookingUpdate(Long bookingId, BookingStatus status, String message) {
        messagingTemplate.convertAndSend(
                "/topic/bookings/" + bookingId,
                new BookingUpdateMessage(bookingId, status, message)
        );
    }
}
