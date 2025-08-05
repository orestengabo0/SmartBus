package com.example.repository;

import com.example.model.Booking;
import com.example.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    List<Payment> findByPaymentStatus(String status);
}
