package com.example.service;

import com.example.dto.TripEnttiesDTO;
import com.example.dto.requests.TripRequest;
import com.example.dto.responses.TripResponse;
import com.example.exception.ConflictException;
import com.example.exception.InvalidInputException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.mappers.TripMapper;
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
    private final TripMapper tripMapper;

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

        TripEnttiesDTO entities = loadTripEntities(tripDTO);


        validateOperatorAccess(currentUser, entities.getBus());
        validateTripTimes(tripDTO.getDepartureTime(), tripDTO.getArrivalTime());
        // Check for scheduling conflicts
        checkBusAvailability(bus, tripDTO.getDepartureTime(), tripDTO.getArrivalTime());

        // Create trip
        Trip trip = new Trip();
        trip.setBus(bus);
        trip.setRoute(entities.getRoute());
        trip.setDeparturePark(entities.getDeparturePark());
        trip.setArrivalPark(entities.getArrivalPark());
        trip.setDepartureTime(tripDTO.getDepartureTime());
        trip.setArrivalTime(tripDTO.getArrivalTime());
        trip.setAmount(entities.getRoute().getPrice());
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setAvailableSeats(bus.getTotalSeats()); // Initially all seats are available
        trip.setActive(true);
        trip.setCreatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        return tripMapper.toTripResponse(savedTrip);
    }

    private void checkBusAvailability(Bus bus, LocalDateTime start, LocalDateTime end) {
        // Find trips that might conflict
        List<Trip> busTrips = tripRepository.findByBus(bus);

        for (Trip existingTrip : busTrips) {
            // Only check active trips with status SCHEDULED or IN_PROGRESS
            if (!existingTrip.isActive() ||
                    !(existingTrip.getStatus().equals(TripStatus.SCHEDULED) ||
                            existingTrip.getStatus().equals(TripStatus.IN_PROGRESS))) {
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

        return tripMapper.toTripResponseList(trips);
    }

    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        return tripMapper.toTripResponse(trip);
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
                            (trip.getStatus().equals(TripStatus.SCHEDULED) ||
                                    trip.getStatus().equals(TripStatus.IN_PROGRESS)))
                    .collect(Collectors.toList());
        } else {
            // Admins see all upcoming trips
            trips = tripRepository.findByActiveAndDepartureTimeAfter(true, LocalDateTime.now());
        }

        return tripMapper.toTripResponseList(trips);
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
        TripEnttiesDTO entities = loadTripEntities(tripDTO);
        validateOperatorAccess(currentUser, entities.getBus());
        validateTripTimes(tripDTO.getDepartureTime(), tripDTO.getArrivalTime());

        // If changing bus or time, check for conflicts
        if (!trip.getBus().getId().equals(entities.getBus().getId()) ||
                !trip.getDepartureTime().equals(tripDTO.getDepartureTime()) ||
                !trip.getArrivalTime().equals(tripDTO.getArrivalTime())) {
            checkBusAvailability(entities.getBus(), tripDTO.getDepartureTime(), tripDTO.getArrivalTime());
        }

        // Update trip
        trip.setBus(entities.getBus());
        trip.setRoute(entities.getRoute());
        trip.setDeparturePark(entities.getDeparturePark());
        trip.setArrivalPark(entities.getArrivalPark());
        trip.setDepartureTime(tripDTO.getDepartureTime());
        trip.setArrivalTime(tripDTO.getArrivalTime());
        trip.setAmount(entities.getRoute().getPrice());

        Trip updatedTrip = tripRepository.save(trip);
        return tripMapper.toTripResponse(updatedTrip);
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

        trip.setStatus(TripStatus.CANCELLED);
        Trip cancelledTrip = tripRepository.save(trip);

        // TODO: Notify booked passengers about cancellation

        return tripMapper.toTripResponse(cancelledTrip);
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateTripStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Update SCHEDULED trips that have departed
        List<Trip> departedTrips = tripRepository.findByStatusAndDepartureTimeBefore(TripStatus.SCHEDULED, now);
        for (Trip trip : departedTrips) {
            if (trip.getArrivalTime().isAfter(now)) {
                trip.setStatus(TripStatus.IN_PROGRESS);
            } else {
                trip.setStatus(TripStatus.COMPLETED);
            }
            tripRepository.save(trip);
        }

        // Update IN_PROGRESS trips that have arrived
        List<Trip> inProgressTrips = tripRepository.findByStatusAndDepartureTimeBefore(TripStatus.IN_PROGRESS, now);
        for (Trip trip : inProgressTrips) {
            if (trip.getArrivalTime().isBefore(now)) {
                trip.setStatus(TripStatus.COMPLETED);
                tripRepository.save(trip);
            }
        }
    }

    // Helper method to load trips by route, bus, and buspark
    private TripEnttiesDTO loadTripEntities(TripRequest tripDTO) {
        Bus bus = busRepository.findById(tripDTO.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        Route route = routeRepository.findById(tripDTO.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        BusPark departurePark = busParkRepository.findById(tripDTO.getDepartureParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure park not found"));

        BusPark arrivalPark = busParkRepository.findById(tripDTO.getArrivalParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival park not found"));

        return new TripEnttiesDTO(bus, route, departurePark, arrivalPark);
    }

    private void validateOperatorAccess(User user, Bus bus) {
        if (user.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only manage trips for your own buses");
        }
    }

    private void validateTripTimes(LocalDateTime departure, LocalDateTime arrival) {
        if (departure.isAfter(arrival)) {
            throw new InvalidInputException("Departure time must be before arrival time");
        }
        if (departure.isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Departure time must be in the future");
        }
    }
}