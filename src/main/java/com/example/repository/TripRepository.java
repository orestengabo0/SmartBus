package com.example.repository;

import com.example.model.Bus;
import com.example.model.Route;
import com.example.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByBus(Bus bus);
    List<Trip> findByRoute(Route route);
    List<Trip> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Trip> findByRouteAndDepartureTimeBetween(
            Route route, LocalDateTime start, LocalDateTime end);
}
