package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class BusPark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String location; // City name

    private String address;
    private String contactNumber;

    @OneToMany(mappedBy = "currentBusPark")
    private List<Bus> buses;

    @OneToMany(mappedBy = "departurePark")
    private List<Trip> departureTrips;

    @OneToMany(mappedBy = "arrivalPark")
    private List<Trip> arrivalTrips;

    // New fields
    private double latitude;
    private double longitude;
    private boolean active = true;
    private LocalDateTime createdAt;
}
