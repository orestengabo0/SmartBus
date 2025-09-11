package com.example.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeakHourDTO {
    private int hour;
    private long count;
}
