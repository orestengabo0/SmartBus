package com.example.dto.responses;

import com.example.model.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;

    // User info
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;

    // Trip info
    private Long tripId;
    private String origin;
    private String destination;
    private String busPlateNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String formattedDepartureTime;
    private String formattedArrivalTime;

    // Booking details
    private List<Integer> seatNumbers;
    private int seatCount;
    private double totalAmount;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED
    private LocalDateTime bookingTime;
    private String formattedBookingTime;
    private LocalDateTime expiryTime;
    private String formattedExpiryTime;

    // Payment info
    private boolean paid;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentTime;

    // Ticket info
    private boolean hasTicket;
    private String ticketNumber;
}
