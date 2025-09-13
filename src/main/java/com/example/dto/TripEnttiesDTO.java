package com.example.dto;

import com.example.model.Bus;
import com.example.model.BusPark;
import com.example.model.Route;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TripEnttiesDTO {
    private final Bus bus;
    private final Route route;
    private final BusPark departurePark;
    private final BusPark arrivalPark;
}
