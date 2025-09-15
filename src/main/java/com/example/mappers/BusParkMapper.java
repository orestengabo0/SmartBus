package com.example.mappers;

import com.example.dto.responses.BusParkResponse;
import com.example.model.BusPark;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusParkMapper {

    @Mapping(target = "busCount", expression = "java(busPark.getBuses() != null ? busPark.getBuses().size() : 0)")
    @Mapping(target = "departureTripCount",
            expression = "java(busPark.getDepartureTrips() != null ? busPark.getDepartureTrips().size() : 0)")
    @Mapping(target = "arrivalTripCount",
            expression = "java(busPark.getArrivalTrips() != null ? busPark.getArrivalTrips().size() : 0)")
    BusParkResponse toBusParkResponse(BusPark busPark);

    List<BusParkResponse> toBusParkResponseList(List<BusPark> busParks);
}
