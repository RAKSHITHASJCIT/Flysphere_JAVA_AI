package com.flysphere.service;

import com.flysphere.entity.Booking;
import com.flysphere.entity.BookingSegment;
import com.flysphere.entity.Passenger;
import com.flysphere.repository.BookingRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final BookingRepository bookingRepository;

    // Purple shade matching My Bookings page
    private static final Color BRAND_BLUE = new Color(124, 58, 237);

    public byte[] generateTicketPdf(String bookingId) {

        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // ===========================
            // HEADER BAR
            // ===========================
            PdfPTable headerBar = new PdfPTable(1);
            headerBar.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell(
                    new Phrase("✈  FlySphere",
                            new Font(Font.HELVETICA, 26, Font.BOLD, Color.WHITE))
            );
            headerCell.setBackgroundColor(BRAND_BLUE);
            headerCell.setPadding(22);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBorder(Rectangle.NO_BORDER);

            headerBar.addCell(headerCell);
            document.add(headerBar);
            document.add(new Paragraph(" "));

            // ===========================
            // BOOKING INFO WITH LEFT ACCENT
            // ===========================
            PdfPTable wrapper = new PdfPTable(1);
            wrapper.setWidthPercentage(100);

            PdfPCell wrapperCell = new PdfPCell();
            wrapperCell.setBorder(Rectangle.NO_BORDER);
            wrapperCell.setBorderWidthLeft(6f);
            wrapperCell.setBorderColorLeft(BRAND_BLUE);
            // Add padding so left accent border does not overlap text
            wrapperCell.setPaddingLeft(12);
            wrapperCell.setPaddingTop(4);
            wrapperCell.setPaddingBottom(4);

            PdfPTable bookingTable = new PdfPTable(2);
            bookingTable.setWidthPercentage(100);

            addRow(bookingTable, "Booking ID", booking.getBookingId());
            addRow(bookingTable, "Status", booking.getStatus());
            addRow(bookingTable, "Total Paid", String.valueOf(booking.getTotalAmount()));

            wrapperCell.addElement(bookingTable);
            wrapper.addCell(wrapperCell);
            document.add(wrapper);

            document.add(new Paragraph(" "));

            // ===========================
            // BOOKING REF BADGE
            // ===========================
            PdfPTable refTable = new PdfPTable(1);
            refTable.setWidthPercentage(50);
            refTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell refCell = new PdfPCell(
                    new Phrase("BOOKING REF: " + booking.getBookingId(),
                            new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE))
            );
            refCell.setBackgroundColor(BRAND_BLUE);
            refCell.setPadding(10);
            refCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            refCell.setBorder(Rectangle.NO_BORDER);

            refTable.addCell(refCell);
            document.add(refTable);

            document.add(new Paragraph(" "));

            // ===========================
            // FLIGHT SECTION
            // ===========================
            List<BookingSegment> segments = booking.getSegments();

            if (segments != null && !segments.isEmpty()) {

                for (int i = 0; i < segments.size(); i++) {

                    BookingSegment segment = segments.get(i);

                    Paragraph flightTitle = new Paragraph(
                            i == 0 ? "DEPARTURE FLIGHT" : "RETURN FLIGHT",
                            new Font(Font.HELVETICA, 18, Font.BOLD, BRAND_BLUE)
                    );
                    flightTitle.setSpacingBefore(15);
                    flightTitle.setSpacingAfter(8);
                    document.add(flightTitle);

                    PdfPTable flightTable = new PdfPTable(2);
                    flightTable.setWidthPercentage(100);

                    addRow(flightTable, "Airline", segment.getFlight().getAirlineName());
                    addRow(flightTable, "Flight No", segment.getFlight().getFlightNo());
                    addRow(flightTable, "Route",
                            segment.getFlight().getDepartureAirport() + " → " +
                                    segment.getFlight().getArrivalAirport());
                    addRow(flightTable, "Departure",
                            segment.getFlight().getDepartureDate() + " " +
                                    segment.getFlight().getDepartureTime());
                    addRow(flightTable, "Arrival",
                            segment.getFlight().getArrivalDate() + " " +
                                    segment.getFlight().getArrivalTime());

                    document.add(flightTable);
                }
            }

            document.add(new Paragraph(" "));

            // ===========================
            // PASSENGER DETAILS TABLE
            // ===========================
            Paragraph passTitle = new Paragraph(
                    "PASSENGER DETAILS",
                    new Font(Font.HELVETICA, 18, Font.BOLD, BRAND_BLUE)
            );
            passTitle.setSpacingBefore(15);
            passTitle.setSpacingAfter(8);
            document.add(passTitle);

            PdfPTable passengerTable = new PdfPTable(6);
            passengerTable.setWidthPercentage(100);

            addHeader(passengerTable, "Name");
            addHeader(passengerTable, "Type");
            addHeader(passengerTable, "Outbound Seat");
            addHeader(passengerTable, "Outbound Meal");
            addHeader(passengerTable, "Return Seat");
            addHeader(passengerTable, "Return Meal");

            List<Passenger> passengers = booking.getPassengers();
            for (Passenger p : passengers) {
                passengerTable.addCell(p.getFirstName() + " " + p.getLastName());
                passengerTable.addCell(capitalize(p.getType()));
                passengerTable.addCell(p.getOutboundSeat() != null ? capitalize(p.getOutboundSeat()) : "N/A");
                passengerTable.addCell(p.getOutboundMeal() != null ? capitalize(p.getOutboundMeal()) : "N/A");
                passengerTable.addCell(p.getReturnSeat() != null ? capitalize(p.getReturnSeat()) : "N/A");
                passengerTable.addCell(p.getReturnMeal() != null ? capitalize(p.getReturnMeal()) : "N/A");
            }

            document.add(passengerTable);

            document.add(new Paragraph(" "));

            // ===========================
            // CONTACT INFORMATION
            // ===========================
            Paragraph contactTitle = new Paragraph(
                    "Contact Information",
                    new Font(Font.HELVETICA, 16, Font.BOLD)
            );
            contactTitle.setSpacingBefore(10);
            contactTitle.setSpacingAfter(6);
            document.add(contactTitle);

            PdfPTable contactTable = new PdfPTable(2);
            contactTable.setWidthPercentage(100);

            addRow(contactTable, "Email", booking.getContactEmail());
            addRow(contactTable, "Phone", booking.getContactPhone());

            document.add(contactTable);

            document.add(new Paragraph(" "));

            // ===========================
            // DOTTED DIVIDER
            // ===========================
            Paragraph dots = new Paragraph(
                    "......................................................",
                    new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY)
            );
            dots.setAlignment(Element.ALIGN_CENTER);
            document.add(dots);

            document.add(new Paragraph(" "));

            // ===========================
            // FOOTER
            // ===========================
            Paragraph footer = new Paragraph(
                    "Thank you for choosing FlySphere  |  Have a pleasant journey",
                    new Font(Font.HELVETICA, 12, Font.NORMAL, BRAND_BLUE)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }

    // ===========================
    // HELPER METHODS
    // ===========================

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(label);
        table.addCell(value != null ? value : "N/A");
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text,
                        new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE))
        );
        cell.setBackgroundColor(BRAND_BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
