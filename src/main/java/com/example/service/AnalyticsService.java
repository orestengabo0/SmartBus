package com.example.service;

import com.example.dto.RouteAnalyticsDTO;
import com.example.exception.UnauthorizedException;
import com.example.model.*;
import com.example.repository.RouteRepository;
import com.example.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final RouteRepository routeRepository;
    private final CurrentUserService currentUserService;
    private final TripRepository tripRepository;

    public List<RouteAnalyticsDTO> getRouteAnalytics() {
        User user = currentUserService.getCurrentUser();
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("You do not have permission to access this resource");
        }
        List<Route> routes = routeRepository.findAll();

        return routes.stream()
                .map(route -> {
                    List<Trip> trips = route.getTrips();
                    List<Booking> bookings = trips.stream()
                            .flatMap(t -> t.getBookings().stream())
                            .toList();

                    var totalBookings = bookings.size();
                    long cancelled = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
                    long confirmed = bookings.stream().filter(t -> t.getStatus() == BookingStatus.CONFIRMED).count();
                    long pending = bookings.stream().filter(t -> t.getStatus() == BookingStatus.PENDING).count();

                    double totalRevenue = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                            .mapToDouble(Booking::getTotalAmount)
                            .sum();
                    double avgRevenuePerTrip = trips.isEmpty() ? 0 : totalRevenue / trips.size();
                    double avgRevenuePerBooking = (confirmed == 0) ? 0 : totalRevenue / bookings.size();

                    double averageOccupancy = trips.stream()
                            .mapToDouble( trip -> {
                                int totalSeats = trip.getBus().getTotalSeats();
                                int bookedSeats = (int)trip.getBookings().stream()
                                        .filter( b -> b.getStatus() == BookingStatus.CONFIRMED).count();

                                return totalSeats > 0 ? (double) bookedSeats / totalSeats : 0;
                                    }
                            ).average().orElse(0);

                    return new RouteAnalyticsDTO(
                            route.getId(),
                            route.getOrigin() + " -> "+route.getDestination(),
                            totalBookings,
                            confirmed,
                            pending,
                            cancelled,
                            totalRevenue,
                            avgRevenuePerTrip,
                            avgRevenuePerBooking,
                            averageOccupancy,
                            trips.size()
                    );
                }).toList();
    }
}
