package com.example.mappers;

import com.example.dto.responses.RouteResponse;
import com.example.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface RouteMapper {

    @Mapping(
            target = "tripCount",
            expression = "java(route.getTrips() != null ? route.getTrips().size() : 0)"
    )
    @Mapping(
            target = "formattedDuration",
            source = "estimatedDurationMinutes",
            qualifiedByName = "formatDurationMinutes"
    )
    RouteResponse toRouteResponse(Route route);

    List<RouteResponse> toRouteResponseList(List<Route> routes);
}