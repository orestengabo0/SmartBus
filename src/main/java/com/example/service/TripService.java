package com.example.service;

import com.example.dto.requests.TripRequest;
import com.example.dto.responses.TripResponse;
import com.example.exception.ConflictException;
import com.example.exception.InvalidInputException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.model.*;
import com.example.repository.BusParkRepository;
import com.example.repository.BusRepository;
import com.example.repository.RouteRepository;
import com.example.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final BusParkRepository busParkRepository;
    private final CurrentUserService currentUserService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public TripResponse createTrip(TripRequest tripDTO) {
        // Validate user access
        User currentUser = currentUserService.getCurrentUser();

        // Load required entities
        Bus bus = busRepository.findById(tripDTO.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        // Check if user is operator of this bus
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only schedule trips for your own buses");
        }

        Route route = routeRepository.findById(tripDTO.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        BusPark departurePark = busParkRepository.findById(tripDTO.getDepartureParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure park not found"));

        BusPark arrivalPark = busParkRepository.findById(tripDTO.getArrivalParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival park not found"));

        // Validate time constraints
        if (tripDTO.getDepartureTime().isAfter(tripDTO.getArrivalTime())) {
            throw new InvalidInputException("Departure time must be before arrival time");
        }

        if (tripDTO.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Departure time must be in the future");
        }

        // Check for scheduling conflicts
        checkBusAvailability(bus, tripDTO.getDepartureTime(), tripDTO.getArrivalTime());

        // Create trip
        Trip trip = new Trip();
        trip.setBus(bus);
        trip.setRoute(route);
        trip.setDeparturePark(departurePark);
        trip.setArrivalPark(arrivalPark);
        trip.setDepartureTime(tripDTO.getDepartureTime());
        trip.setArrivalTime(tripDTO.getArrivalTime());
        trip.setAmount(route.getPrice());
        trip.setStatus("SCHEDULED");
        trip.setAvailableSeats(bus.getTotalSeats()); // Initially all seats are available
        trip.setActive(true);
        trip.setCreatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        return convertToDTO(savedTrip);
    }

    private void checkBusAvailability(Bus bus, LocalDateTime start, LocalDateTime end) {
        // Find trips that might conflict
        List<Trip> busTrips = tripRepository.findByBus(bus);

        for (Trip existingTrip : busTrips) {
            // Only check active trips with status SCHEDULED or IN_PROGRESS
            if (!existingTrip.isActive() ||
                    !(existingTrip.getStatus().equals("SCHEDULED") ||
                            existingTrip.getStatus().equals("IN_PROGRESS"))) {
                continue;
            }

            LocalDateTime existingStart = existingTrip.getDepartureTime();
            LocalDateTime existingEnd = existingTrip.getArrivalTime();

            // Check for overlap
            if ((start.isBefore(existingEnd) && end.isAfter(existingStart))) {
                throw new ConflictException("Bus is already scheduled during this time period");
            }
        }
    }

    public List<TripResponse> searchTrips(String origin, String destination, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);

        List<Trip> trips = tripRepository.findTripsForSearch(origin, destination, startOfDay, endOfDay);

        return trips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        return convertToDTO(trip);
    }

    public List<TripResponse> getUpcomingTrips() {
        User currentUser = currentUserService.getCurrentUser();
        List<Trip> trips;

        if (currentUser.getRole() == Role.OPERATOR) {
            // Get buses operated by this user
            List<Bus> operatorBuses = busRepository.findByOperator(currentUser);

            // Get trips for these buses
            trips = operatorBuses.stream()
                    .flatMap(bus -> tripRepository.findByBus(bus).stream())
                    .filter(trip -> trip.isActive() &&
                            trip.getDepartureTime().isAfter(LocalDateTime.now()) &&
                            (trip.getStatus().equals("SCHEDULED") ||
                                    trip.getStatus().equals("IN_PROGRESS")))
                    .collect(Collectors.toList());
        } else {
            // Admins see all upcoming trips
            trips = tripRepository.findByActiveAndDepartureTimeAfter(true, LocalDateTime.now());
        }

        return trips.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest tripDTO) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        System.out.println("Updating trip");

        // Validate user access
        User currentUser = currentUserService.getCurrentUser();

        // Check if user is operator of this bus
        if (currentUser.getRole() == Role.OPERATOR &&
                !trip.getBus().getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update trips for your own buses");
        }

        // Can't update trips that already departed
        if (trip.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Cannot update a trip that has already departed");
        }

        // Load required entities
        Bus bus = busRepository.findById(tripDTO.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        Route route = routeRepository.findById(tripDTO.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        BusPark departurePark = busParkRepository.findById(tripDTO.getDepartureParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure park not found"));

        BusPark arrivalPark = busParkRepository.findById(tripDTO.getArrivalParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival park not found"));

        // Validate time constraints
        if (tripDTO.getDepartureTime().isAfter(tripDTO.getArrivalTime())) {
            throw new InvalidInputException("Departure time must be before arrival time");
        }

        if (tripDTO.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Departure time must be in the future");
        }

        // If changing bus or time, check for conflicts
        if (!trip.getBus().getId().equals(bus.getId()) ||
                !trip.getDepartureTime().equals(tripDTO.getDepartureTime()) ||
                !trip.getArrivalTime().equals(tripDTO.getArrivalTime())) {
            checkBusAvailability(bus, tripDTO.getDepartureTime(), tripDTO.getArrivalTime());
        }

        // Update trip
        trip.setBus(bus);
        trip.setRoute(route);
        trip.setDeparturePark(departurePark);
        trip.setArrivalPark(arrivalPark);
        trip.setDepartureTime(tripDTO.getDepartureTime());
        trip.setArrivalTime(tripDTO.getArrivalTime());
        trip.setAmount(route.getPrice());

        Trip updatedTrip = tripRepository.save(trip);
        return convertToDTO(updatedTrip);
    }

    public TripResponse cancelTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Validate user access
        User currentUser = currentUserService.getCurrentUser();

        // Check if user is operator of this bus
        if (currentUser.getRole() == Role.OPERATOR &&
                !trip.getBus().getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only cancel trips for your own buses");
        }

        // Can't cancel trips that already departed
        if (trip.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Cannot cancel a trip that has already departed");
        }

        trip.setStatus("CANCELLED");
        Trip cancelledTrip = tripRepository.save(trip);

        // TODO: Notify booked passengers about cancellation

        return convertToDTO(cancelledTrip);
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateTripStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Update SCHEDULED trips that have departed
        List<Trip> departedTrips = tripRepository.findByStatusAndDepartureTimeBefore("SCHEDULED", now);
        for (Trip trip : departedTrips) {
            if (trip.getArrivalTime().isAfter(now)) {
                trip.setStatus("IN_PROGRESS");
            } else {
                trip.setStatus("COMPLETED");
            }
            tripRepository.save(trip);
        }

        // Update IN_PROGRESS trips that have arrived
        List<Trip> inProgressTrips = tripRepository.findByStatusAndDepartureTimeBefore("IN_PROGRESS", now);
        for (Trip trip : inProgressTrips) {
            if (trip.getArrivalTime().isBefore(now)) {
                trip.setStatus("COMPLETED");
                tripRepository.save(trip);
            }
        }
    }

    private TripResponse convertToDTO(Trip trip) {
        TripResponse dto = new TripResponse();

        // Basic trip info
        dto.setId(trip.getId());
        dto.setStatus(trip.getStatus());
        dto.setActive(trip.isActive());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setAmount(trip.getAmount());

        // Bus info
        if (trip.getBus() != null) {
            dto.setBusId(trip.getBus().getId());
            dto.setBusPlateNumber(trip.getBus().getPlateNumber());
            dto.setBusType(trip.getBus().getBusType());
            dto.setTotalSeats(trip.getBus().getTotalSeats());
        }

        // Available seats
        dto.setAvailableSeats(trip.getAvailableSeats());

        // Route info
        if (trip.getRoute() != null) {
            dto.setRouteId(trip.getRoute().getId());
            dto.setOrigin(trip.getRoute().getOrigin());
            dto.setDestination(trip.getRoute().getDestination());
            dto.setDistanceKm(trip.getRoute().getDistanceKm());
        }

        // Park info
        if (trip.getDeparturePark() != null) {
            dto.setDepartureParkId(trip.getDeparturePark().getId());
            dto.setDepartureParkName(trip.getDeparturePark().getName());
        }

        if (trip.getArrivalPark() != null) {
            dto.setArrivalParkId(trip.getArrivalPark().getId());
            dto.setArrivalParkName(trip.getArrivalPark().getName());
        }

        // Time info
        dto.setDepartureTime(trip.getDepartureTime());
        dto.setArrivalTime(trip.getArrivalTime());

        // Formatted time
        if (trip.getDepartureTime() != null) {
            dto.setFormattedDepartureTime(trip.getDepartureTime().format(DATE_TIME_FORMATTER));
        }

        if (trip.getArrivalTime() != null) {
            dto.setFormattedArrivalTime(trip.getArrivalTime().format(DATE_TIME_FORMATTER));
        }

        // Calculate duration
        if (trip.getDepartureTime() != null && trip.getArrivalTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(trip.getDepartureTime(), trip.getArrivalTime());
            dto.setDurationMinutes((int) minutes);

            // Format duration
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours > 0) {
                dto.setFormattedDuration(hours + "h " + (remainingMinutes > 0 ? remainingMinutes + "m" : ""));
            } else {
                dto.setFormattedDuration(remainingMinutes + "m");
            }
        }

        return dto;
    }
}
