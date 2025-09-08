package com.example.controller;

import com.example.dto.SeatAvailabilityDTO;
import com.example.dto.requests.BookingRequest;
import com.example.dto.responses.BookingResponse;
import com.example.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "APIs for managing bookings")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/seats/{tripId}")
    @Operation(summary = "Get seat availability for a trip")
    public ResponseEntity<SeatAvailabilityDTO> getSeatAvailability(@PathVariable Long tripId) {
        return ResponseEntity.ok(bookingService.getSeatAvailability(tripId));
    }

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get current user's bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getUserBookings());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
