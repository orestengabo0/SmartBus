package com.example.model;

import jakarta.persistence.*;

@Entity
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;
    private int totalSeats;

    @ManyToOne
    private BusPark currentBusPark;

    private String operatorName;
}

