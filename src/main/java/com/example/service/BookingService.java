package com.example.service;

import com.example.dto.SeatAvailabilityDTO;
import com.example.dto.requests.BookingRequest;
import com.example.dto.responses.BookingResponse;
import com.example.exception.*;
import com.example.mappers.BookingMapper;
import com.example.model.*;
import com.example.repository.BookingRepository;
import com.example.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final WebSocketService webSocketService;
    private final TripRepository tripRepository;
    private final CurrentUserService currentUserService;
    private final BookingMapper bookingMapper;

    /**
     * Get seat availability for a trip
     */
    public SeatAvailabilityDTO getSeatAvailability(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Check if trip is bookable
        if (!trip.isActive() || trip.getStatus() != TripStatus.SCHEDULED) {
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
            if (booking.getStatus() != BookingStatus.CANCELLED) {
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
            if (!trip.isActive() || trip.getStatus() != TripStatus.SCHEDULED) {
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
            booking.setStatus(BookingStatus.PENDING);
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

            return bookingMapper.toBookingResponse(savedBooking);
        }catch (OptimisticLockingException ex){
            throw new ConflictException("Seat availability has been changed. Please try again");
        }
    }

    public List<BookingResponse> getUserBookings(){
        var currentUser = currentUserService.getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUser(currentUser);

        return bookingMapper.toBookingResponseList(bookings);
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
                    userService.isAdmin(currentUser)) {
                throw new PermissionException("You don't have permission to cancel this booking");
            }

            // Check if booking can be cancelled
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                throw new BadRequestException("Booking is already cancelled");
            }

            if (booking.getTrip().getDepartureTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Cannot cancel a booking for a trip that has already departed");
            }

            return processBookings(
                    booking,
                    BookingStatus.CANCELLED,
                    "Booking has been cancelled"
            );
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
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiryTimeBefore(BookingStatus.PENDING, now);

        for (Booking booking : expiredBookings) {
            try {
                processBookings(
                        booking,
                        BookingStatus.EXPIRED,
                        "Booking has expired"
                );
            } catch (Exception e) {
                // Log the error but continue with other bookings
                System.err.println("Error expiring booking " + booking.getId() + ": " + e.getMessage());
            }
        }
    }

    private BookingResponse processBookings(Booking booking,
                                            BookingStatus newBookingStatus,
                                            String notificationMessage) {
        // Update booking status
        booking.setStatus(newBookingStatus);
        Booking updatedBooking = bookingRepository.save(booking);

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
        webSocketService.sendBookingUpdate(booking.getId(), newBookingStatus, notificationMessage);
        return bookingMapper.toBookingResponse(updatedBooking);
    }
}