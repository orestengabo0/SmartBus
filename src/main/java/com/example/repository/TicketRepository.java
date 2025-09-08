package com.example.repository;

import com.example.model.Booking;
import com.example.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByBooking(Booking booking);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
