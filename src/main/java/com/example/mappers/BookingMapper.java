package com.example.mappers;

import com.example.dto.responses.BookingResponse;
import com.example.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface BookingMapper {
    // User info
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userPhone", source = "user.phone")

    // Trip info
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "origin", source = "trip.route.origin")
    @Mapping(target = "destination", source = "trip.route.destination")
    @Mapping(target = "busPlateNumber", source = "trip.bus.plateNumber")
    @Mapping(target = "departureTime", source = "trip.departureTime")
    @Mapping(target = "arrivalTime", source = "trip.arrivalTime")
    @Mapping(target = "formattedDepartureTime", source = "trip.departureTime", qualifiedByName = "formatDate")
    @Mapping(target = "formattedArrivalTime", source = "trip.arrivalTime", qualifiedByName = "formatDate")

    // Booking info
    @Mapping(target = "seatCount", expression = "java(booking.getSeatNumbers() != null ? booking.getSeatNumbers().size() : 0)")
    @Mapping(target = "formattedBookingTime", source = "bookingTime", qualifiedByName = "formatDate")
    @Mapping(target = "formattedExpiryTime", source = "expiryTime", qualifiedByName = "formatDate")

    // Payment info
    @Mapping(
            target = "paid",
            expression = "java(booking.getPayment() != null && booking.getPayment().getStatus() == com.example.model.PaymentStatus.COMPLETED)"
    )
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    @Mapping(target = "paymentTime", source = "payment.paymentTime")

    // Ticket info
    @Mapping(target = "hasTicket", expression = "java(booking.getTicket() != null)")
    @Mapping(target = "ticketNumber", source = "ticket.ticketNumber")
    BookingResponse toBookingResponse(Booking booking);

    List<BookingResponse> toBookingResponseList(List<Booking> bookings);
}