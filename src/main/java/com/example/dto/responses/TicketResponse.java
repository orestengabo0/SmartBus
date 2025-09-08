package com.example.dto.responses;

import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {
    private Long ticketId;
    private String ticketNumber;
    private LocalDateTime issueTime;
    private String formattedIssueTime;
    private boolean validated;
    private LocalDateTime validationTime;
    private String formattedValidationTime;

    // Passenger info
    private Long bookingId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;

    // Trip info
    private Long tripId;
    private String origin;
    private String destination;
    private String busPlateNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String formattedDepartureTime;
    private String formattedArrivalTime;

    // Seat info
    private List<Integer> seatNumbers;
}
