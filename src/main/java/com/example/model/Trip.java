package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Bus bus;

    @ManyToOne
    private Route route;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private double amount;
}

