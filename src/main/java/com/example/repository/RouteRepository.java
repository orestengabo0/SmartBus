package com.example.repository;

import com.example.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOriginAndDestination(String origin, String destination);
    boolean existsByOriginAndDestination(String origin, String destination);
}
