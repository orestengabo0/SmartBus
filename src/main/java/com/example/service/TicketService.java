package com.example.service;

import com.example.dto.responses.TicketResponse;
import com.example.exception.BadRequestException;
import com.example.exception.PermissionException;
import com.example.exception.QRCodeException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.*;
import com.example.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    @Transactional
    public Ticket generateTicket(Booking booking) {
        // Validate booking status
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Cannot generate ticket for unconfirmed booking");
        }

        // Generate ticket number
        String ticketNumber = generateTicketNumber();

        // Generate QR Code
        byte[] qrCode = generateQRCode(ticketNumber, booking);

        // Create ticket
        Ticket ticket = new Ticket();
        ticket.setBooking(booking);
        ticket.setTicketNumber(ticketNumber);
        ticket.setQrcode(qrCode);
        ticket.setIssueTime(LocalDateTime.now());
        ticket.setValidated(false);

        return ticketRepository.save(ticket);
    }

    public TicketResponse getTicketByBookingId(Long bookingId) {
        User currentUser = currentUserService.getCurrentUser();

        // Check tickets booked
        Ticket ticket = ticketRepository.findAll()
                .stream()
                .filter(t -> t.getBooking().getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No ticket found"));


        // Check ownership
        if (!ticket.getBooking().getUser().getId().equals(currentUser.getId()) &&
                userService.isAdmin(currentUser)) {
            throw new PermissionException("You don't have access to this ticket");
        }

        return mapToTicketResponse(ticket);
    }

    private String generateTicketNumber(){
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%05d", new Random().nextInt(100000));
        return "SB-" + dateStr + "-" + randomPart;
    }

    /**
     * Get ticket QR code
     */
    public byte[] getTicketQRCode(Long bookingId) {
        Ticket ticket = findMyTicket(bookingId);
        return ticket.getQrcode();
    }

    /**
     * Generate PDF ticket
     */
    public byte[] generateTicketPDF(Long bookingId) {
        Ticket ticket = findMyTicket(bookingId);
        // Generate PDF
        return createPDF(ticket);
    }

    @Transactional
    public TicketResponse validateTicket(String ticketNumber) {
        User currentUser = currentUserService.getCurrentUser();

        // Only admins and operators can validate
        if (!userService.isAdminOrOperator(currentUser)) {
            throw new PermissionException("Only staff can validate tickets");
        }

        // Find ticket
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        // Check if already validated
        if (ticket.isValidated()) {
            throw new BadRequestException("Ticket has already been validated");
        }

        // Check if trip is for today
        LocalDate tripDate = ticket.getBooking().getTrip().getDepartureTime().toLocalDate();
        LocalDate today = LocalDate.now();

        if (!tripDate.isEqual(today)) {
            throw new BadRequestException("Ticket is not valid for today's date");
        }

        // Mark as validated
        ticket.setValidated(true);
        ticket.setValidationTime(LocalDateTime.now());

        Ticket validatedTicket = ticketRepository.save(ticket);
        return mapToTicketResponse(validatedTicket);
    }

    private Ticket findMyTicket(Long bookingId){
        User currentUser = currentUserService.getCurrentUser();

        // Find ticket
        Ticket ticket = ticketRepository.findAll().stream()
                .filter(t -> t.getBooking().getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        // Check ownership
        if (!ticket.getBooking().getUser().getId().equals(currentUser.getId()) &&
                userService.isAdmin(currentUser)) {
            throw new PermissionException("You don't have access to this ticket");
        }

        return ticket;
    }

    private byte[] createPDF(Ticket ticket) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Create a smaller page size (similar to a receipt)
            // Custom size: 3.5 inches wide x 5.5 inches tall (mobile-friendly)
            float width = 252; // 3.5 inches * 72 points/inch
            float height = 396; // 5.5 inches * 72 points/inch
            PDRectangle customPageSize = new PDRectangle(width, height);
            PDPage page = new PDPage(customPageSize);
            document.addPage(page);

            // Create content stream
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Set up fonts
                PDFont titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDFont normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float margin = 20;
                float yPosition = height - 30;

                // Company header
                contentStream.setFont(titleFont, 16);
                drawCenteredText(contentStream, "SMARTBUS", width / 2, yPosition);
                yPosition -= 18;

                contentStream.setFont(normalFont, 8);
                drawCenteredText(contentStream, "Digital Ticket", width / 2, yPosition);
                yPosition -= 20;

                // Draw separator line
                drawLine(contentStream, margin, yPosition, width - margin, yPosition);
                yPosition -= 15;

                // Ticket number (prominent)
                contentStream.setFont(titleFont, 10);
                drawCenteredText(contentStream, ticket.getTicketNumber(), width / 2, yPosition);
                yPosition -= 20;

                // QR Code at the top for easy scanning
                if (ticket.getQrcode() != null) {
                    PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, ticket.getQrcode(), "QR");
                    float qrSize = 100;
                    float qrX = (width - qrSize) / 2;
                    contentStream.drawImage(qrImage, qrX, yPosition - qrSize, qrSize, qrSize);
                    yPosition -= (qrSize + 15);
                }

                // Route information (most important)
                drawLine(contentStream, margin, yPosition, width - margin, yPosition);
                yPosition -= 15;

                contentStream.setFont(titleFont, 9);
                addTextLine(contentStream, "FROM:", margin, yPosition);
                contentStream.setFont(normalFont, 9);
                addTextLine(contentStream, ticket.getBooking().getTrip().getRoute().getOrigin(), margin + 35, yPosition);
                yPosition -= 15;

                contentStream.setFont(titleFont, 9);
                addTextLine(contentStream, "TO:", margin, yPosition);
                contentStream.setFont(normalFont, 9);
                addTextLine(contentStream, ticket.getBooking().getTrip().getRoute().getDestination(), margin + 35, yPosition);
                yPosition -= 20;

                // Date and time
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                Trip trip = ticket.getBooking().getTrip();

                contentStream.setFont(titleFont, 9);
                addTextLine(contentStream, "DATE:", margin, yPosition);
                contentStream.setFont(normalFont, 9);
                addTextLine(contentStream, trip.getDepartureTime().format(dateFormatter), margin + 35, yPosition);
                yPosition -= 15;

                contentStream.setFont(titleFont, 9);
                addTextLine(contentStream, "TIME:", margin, yPosition);
                contentStream.setFont(normalFont, 9);
                addTextLine(contentStream, trip.getDepartureTime().format(timeFormatter) + " - " +
                        trip.getArrivalTime().format(timeFormatter), margin + 35, yPosition);
                yPosition -= 20;

                // Passenger and seat info
                drawLine(contentStream, margin, yPosition, width - margin, yPosition);
                yPosition -= 15;

                contentStream.setFont(normalFont, 8);
                addTextLine(contentStream, "Passenger: " + ticket.getBooking().getUser().getFullName(), margin, yPosition);
                yPosition -= 12;

                String seats = ticket.getBooking().getSeatNumbers().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                addTextLine(contentStream, "Seat(s): " + seats, margin, yPosition);
                yPosition -= 12;

                addTextLine(contentStream, "Bus: " + trip.getBus().getPlateNumber(), margin, yPosition);
                yPosition -= 20;

                // Price
                drawLine(contentStream, margin, yPosition, width - margin, yPosition);
                yPosition -= 15;

                contentStream.setFont(titleFont, 10);
                addTextLine(contentStream, "TOTAL: $" + String.format("%.2f", ticket.getBooking().getTotalAmount()), margin, yPosition);
                yPosition -= 20;

                // Footer
                drawLine(contentStream, margin, yPosition, width - margin, yPosition);
                yPosition -= 12;

                contentStream.setFont(normalFont, 6);
                drawCenteredText(contentStream, "Present this ticket at boarding", width / 2, yPosition);
                yPosition -= 10;
                drawCenteredText(contentStream, "Generated: " + LocalDateTime.now().format(dateFormatter), width / 2, yPosition);
            }

            // Save to byte array
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF ticket", e);
        }
    }

    // Helper method to draw centered text
    private void drawCenteredText(PDPageContentStream contentStream, String text, float centerX, float y) throws IOException {
        contentStream.beginText();
        // This is approximate - for perfect centering you'd need to calculate text width
        float textWidth = text.length() * 4; // Rough approximation
        contentStream.newLineAtOffset(centerX - (textWidth / 2), y);
        contentStream.showText(text);
        contentStream.endText();
    }

    // Helper method to draw a line
    private void drawLine(PDPageContentStream contentStream, float startX, float startY, float endX, float endY) throws IOException {
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(endX, endY);
        contentStream.stroke();
    }

    // Helper method to add text lines
    private void addTextLine(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private byte[] generateQRCode(String ticketNumber, Booking booking){
        try{
            String qrData = String.format(
                    "TICKET:%s\nPASSENGER:%s\nFROM:%s\nTO:%s\nDEPARTURE:%s\nSEATS:%s",
                    ticketNumber,
                    booking.getUser().getFullName(),
                    booking.getTrip().getRoute().getOrigin(),
                    booking.getTrip().getRoute().getDestination(),
                    booking.getTrip().getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    booking.getSeatNumbers().stream().map(String::valueOf).collect(Collectors.joining(","))
            );

            BitMatrix bitMatrix = new MultiFormatWriter().
                    encode(qrData, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

            return baos.toByteArray();
        }catch(Exception ex){
            throw new QRCodeException("Cannot generate QR code");
        }
    }

    /**
     * Map Ticket entity to TicketResponseDTO
     */
    private TicketResponse mapToTicketResponse(Ticket ticket) {
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        TicketResponse dto = new TicketResponse();

        // Basic ticket info
        dto.setTicketId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setIssueTime(ticket.getIssueTime());
        dto.setValidated(ticket.isValidated());
        dto.setValidationTime(ticket.getValidationTime());

        // Formatted dates
        if (ticket.getIssueTime() != null) {
            dto.setFormattedIssueTime(ticket.getIssueTime().format(FORMATTER));
        }

        if (ticket.getValidationTime() != null) {
            dto.setFormattedValidationTime(ticket.getValidationTime().format(FORMATTER));
        }

        // Get booking info
        Booking booking = ticket.getBooking();
        if (booking != null) {
            dto.setBookingId(booking.getId());

            // Passenger info from user
            User user = booking.getUser();
            if (user != null) {
                dto.setPassengerName(user.getFullName());
                dto.setPassengerEmail(user.getEmail());
                dto.setPassengerPhone(user.getPhone());
            }

            // Seat info
            dto.setSeatNumbers(booking.getSeatNumbers());

            // Trip info
            Trip trip = booking.getTrip();
            if (trip != null) {
                dto.setTripId(trip.getId());

                // Route info
                if (trip.getRoute() != null) {
                    dto.setOrigin(trip.getRoute().getOrigin());
                    dto.setDestination(trip.getRoute().getDestination());
                }

                // Bus info
                if (trip.getBus() != null) {
                    dto.setBusPlateNumber(trip.getBus().getPlateNumber());
                }

                // Trip times
                dto.setDepartureTime(trip.getDepartureTime());
                dto.setArrivalTime(trip.getArrivalTime());

                if (trip.getDepartureTime() != null) {
                    dto.setFormattedDepartureTime(trip.getDepartureTime().format(FORMATTER));
                }

                if (trip.getArrivalTime() != null) {
                    dto.setFormattedArrivalTime(trip.getArrivalTime().format(FORMATTER));
                }
            }
        }

        return dto;
    }
}
