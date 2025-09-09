package com.example.service;

import com.example.dto.requests.RouteRequest;
import com.example.dto.responses.RouteDistanceInfo;
import com.example.dto.responses.RouteResponse;
import com.example.exception.BadRequestException;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.model.Role;
import com.example.model.Route;
import com.example.model.User;
import com.example.repository.RouteRepository;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.TravelMode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${google.maps.api.key}")
    private String apiKey;
    @Value("${route.pricing.base-rate-per-km:0.50}")
    private double baseRatePerKm;

    @Value("${route.pricing.minimum-price:5.00}")
    private double minimumPrice;

    private GeoApiContext context;

    @PostConstruct
    public void init() {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    @PreDestroy
    public void cleanup() {
        context.shutdown();
    }

    public RouteResponse createRoute(RouteRequest routeRequest) {
        User currentUser = currentUserService.getCurrentUser();
        if(currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("You do not have permission to access this resource");
        }
        if(routeRepository.existsByOriginAndDestination(routeRequest.getOrigin(), routeRequest.getDestination())) {
            throw new ResourceAlreadyExistsException("Route already exists between these locations");
        }
        Route route = new Route();
        route.setOrigin(routeRequest.getOrigin());
        route.setDestination(routeRequest.getDestination());

        // Handle distance calculation
        if(routeRequest.isAutoCalculateDistance() || routeRequest.getDistanceKm() <= 0){
            try{
                // Calculate distance using Google Maps
                RouteDistanceInfo distanceInfo = calculateDistance(
                        routeRequest.getOrigin(),
                        routeRequest.getDestination()
                );
                route.setDistanceKm(distanceInfo.getDistanceKm());

                // Use calculated duration if not provided
                if (routeRequest.getEstimatedDurationMinutes() <= 0) {
                    route.setEstimatedDurationMinutes((int) distanceInfo.getDurationMinutes());
                } else {
                    route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
                }
            }catch(Exception e){
                // If auto-calculation fails, check if manual values were provided
                if (routeRequest.getDistanceKm() <= 0) {
                    throw new BadRequestException(
                            "Could not auto-calculate distance between '" + routeRequest.getOrigin() +
                                    "' and '" + routeRequest.getDestination() +
                                    "'. Please check the location names or provide manual distance. Error: " + e.getMessage()
                    );
                }
                // Use manual values if auto-calculation failed
                route.setDistanceKm(routeRequest.getDistanceKm());
                route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
            }
        } else {
            // Use manually provided values
            route.setDistanceKm(routeRequest.getDistanceKm());
            route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
        }
        // Handle pricing
        if (routeRequest.getPrice() <= 0) {
            // Auto-calculate price based on distance
            double calculatedPrice = calculatePrice(route.getDistanceKm());
            route.setPrice(calculatedPrice);
        } else {
            route.setPrice(routeRequest.getPrice());
        }

        route.setActive(true);
        route.setCreatedAt(LocalDateTime.now());
        Route savedRoute = routeRepository.save(route);
        return convertToDTO(savedRoute);
    }

    private double calculatePrice(double distanceKm) {
        double price = distanceKm * baseRatePerKm;
        // Ensure minimum price
        price = Math.max(price, minimumPrice);
        //round to the 2 demical price
        return Math.round(price * 100.0) / 100.0;
    }

    public RouteDistanceInfo previewRouteDistance(String origin, String destination) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can preview routes");
        }
        try {
            RouteDistanceInfo distanceInfo = calculateDistance(origin, destination);
            // Add suggested price to the response
            distanceInfo.setSuggestedPrice(calculatePrice(distanceInfo.getDistanceKm()));
            return distanceInfo;
        } catch (Exception e) {
            throw new BadRequestException(
                    "Could not calculate distance between '" + origin + "' and '" + destination +
                            "'. Error: " + e.getMessage()
            );
        }
    }

    private RouteDistanceInfo calculateDistance(String origin, String destination) {
        try{
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(context)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.DRIVING)
                    .language("en")
                    .await();

            if(matrix.rows.length > 0 &&
                    matrix.rows[0].elements.length > 0){
                DistanceMatrixElement element = matrix.rows[0].elements[0];

                if(element.status == DistanceMatrixElementStatus.OK){
                    // Convert Meters to kilometers
                    double distanceKm = element.distance.inMeters / 1000.0;
                    // Convert seconds to minutes
                    long durationMinutes = element.duration.inSeconds / 60;

                    //Round distance to 2 decimal places
                    distanceKm = Math.round(distanceKm * 100.0) / 100.0;

                    return new RouteDistanceInfo(distanceKm,durationMinutes,0.0);
                } else {
                    throw new ResourceNotFoundException("Route not found. Status: " + element.status);
                }
            }
            throw new ResourceNotFoundException("No route data found on google map.");
        }catch (Exception e){
            throw new RuntimeException("Error calculating distance"+ e.getMessage());
        }
    }

    public List<RouteResponse> getAllRoutes() {
        List<Route> routes = routeRepository.findByActive(true);
        return routes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return convertToDTO(route);
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

    public RouteResponse updateRoute(Long routeId, RouteRequest routeRequest) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can update routes");
        }

        Route route = routeRepository.findById(routeId).orElseThrow(
                () -> new ResourceNotFoundException("Route not found with id: " + routeId)
        );

        // Check if origin or destination changed
        boolean locationChanged = !route.getOrigin().equals(routeRequest.getOrigin()) ||
                !route.getDestination().equals(routeRequest.getDestination());

        route.setOrigin(routeRequest.getOrigin());
        route.setDestination(routeRequest.getDestination());

        if(locationChanged && routeRequest.isAutoCalculateDistance()){
            try{
                RouteDistanceInfo distanceInfo = calculateDistance(
                        routeRequest.getOrigin(),
                        routeRequest.getDestination()
                );

                route.setDistanceKm(distanceInfo.getDistanceKm());

                // Update duration if not manually specified
                if (routeRequest.getEstimatedDurationMinutes() <= 0) {
                    route.setEstimatedDurationMinutes((int) distanceInfo.getDurationMinutes());
                } else {
                    route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
                }
            } catch (Exception e) {
                // If calculation fails, use provided values
                if (routeRequest.getDistanceKm() > 0) {
                    route.setDistanceKm(routeRequest.getDistanceKm());
                }
                if (routeRequest.getEstimatedDurationMinutes() > 0) {
                    route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
                }
                if (routeRequest.getPrice() > 0) {
                    route.setPrice(routeRequest.getPrice());
                }
            }
        }else{
            // Use manually provided values
            if (routeRequest.getDistanceKm() > 0) {
                route.setDistanceKm(routeRequest.getDistanceKm());
            }
            if (routeRequest.getEstimatedDurationMinutes() > 0) {
                route.setEstimatedDurationMinutes(routeRequest.getEstimatedDurationMinutes());
            }
            if (routeRequest.getPrice() > 0) {
                route.setPrice(routeRequest.getPrice());
            }
        }
        Route updatedRoute = routeRepository.save(route);
        return convertToDTO(updatedRoute);
    }

    public void deleteRoute(Long id) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can delete routes");
        }

        Route route = routeRepository.getRouteById(id);
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
