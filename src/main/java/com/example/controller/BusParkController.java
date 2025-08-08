package com.example.controller;

import com.example.dto.requests.BusParkRequest;
import com.example.dto.responses.BusParkResponse;
import com.example.service.BusParkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buspark")
@RequiredArgsConstructor
@Tag(name = "Bus Park Management", description = "APIs for managing bus terminals and stations")
@SecurityRequirement(name = "bearer-jwt")
public class BusParkController {
    private final BusParkService busParkService;

    @Operation(summary = "Get all bus parks",
            description = "Retrieves a list of all active bus parks in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bus parks"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<List<BusParkResponse>> getAllBusParks() {
        return ResponseEntity.ok(busParkService.getAllBusParks());
    }

    @Operation(summary = "Get bus park by ID",
            description = "Retrieves a specific bus park by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the bus park"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "404", description = "Bus park not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BusParkResponse> getBusParkById(@PathVariable Long id) {
        return ResponseEntity.ok(busParkService.getBusParkById(id));
    }

    @Operation(summary = "Get bus parks by location",
            description = "Retrieves all bus parks in a specific city or location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bus parks"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping("/location/{location}")
    public ResponseEntity<List<BusParkResponse>> getBusParksByLocation(@PathVariable String location) {
        return ResponseEntity.ok(busParkService.getBusParksByLocation(location));
    }

    @Operation(summary = "Create a new bus park",
            description = "Creates a new bus park with the provided details. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bus park created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BusParkResponse> createBusPark(@Valid @RequestBody BusParkRequest busParkDTO) {
        return ResponseEntity.ok(busParkService.createBusPark(busParkDTO));
    }

    @Operation(summary = "Update a bus park",
            description = "Updates an existing bus park. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus park updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
            @ApiResponse(responseCode = "404", description = "Bus park not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BusParkResponse> updateBusPark(
            @PathVariable Long id,
            @Valid @RequestBody BusParkRequest busParkDTO) {
        return ResponseEntity.ok(busParkService.updateBusPark(id, busParkDTO));
    }

    @Operation(summary = "Delete a bus park",
            description = "Soft deletes a bus park. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bus park deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
            @ApiResponse(responseCode = "404", description = "Bus park not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBusPark(@PathVariable Long id) {
        busParkService.deleteBusPark(id);
        return ResponseEntity.noContent().build();
    }
}