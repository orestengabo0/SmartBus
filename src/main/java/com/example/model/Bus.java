package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String plateNumber;

    private int totalSeats;

    @ManyToOne
    private BusPark currentBusPark;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private User operator; // Replacing operatorName with actual User entity

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL)
    private List<Seat> seats;

    // New fields
    private String busModel;
    private String busType; // e.g., "LUXURY", "STANDARD", "ECONOMY"
    private int yearOfManufacture;
    private boolean active = true;
    private LocalDateTime createdAt;

    // Method to initialize seats based on totalSeats
    public void initializeSeats() {
        if (seats == null) {
            seats = new ArrayList<>();
        }

        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setSeatNumber(i);
            seat.setBus(this);
            seat.setBooked(false);
            seats.add(seat);
        }
    }
}

