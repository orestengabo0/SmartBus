package com.example.dto.responses;

import com.example.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long bookingId;
    private String transactionId;
    private double amount;
    private PaymentStatus status;
    private String ticketNumber;
}