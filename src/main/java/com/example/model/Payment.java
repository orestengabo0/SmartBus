package com.example.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // e.g., mobile money, credit card
    private String transactionId;
    private double amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS, FAILED
    private LocalDateTime paymentTime;
}
