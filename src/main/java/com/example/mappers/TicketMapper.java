package com.example.mappers;

import com.example.dto.responses.TicketResponse;
import com.example.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface TicketMapper {

    // Ticket info
    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "formattedIssueTime", source = "issueTime", qualifiedByName = "formatDate")
    @Mapping(target = "formattedValidationTime", source = "validationTime", qualifiedByName = "formatDate")

    // Booking info
    @Mapping(target = "bookingId", source = "booking.id")

    // Passenger info
    @Mapping(target = "passengerName", source = "booking.user.fullName")
    @Mapping(target = "passengerEmail", source = "booking.user.email")
    @Mapping(target = "passengerPhone", source = "booking.user.phone")

    // Trip info
    @Mapping(target = "tripId", source = "booking.trip.id")
    @Mapping(target = "origin", source = "booking.trip.route.origin")
    @Mapping(target = "destination", source = "booking.trip.route.destination")
    @Mapping(target = "busPlateNumber", source = "booking.trip.bus.plateNumber")
    @Mapping(target = "departureTime", source = "booking.trip.departureTime")
    @Mapping(target = "arrivalTime", source = "booking.trip.arrivalTime")
    @Mapping(target = "formattedDepartureTime", source = "booking.trip.departureTime", qualifiedByName = "formatDate")
    @Mapping(target = "formattedArrivalTime", source = "booking.trip.arrivalTime", qualifiedByName = "formatDate")

    // Seat info
    @Mapping(target = "seatNumbers", source = "booking.seatNumbers")
    TicketResponse toTicketResponse(Ticket ticket);
}