package com.example.controller;

import com.example.dto.requests.BusRequest;
import com.example.dto.responses.BusResponse;
import com.example.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buses")
@Tag(name = "Bus Management", description = "APIs for managing buses")
@SecurityRequirement(name = "bearer-jwt")
public class BusController {
    private final BusService busService;

    @Autowired
    public BusController(BusService busService) {
        this.busService = busService;
    }

    @Operation(summary = "Get all buses",
            description = "Retrieves all buses. Operators can only view their own buses while admins can view all buses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved buses"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<List<BusResponse>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @Operation(summary = "Get bus by ID",
            description = "Retrieves a specific bus by its ID. Operators can only access their own buses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the bus"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - no access to this bus"),
            @ApiResponse(responseCode = "404", description = "Bus not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> getBusById(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @Operation(summary = "Create a new bus",
            description = "Creates a new bus with the provided details. Operators can only create buses for themselves.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bus created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody BusRequest busRequest) {
        return ResponseEntity.ok(busService.createBus(busRequest));
    }

    @Operation(summary = "Update a bus",
            description = "Updates an existing bus. Operators can only update their own buses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bus updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - no access to this bus"),
            @ApiResponse(responseCode = "404", description = "Bus not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<BusResponse> updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusRequest busRequest) {
        return ResponseEntity.ok(busService.updateBus(id, busRequest));
    }

    @Operation(summary = "Delete a bus",
            description = "Soft deletes a bus. Operators can only delete their own buses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bus deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - no access to this bus"),
            @ApiResponse(responseCode = "404", description = "Bus not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }
}