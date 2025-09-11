package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteAnalyticsDTO {
    private Long routeId;
    private String routeName;

    // Bookings
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;

    // Revenue
    private double totalRevenue;
    private double averageRevenuePerTrip;
    private double averageRevenuePerBooking;

    // Efficiency
    private double averageOccupancy; // 0.0 - 1.0
    private long totalTripsOnRoute;
}
