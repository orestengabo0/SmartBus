package com.example.controller;

import com.example.dto.responses.TicketResponse;
import com.example.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Ticket Management", description = "APIs for managing tickets")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get ticket details")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ticketService.getTicketByBookingId(bookingId));
    }

    @GetMapping("/{bookingId}/qr")
    @Operation(summary = "Get ticket QR code")
    public ResponseEntity<byte[]> getTicketQRCode(@PathVariable Long bookingId) {
        byte[] qrCode = ticketService.getTicketQRCode(bookingId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
    }

    @GetMapping("/{bookingId}/pdf")
    @Operation(summary = "Download ticket as PDF")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long bookingId) {
        byte[] pdfBytes = ticketService.generateTicketPDF(bookingId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "ticket.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/validate/{ticketNumber}")
    @Operation(summary = "Validate a ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<TicketResponse> validateTicket(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ticketService.validateTicket(ticketNumber));
    }
}
