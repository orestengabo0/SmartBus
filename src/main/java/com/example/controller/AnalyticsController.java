package com.example.controller;

import com.example.dto.RouteAnalyticsDTO;
import com.example.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics API", description = "API to fetch analytics data")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/routes")
    @Operation(summary = "API to fetch routes analytics")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<RouteAnalyticsDTO>> getRouteAnalytics() {
        return ResponseEntity.ok(analyticsService.getRouteAnalytics());
    }
}
