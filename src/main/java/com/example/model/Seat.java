package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int seatNumber;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    private boolean isBooked;

    // New fields for seat configuration
    private String seatType; // e.g., "WINDOW", "AISLE", "MIDDLE"
    private String deck; // For double-decker buses: "UPPER", "LOWER"
    private double additionalPrice; // Premium for certain seats
}
