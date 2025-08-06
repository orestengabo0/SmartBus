package com.example.controller;

import com.example.dto.RouteDTO;
import com.example.model.Route;
import com.example.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Route>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {

        if (origin != null && destination != null) {
            return ResponseEntity.ok(routeService.getRoutesByOriginAndDestination(origin, destination));
        } else if (origin != null) {
            return ResponseEntity.ok(routeService.getRoutesByOrigin(origin));
        } else if (destination != null) {
            return ResponseEntity.ok(routeService.getRoutesByDestination(destination));
        } else {
            return ResponseEntity.ok(routeService.getAllRoutes());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Route> createRoute(@Valid @RequestBody RouteDTO routeDTO) {
        Route route = routeService.createRoute(routeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Route> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteDTO routeDTO) {
        Route route = routeService.updateRoute(id, routeDTO);
        return ResponseEntity.ok(route);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
