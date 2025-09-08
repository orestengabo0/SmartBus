package com.example.service;

import com.example.dto.requests.PaymentRequest;
import com.example.dto.responses.PaymentResponse;
import com.example.exception.BadRequestException;
import com.example.exception.PermissionException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Booking;
import com.example.model.Payment;
import com.example.model.Ticket;
import com.example.model.User;
import com.example.repository.BookingRepository;
import com.example.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final WebSocketService webSocketService;
    private final TicketService ticketService;

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        User currentUser = currentUserService.getCurrentUser();

        // Find booking
        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Check ownership
        if (!booking.getUser().getId().equals(currentUser.getId()) &&
                !userService.isAdmin(currentUser)) {
            throw new PermissionException("You don't have permission to pay for this booking");
        }

        // Validate booking status
        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("This booking is not in PENDING state");
        }

        // Check if booking is expired
        if (booking.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This booking has expired");
        }

        // Simulate payment processing
        boolean paymentSuccessful = simulatePaymentProcessing(paymentRequest);

        if (!paymentSuccessful) {
            throw new BadRequestException("Payment failed. Please try again.");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setTransactionId(generateTransactionId());
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Generate ticket
        Ticket ticket = ticketService.generateTicket(booking);

        // Notify about payment confirmation
        webSocketService.sendBookingUpdate(
                booking.getId(),
                "CONFIRMED",
                "Payment successful. Your ticket is ready."
        );

        // Create response
        PaymentResponse response = new PaymentResponse();
        response.setBookingId(booking.getId());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setTicketNumber(ticket.getTicketNumber());

        return response;
    }

    /**
     * Simulate payment processing (dummy implementation)
     */
    private boolean simulatePaymentProcessing(PaymentRequest request) {
        // Simulate 97% success rate
        return Math.random() > 0.03;
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
