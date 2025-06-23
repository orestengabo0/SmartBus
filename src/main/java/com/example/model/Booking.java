package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Trip trip;

    private int seatNumber;

    private double price;
    private LocalDateTime bookingTime;

    private String status; // PENDING, CONFIRMED, CANCELLED
}
