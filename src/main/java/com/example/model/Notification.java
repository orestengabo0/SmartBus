package com.example.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @ManyToOne
    private User user;

    private boolean read;
    private LocalDateTime timestamp;
}
