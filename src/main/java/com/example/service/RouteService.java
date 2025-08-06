package com.example.service;

import com.example.dto.RouteDTO;
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

@Service
@Transactional
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final CurrentUserService currentUserService;

    public Route createRoute(RouteDTO routeDTO) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can create routes");
        }

        // Validate
        if (routeRepository.existsByOriginAndDestination(
                routeDTO.getOrigin(), routeDTO.getDestination())) {
            throw new ResourceAlreadyExistsException("Route already exists between these locations");
        }

        Route route = new Route();
        route.setOrigin(routeDTO.getOrigin());
        route.setDestination(routeDTO.getDestination());
        route.setDistanceKm(routeDTO.getDistanceKm());
        route.setPrice(routeDTO.getPrice());
        route.setEstimatedDurationMinutes(routeDTO.getEstimatedDurationMinutes());
        route.setCreatedAt(LocalDateTime.now());

        return routeRepository.save(route);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findByActive(true);
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
    }

    public List<Route> getRoutesByOriginAndDestination(String origin, String destination) {
        return routeRepository.findByOriginAndDestination(origin, destination);
    }

    public List<Route> getRoutesByOrigin(String origin) {
        return routeRepository.findByOrigin(origin);
    }

    public List<Route> getRoutesByDestination(String destination) {
        return routeRepository.findByDestination(destination);
    }

    public Route updateRoute(Long id, RouteDTO routeDTO) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can update routes");
        }

        Route route = getRouteById(id);

        route.setOrigin(routeDTO.getOrigin());
        route.setDestination(routeDTO.getDestination());
        route.setDistanceKm(routeDTO.getDistanceKm());
        route.setPrice(routeDTO.getPrice());
        route.setEstimatedDurationMinutes(routeDTO.getEstimatedDurationMinutes());

        return routeRepository.save(route);
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
}
