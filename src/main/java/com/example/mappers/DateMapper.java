package com.example.mappers;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface DateMapper {
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Helper methods for formatting
    default String formatDate(LocalDateTime time) {
        return time != null ? time.format(DATE_TIME_FORMATTER) : null;
    }

    default Integer calculateDurationMinutes(LocalDateTime departure, LocalDateTime arrival) {
        if (departure != null && arrival != null) {
            return (int) ChronoUnit.MINUTES.between(departure, arrival);
        }
        return null;
    }

    default String formatDuration(LocalDateTime departure, LocalDateTime arrival) {
        if (departure != null && arrival != null) {
            long minutes = ChronoUnit.MINUTES.between(departure, arrival);
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours > 0) {
                return hours + "h " + (remainingMinutes > 0 ? remainingMinutes + "m" : "");
            } else {
                return remainingMinutes + "m";
            }
        }
        return null;
    }
}
