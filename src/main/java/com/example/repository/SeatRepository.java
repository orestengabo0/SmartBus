package com.example.repository;

import com.example.model.Bus;
import com.example.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByBus(Bus bus);
    Optional<Seat> findByBusAndSeatNumber(Bus bus, int seatNumber);
    List<Seat> findByBusAndIsBooked(Bus bus, boolean isBooked);
}
