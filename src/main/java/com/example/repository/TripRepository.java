package com.example.repository;

import com.example.model.Trip;
import com.example.model.Bus;
import com.example.model.Route;
import com.example.model.BusPark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByBus(Bus bus);
    List<Trip> findByRoute(Route route);
    List<Trip> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Trip> findByDepartureParkAndDepartureTimeBetween(BusPark departurePark, LocalDateTime start, LocalDateTime end);
    List<Trip> findByArrivalParkAndDepartureTimeBetween(BusPark arrivalPark, LocalDateTime start, LocalDateTime end);
    List<Trip> findByRouteAndDepartureTimeBetween(Route route, LocalDateTime start, LocalDateTime end);
    List<Trip> findByStatusAndDepartureTimeBefore(String status, LocalDateTime time);
    List<Trip> findByActiveAndDepartureTimeAfter(boolean active, LocalDateTime time);

    @Query("SELECT t FROM Trip t WHERE t.route.origin = :origin AND t.route.destination = :destination AND t.departureTime BETWEEN :startTime AND :endTime AND t.active = true AND t.status = 'SCHEDULED'")
    List<Trip> findTripsForSearch(String origin, String destination, LocalDateTime startTime, LocalDateTime endTime);
}