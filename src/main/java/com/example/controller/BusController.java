package com.example.controller;

import com.example.dto.BusDTO;
import com.example.model.Bus;
import com.example.service.BusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buses")
public class BusController {
    private final BusService busService;

    @Autowired
    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(busService.getAllBuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bus> getBusById(@PathVariable Long id) {
        return ResponseEntity.ok(busService.getBusById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<Bus> createBus(@Valid @RequestBody BusDTO busDTO) {
        Bus bus = busService.createBus(busDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bus);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<Bus> updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusDTO busDTO) {
        Bus bus = busService.updateBus(id, busDTO);
        return ResponseEntity.ok(bus);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }
}
