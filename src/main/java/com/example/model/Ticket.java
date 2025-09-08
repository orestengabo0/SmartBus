package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String ticketNumber;
    private byte[] qrcode;
    private LocalDateTime issueTime;
    private boolean validated;
    private LocalDateTime validationTime;
}
