package com.example.repository;

import com.example.model.Booking;
import com.example.model.Trip;
import com.example.model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByTrip(Trip trip);
    List<Booking> findByTripAndStatusNot(Trip trip, String status);
    List<Booking> findByUserOrderByBookingTimeDesc(User user);
    List<Booking> findByStatusAndBookingTimeBefore(String status, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking b WHERE b.trip = :trip AND b.status != 'CANCELED' AND :seatNumber MEMBER OF b.seatNumbers")
    List<Booking> findBySeatNumber(@Param("trip") Trip trip, @Param("seatNumber") Integer seatNumber);

    List<Booking> findByStatusAndExpiryTimeBefore(String status, LocalDateTime expiryTimeBefore);
}