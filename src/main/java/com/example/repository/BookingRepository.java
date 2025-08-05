package com.example.repository;

import com.example.model.Booking;
import com.example.model.Trip;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByTrip(Trip trip);
    List<Booking> findByTripAndStatus(Trip trip, String status);
    List<Booking> findByStatusAndBookingTimeBefore(String status, LocalDateTime dateTime);
}