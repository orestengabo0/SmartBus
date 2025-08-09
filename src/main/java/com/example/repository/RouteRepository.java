package com.example.repository;

import com.example.model.Route;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOriginAndDestination(String origin, String destination);
    boolean existsByOriginAndDestination(String origin, String destination);

    List<Route> findByActive(boolean active);

    List<Route> findByOrigin(String origin);

    List<Route> findByDestination(@NotBlank String destination);

    Route getRouteById(Long id);
}
