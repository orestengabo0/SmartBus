package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    private double distanceKm;
    private double price;

    @OneToMany(mappedBy = "route")
    private List<Trip> trips;

    // New fields
    private int estimatedDurationMinutes;
    private boolean active = true;
    private LocalDateTime createdAt;
}

