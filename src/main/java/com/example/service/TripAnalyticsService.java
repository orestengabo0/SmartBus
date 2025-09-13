package com.example.service;

import com.example.dto.TripAnalyticsDTO;
import com.example.model.Trip;
import com.example.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripAnalyticsService {
    private final TripRepository tripRepository;

//    public List<TripAnalyticsDTO> getTripAnalytics() {
//        List<Trip> trips = tripRepository.findAll();
//
//    }
}
