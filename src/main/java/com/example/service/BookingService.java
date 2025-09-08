package com.example.service;

import com.example.dto.SeatAvailabilityDTO;
import com.example.dto.requests.BookingRequest;
import com.example.dto.responses.BookingResponse;
import com.example.exception.*;
import com.example.model.*;
import com.example.repository.BookingRepository;
import com.example.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final TripService tripService;
    private final WebSocketService webSocketService;
    private final TripRepository tripRepository;
    private final CurrentUserService currentUserService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Get seat availability for a trip
     */
    public SeatAvailabilityDTO getSeatAvailability(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Check if trip is bookable
        if (!trip.isActive() || !"SCHEDULED".equals(trip.getStatus())) {
            throw new BadRequestException("Trip is not available for booking");
        }

        //Build Seat Map
        Map<Integer, Boolean> seatStatus = new HashMap<>();

        // Initialize all seats as available
        for (int i = 1; i <= trip.getBus().getTotalSeats(); i++) {
            seatStatus.put(i, true);
        }

        // Mark booked seats as unavailable
        List<Booking> bookings = bookingRepository.findByTrip(trip);
        for (Booking booking : bookings) {
            if (!"CANCELLED".equals(booking.getStatus())) {
                for (Integer seatNumber : booking.getSeatNumbers()) {
                    seatStatus.put(seatNumber, false);
                }
            }
        }

        return new SeatAvailabilityDTO(
              tripId,
              trip.getBus().getTotalSeats(),
              trip.getAvailableSeats(),
              seatStatus
        );
    }

    public BookingResponse createBooking(BookingRequest bookingRequest) {
        try{
            // Get current user and trip
            User currentUser = currentUserService.getCurrentUser();
            Trip trip = tripRepository.findById(bookingRequest.getTripId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

            // Validate trip status
            if (!trip.isActive() || !"SCHEDULED".equals(trip.getStatus())) {
                throw new BadRequestException("Trip is not available for booking");
            }

            // Validate seat numbers
            List<Integer> seatNumbers = bookingRequest.getSeatNumbers();
            if (seatNumbers.isEmpty()) {
                throw new BadRequestException("No seats selected");
            }

            // Check each seat
            for (Integer seatNumber : seatNumbers) {
                // Validate seat number range
                if (seatNumber < 1 || seatNumber > trip.getBus().getTotalSeats()) {
                    throw new BadRequestException("Invalid seat number: " + seatNumber);
                }

                // Check if seat is already booked
                List<Booking> existingBookings = bookingRepository.findBySeatNumber(trip, seatNumber);
                if (!existingBookings.isEmpty()) {
                    throw new ConflictException("Seat " + seatNumber + " is already booked");
                }
            }

            double totalAmount = trip.getAmount() * seatNumbers.size();

            // Create booking
            Booking booking = new Booking();
            booking.setUser(currentUser);
            booking.setTrip(trip);
            booking.setSeatNumbers(seatNumbers);
            booking.setTotalAmount(totalAmount);
            booking.setStatus("PENDING");
            booking.setBookingTime(LocalDateTime.now());
            booking.setExpiryTime(LocalDateTime.now().plusMinutes(15));

            // Save booking
            Booking savedBooking = bookingRepository.save(booking);

            // Update trip's available seats - triggers optimistic locking
            trip.setAvailableSeats(trip.getAvailableSeats() - seatNumbers.size());
            tripRepository.save(trip);

            // Notify clients about seat updates
            Map<Integer, Boolean> seatUpdates = new HashMap<>();
            for (Integer seatNumber : seatNumbers) {
                seatUpdates.put(seatNumber, false);
            }
            webSocketService.sendSeatUpdate(trip.getId(), seatUpdates);

            return mapToBookingResponse(savedBooking);
        }catch (OptimisticLockingException ex){
            throw new ConflictException("Seat availability has been changed. Please try again");
        }
    }

    public List<BookingResponse> getUserBookings(){
        var currentUser = currentUserService.getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUser(currentUser);

        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse cancelBooking(Long bookingId) {
        try{
            var currentUser = currentUserService.getCurrentUser();
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found")
                    );
            //check ownership
            if (!booking.getUser().getId().equals(currentUser.getId()) &&
                    !userService.isAdmin(currentUser)) {
                throw new PermissionException("You don't have permission to cancel this booking");
            }

            // Check if booking can be cancelled
            if ("CANCELLED".equals(booking.getStatus())) {
                throw new BadRequestException("Booking is already cancelled");
            }

            if (booking.getTrip().getDepartureTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Cannot cancel a booking for a trip that has already departed");
            }

            // Update booking status
            booking.setStatus("CANCELLED");
            Booking cancelledBooking = bookingRepository.save(booking);

            //Update trip's available seats
            Trip trip = booking.getTrip();
            trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeatNumbers().size());
            tripRepository.save(trip);

            // Notify clients about seat updates
            Map<Integer, Boolean> seatUpdates = new HashMap<>();
            for (Integer seatNumber : booking.getSeatNumbers()) {
                seatUpdates.put(seatNumber, true);
            }
            webSocketService.sendSeatUpdate(trip.getId(), seatUpdates);

            // Notify about booking cancellation
            webSocketService.sendBookingUpdate(booking.getId(), "CANCELLED", "Booking has been cancelled");

            return mapToBookingResponse(cancelledBooking);
        } catch (OptimisticLockingException e) {
            throw new ConflictException("Failed to cancel booking");
        }
    }

    /**
     * Clear expired bookings (scheduled task)
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void clearExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiryTimeBefore("PENDING", now);

        for (Booking booking : expiredBookings) {
            try {
                // Update booking status
                booking.setStatus("EXPIRED");
                bookingRepository.save(booking);

                // Release seats
                Trip trip = booking.getTrip();
                trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeatNumbers().size());
                tripRepository.save(trip);

                // Notify clients
                Map<Integer, Boolean> seatUpdates = new HashMap<>();
                for (Integer seatNumber : booking.getSeatNumbers()) {
                    seatUpdates.put(seatNumber, true);
                }
                webSocketService.sendSeatUpdate(trip.getId(), seatUpdates);

                // Notify about booking expiration
                webSocketService.sendBookingUpdate(booking.getId(), "EXPIRED", "Booking has expired");
            } catch (Exception e) {
                // Log the error but continue with other bookings
                System.err.println("Error expiring booking " + booking.getId() + ": " + e.getMessage());
            }
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse dto = new BookingResponse();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setUserFullName(booking.getUser().getFullName());
        dto.setUserEmail(booking.getUser().getEmail());
        dto.setUserPhone(booking.getUser().getPhone());

        if(booking.getBookingTime() != null){
            dto.setFormattedBookingTime(booking.getBookingTime().format(FORMATTER));
        }

        if (booking.getExpiryTime() != null) {
            dto.setFormattedExpiryTime(booking.getExpiryTime().format(FORMATTER));
        }

        // User info
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserFullName(booking.getUser().getFullName());
            dto.setUserEmail(booking.getUser().getEmail());
            dto.setUserPhone(booking.getUser().getPhone());
        }

        // Trip info
        if (booking.getTrip() != null) {
            Trip trip = booking.getTrip();
            dto.setTripId(trip.getId());

            if (trip.getRoute() != null) {
                dto.setOrigin(trip.getRoute().getOrigin());
                dto.setDestination(trip.getRoute().getDestination());
            }

            if (trip.getBus() != null) {
                dto.setBusPlateNumber(trip.getBus().getPlateNumber());
            }

            dto.setDepartureTime(trip.getDepartureTime());
            dto.setArrivalTime(trip.getArrivalTime());

            if (trip.getDepartureTime() != null) {
                dto.setFormattedDepartureTime(trip.getDepartureTime().format(FORMATTER));
            }

            if (trip.getArrivalTime() != null) {
                dto.setFormattedArrivalTime(trip.getArrivalTime().format(FORMATTER));
            }
        }

        // Payment info
        if (booking.getPayment() != null) {
            Payment payment = booking.getPayment();
            dto.setPaid("SUCCESS".equals(payment.getStatus()));
            dto.setPaymentMethod(payment.getPaymentMethod());
            dto.setPaymentTime(payment.getPaymentTime());
        } else {
            dto.setPaid(false);
        }

        // Ticket info
        if (booking.getTicket() != null) {
            Ticket ticket = booking.getTicket();
            dto.setHasTicket(true);
            dto.setTicketNumber(ticket.getTicketNumber());
        } else {
            dto.setHasTicket(false);
        }
        return dto;
    }
}
