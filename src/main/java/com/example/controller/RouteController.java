package com.example.controller;

import com.example.dto.requests.RouteRequest;
import com.example.dto.responses.RouteResponse;
import com.example.model.Route;
import com.example.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Route Management", description = "APIs for managing travel routes between locations")
@SecurityRequirement(name = "bearer-jwt")
public class RouteController {
    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(summary = "Get all routes",
            description = "Retrieves a list of all active routes in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved routes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @Operation(summary = "Get route by ID",
            description = "Retrieves a specific route by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the route"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(
            @Parameter(description = "ID of the route to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @Operation(summary = "Search routes",
            description = "Search routes by origin and/or destination. Returns all routes if no parameters provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching routes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> searchRoutes(
            @Parameter(description = "Origin city/location") @RequestParam(required = false) String origin,
            @Parameter(description = "Destination city/location") @RequestParam(required = false) String destination) {

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

    @Operation(summary = "Create a new route",
            description = "Creates a new route with the provided details. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Route created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or route already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest routeRequest) {
        return ResponseEntity.ok(routeService.createRoute(routeRequest));
    }

    @Operation(summary = "Update a route",
            description = "Updates an existing route with new details. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(
            @Parameter(description = "ID of the route to update") @PathVariable Long id,
            @Valid @RequestBody RouteRequest routeRequest) {
        return ResponseEntity.ok(routeService.updateRoute(id, routeRequest));
    }

    @Operation(summary = "Delete a route",
            description = "Soft deletes a route from the system. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Route deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRoute(
            @Parameter(description = "ID of the route to delete") @PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}