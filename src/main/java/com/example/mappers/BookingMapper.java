package com.example.mappers;

import com.example.dto.responses.BookingResponse;
import com.example.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface BookingMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userPhone", source = "user.phone")

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "origin", source = "trip.route.origin")
    @Mapping(target = "destination", source = "trip.route.destination")
    @Mapping(target = "busPlateNumber", source = "trip.bus.plateNumber")
    @Mapping(target = "departureTime", source = "trip.departureTime")
    @Mapping(target = "arrivalTime", source = "trip.arrivalTime")
    @Mapping(target = "formattedDepartureTime", expression = "java(dateMapper.formatDate(booking.getTrip() != null ? booking.getTrip().getDepartureTime() : null))")
    @Mapping(target = "formattedArrivalTime", expression = "java(dateMapper.formatDate(booking.getTrip() != null ? booking.getTrip().getArrivalTime() : null))")

    // Booking info
    @Mapping(target = "seatNumbers", source = "seatNumbers")
    @Mapping(target = "seatCount", expression = "java(booking.getSeatNumbers() != null ? booking.getSeatNumbers().size() : 0)")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "bookingTime", source = "bookingTime")
    @Mapping(target = "formattedBookingTime", expression = "java(dateMapper.formatDate(booking.getBookingTime()))")
    @Mapping(target = "expiryTime", source = "expiryTime")
    @Mapping(target = "formattedExpiryTime", expression = "java(dateMapper.formatDate(booking.getExpiryTime()))")

    // Payment info
    @Mapping(target = "isPaid", expression = "java(booking.getPayment() != null && booking.getPayment().getStatus() == PaymentStatus.COMPLETED)")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    @Mapping(target = "paymentTime", source = "payment.paymentTime")

    // Ticket info
    @Mapping(target = "hasTicket", expression = "java(booking.getTicket() != null)")
    @Mapping(target = "ticketNumber", source = "ticket.ticketNumber")
    BookingResponse toBookingResponse(Booking booking);

    List<BookingResponse> toBookingResponseList(List<Booking> bookings);
}
