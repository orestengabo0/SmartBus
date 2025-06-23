package com.example.model;

import jakarta.persistence.*;

@Entity
public class BusPark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location; // city, address
}
