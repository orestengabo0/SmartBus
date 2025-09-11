package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingAnalytics {
    private long totalBookings;

    // Time-based aggregates
    private long totalBookingsPerDay;
    private long totalBookingsPerWeek;
    private long totalBookingsPerMonth;

    // Status-based breakdown
    private long totalBookingsConfirmed;
    private long totalBookingsPending;
    private long totalBookingsCancelled;

    // Optional: revenue metrics
    private double totalRevenue;
    private double avgRevenuePerBooking;
}
