package com.example.model;

import jakarta.persistence.*;

@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int seatNumber;
    
    @ManyToOne
    private Bus bus;
    
    private boolean isBooked;
}
