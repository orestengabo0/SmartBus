package com.example.mappers;

import com.example.dto.responses.BusResponse;
import com.example.model.Bus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @Mapping(target = "busParkId", source = "currentBusPark.id")
    @Mapping(target = "busParkName", source = "currentBusPark.name")
    @Mapping(target = "busParkLocation", source = "currentBusPark.location")

    @Mapping(target = "operatorId", source = "operator.id")
    @Mapping(target = "operatorName", source = "operator.fullName")
    @Mapping(target = "operatorEmail", source = "operator.email")

    // Computed seat counts
    @Mapping(target = "bookedSeats", expression = "java(bus.getSeats() != null ? (int) bus.getSeats().stream().filter(seat -> seat.isBooked()).count() : 0)")
    @Mapping(target = "availableSeats", expression = "java(bus.getSeats() != null ? bus.getTotalSeats() - (int) bus.getSeats().stream().filter(seat -> seat.isBooked()).count() : bus.getTotalSeats())")
    BusResponse toBusResponse(Bus bus);

    List<BusResponse> toBusResponseList(List<Bus> buses);
}
