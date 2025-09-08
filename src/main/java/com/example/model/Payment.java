package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String paymentMethod; // e.g., mobile money, credit card
    private String transactionId;
    private double amount;
    private String status; // SUCCESS, FAILED
    private LocalDateTime paymentTime;
}
