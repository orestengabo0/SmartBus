package com.example.repository;

import com.example.model.Booking;
import com.example.model.BookingStatus;
import com.example.model.Trip;
import com.example.model.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByTrip(Trip trip);

    @Query("SELECT b FROM Booking b WHERE b.trip = :trip AND b.status != 'CANCELLED' AND :seatNumber MEMBER OF b.seatNumbers")
    List<Booking> findBySeatNumber(@Param("trip") Trip trip, @Param("seatNumber") Integer seatNumber);

    List<Booking> findByStatusAndExpiryTimeBefore(BookingStatus status, LocalDateTime expiryTimeBefore);
}