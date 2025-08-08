package com.example.service;

import com.example.dto.requests.RouteRequest;
import com.example.dto.responses.RouteResponse;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.model.Role;
import com.example.model.Route;
import com.example.model.User;
import com.example.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final CurrentUserService currentUserService;

    public RouteResponse createRoute(RouteRequest routeRequest) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can create routes");
        }

        // Validate
        if (routeRepository.existsByOriginAndDestination(
                routeRequest.getOrigin(), routeRequest.getDestination())) {
            throw new ResourceAlreadyExistsException("Route already exists between these locations");
        }

        Route route = new Route();
        route.setOrigin(routeRequest.getOrigin());
        route.setDestination(routeRequest.getDestination());
        route.setDistanceKm(routeRequest.getDistanceKm());
        route.setPrice(routeRequest.getPrice());
        route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
        route.setCreatedAt(LocalDateTime.now());

        routeRepository.save(route);
        return convertToDTO(route);
    }

    public List<RouteResponse> getAllRoutes() {
        List<Route> routes = routeRepository.findByActive(true);
        return routes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
    }

    public List<RouteResponse> getRoutesByOriginAndDestination(String origin, String destination) {
        List<Route> routes = routeRepository.findByOriginAndDestination(origin, destination);
        return routes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByOrigin(String origin) {
        List<Route> routes = routeRepository.findByOrigin(origin);
        return routes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByDestination(String destination) {
        List<Route> routes = routeRepository.findByDestination(destination);
        return routes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RouteResponse updateRoute(Long id, RouteRequest routeRequest) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can update routes");
        }

        Route route = getRouteById(id);

        route.setOrigin(routeRequest.getOrigin());
        route.setDestination(routeRequest.getDestination());
        route.setDistanceKm(routeRequest.getDistanceKm());
        route.setPrice(routeRequest.getPrice());
        route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());

        routeRepository.save(route);
        return convertToDTO(route);
    }

    public void deleteRoute(Long id) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can delete routes");
        }

        Route route = getRouteById(id);
        route.setActive(false); // Soft delete
        routeRepository.save(route);
    }

    public RouteResponse convertToDTO(Route route) {
        RouteResponse dto = new RouteResponse();

        dto.setId(route.getId());
        dto.setOrigin(route.getOrigin());
        dto.setDestination(route.getDestination());
        dto.setDistanceKm(route.getDistanceKm());
        dto.setPrice(route.getPrice());

        // Set count instead of collection
        dto.setTripCount(route.getTrips() != null ? route.getTrips().size() : 0);

        dto.setEstimatedDurationMinutes(route.getEstimatedDurationMinutes());
        dto.setActive(route.isActive());
        dto.setCreatedAt(route.getCreatedAt());

        // Format duration nicely
        int hours = route.getEstimatedDurationMinutes() / 60;
        int minutes = route.getEstimatedDurationMinutes() % 60;
        if (hours > 0) {
            dto.setFormattedDuration(hours + "h " + (minutes > 0 ? minutes + "m" : ""));
        } else {
            dto.setFormattedDuration(minutes + "m");
        }

        return dto;
    }
}
