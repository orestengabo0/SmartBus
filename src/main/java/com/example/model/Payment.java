package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentMethod; // e.g., mobile money, credit card

    @OneToOne
    private Booking booking;

    private double amount;
    private String paymentStatus; // SUCCESS, FAILED
    private LocalDateTime paymentDate;
}
