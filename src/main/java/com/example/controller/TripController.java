package com.example.controller;

import com.example.dto.requests.TripRequest;
import com.example.dto.responses.TripResponse;
import com.example.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@Tag(name = "Trip Management", description = "APIs for scheduling and managing bus trips")
@SecurityRequirement(name = "bearer-jwt")
public class TripController {
    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @Operation(summary = "Create a new trip schedule",
            description = "Schedule a new trip for a specific bus on a route")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trip scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Unauthorized to schedule this bus"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripRequest tripRequest) {
        TripResponse trip = tripService.createTrip(tripRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    @Operation(summary = "Search for trips",
            description = "Search for trips by origin, destination and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<List<TripResponse>> searchTrips(
            @Parameter(description = "Origin location") @RequestParam String origin,
            @Parameter(description = "Destination location") @RequestParam String destination,
            @Parameter(description = "Travel date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TripResponse> trips = tripService.searchTrips(origin, destination, date);
        return ResponseEntity.ok(trips);
    }

    @Operation(summary = "Get trip details",
            description = "Get detailed information about a specific trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip details retrieved"),
            @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id) {
        TripResponse trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    @Operation(summary = "Get upcoming trips",
            description = "Get list of upcoming trips. For operators, shows only their buses' trips.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trips retrieved successfully")
    })
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<List<TripResponse>> getUpcomingTrips() {
        List<TripResponse> trips = tripService.getUpcomingTrips();
        return ResponseEntity.ok(trips);
    }

    @Operation(summary = "Update trip details",
            description = "Update schedule or details of a trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this trip"),
            @ApiResponse(responseCode = "404", description = "Trip not found"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest tripRequest) {
        TripResponse trip = tripService.updateTrip(id, tripRequest);
        return ResponseEntity.ok(trip);
    }

    @Operation(summary = "Cancel a trip",
            description = "Cancel an upcoming trip")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trip cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Trip cannot be cancelled"),
            @ApiResponse(responseCode = "403", description = "Not authorized to cancel this trip"),
            @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<TripResponse> cancelTrip(@PathVariable Long id) {
        TripResponse trip = tripService.cancelTrip(id);
        return ResponseEntity.ok(trip);
    }
}