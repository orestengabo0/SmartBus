package com.example.mappers;

import com.example.dto.responses.TripResponse;
import com.example.model.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface TripMapper {

    // Bus info
    @Mapping(target = "busId", source = "trip.bus.id")
    @Mapping(target = "busPlateNumber", source = "trip.bus.plateNumber")
    @Mapping(target = "busType", source = "trip.bus.busType")
    @Mapping(target = "totalSeats", source = "trip.bus.totalSeats")

    // Route info
    @Mapping(target = "routeId", source = "trip.route.id")
    @Mapping(target = "origin", source = "trip.route.origin")
    @Mapping(target = "destination", source = "trip.route.destination")
    @Mapping(target = "distanceKm", source = "trip.route.distanceKm")

    // Parks
    @Mapping(target = "departureParkId", source = "trip.departurePark.id")
    @Mapping(target = "departureParkName", source = "trip.departurePark.name")
    @Mapping(target = "arrivalParkId", source = "trip.arrivalPark.id")
    @Mapping(target = "arrivalParkName", source = "trip.arrivalPark.name")

    // DateMapper transformations
    // one field → transformer method
    @Mapping(target = "formattedDepartureTime", source = "departureTime", qualifiedByName = "formatDate")
    @Mapping(target = "formattedArrivalTime", source = "arrivalTime", qualifiedByName = "formatDate")

    // multiple fields → need expression
    @Mapping(
            target = "durationMinutes",
            expression = "java(dateMapper.calculateDurationMinutes(trip.getDepartureTime(), trip.getArrivalTime()))"
    )
    @Mapping(
            target = "formattedDuration",
            expression = "java(dateMapper.formatDuration(trip.getDepartureTime(), trip.getArrivalTime()))"
    )
    TripResponse toTripResponse(Trip trip);

    List<TripResponse> toTripResponseList(List<Trip> trips);
}