package com.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;
    
    // Add these fields to match BusPark mappings
    @ManyToOne
    @JoinColumn(name = "departure_park_id")
    private BusPark departurePark;
    
    @ManyToOne
    @JoinColumn(name = "arrival_park_id")
    private BusPark arrivalPark;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    
    // Price information
    private double amount;
    
    // Trip status management
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    
    // Tracking
    @OneToMany(mappedBy = "trip")
    private List<Booking> bookings = new ArrayList<>();
    
    private int availableSeats;
    private boolean active = true;
    private LocalDateTime createdAt;
    
    // Utility method to check seat availability
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }
    
    // Method to update available seats count
    public void updateAvailableSeats() {
        if (bus != null) {
            int bookedSeats = 0;
            if (bookings != null) {
                bookedSeats = (int) bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .count();
            }
            this.availableSeats = bus.getTotalSeats() - bookedSeats;
        }
    }
}