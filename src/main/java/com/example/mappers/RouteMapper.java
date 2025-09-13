package com.example.mappers;

import com.example.dto.responses.RouteResponse;
import com.example.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    @Mapping(target = "id", source = "id")
    @Mapping()
    RouteResponse toRouteResponse(Route route);
}
