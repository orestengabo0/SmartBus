package com.example.controller;

import com.example.dto.BusParkDTO;
import com.example.model.BusPark;
import com.example.service.BusParkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buspark")
@RequiredArgsConstructor
public class BusParkController {
    private final BusParkService busParkService;

    @GetMapping
    public ResponseEntity<List<BusPark>> getAllBusParks() {
        return ResponseEntity.ok(busParkService.getAllBusParks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusPark> getBusParkById(@PathVariable Long id) {
        return ResponseEntity.ok(busParkService.getBusParkById(id));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<BusPark>> getBusParksByLocation(@PathVariable String location) {
        return ResponseEntity.ok(busParkService.getBusParksByLocation(location));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BusPark> createBusPark(@Valid @RequestBody BusParkDTO busParkDTO) {
        BusPark busPark = busParkService.createBusPark(busParkDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(busPark);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BusPark> updateBusPark(
            @PathVariable Long id,
            @Valid @RequestBody BusParkDTO busParkDTO) {
        BusPark busPark = busParkService.updateBusPark(id, busParkDTO);
        return ResponseEntity.ok(busPark);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBusPark(@PathVariable Long id) {
        busParkService.deleteBusPark(id);
        return ResponseEntity.noContent().build();
    }
}
