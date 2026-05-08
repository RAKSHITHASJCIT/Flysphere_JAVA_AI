package com.flysphere.service;

import com.flysphere.entity.*;
import com.flysphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSegmentRepository bookingSegmentRepository;
    private final PassengerRepository passengerRepository;
    private final FlightService flightService;

    @Transactional
    public Booking createBooking(Long userId,
                                 Long outboundFlightId,
                                 Long returnFlightId,
                                 List<Passenger> passengers,
                                 Double totalAmount,
                                 String outboundCabinClass,
                                 String returnCabinClass,
                                 String contactEmail,
                                 String contactPhone,
                                 com.flysphere.controller.BookingController.SegmentAddOns outboundAddOns,
                                 com.flysphere.controller.BookingController.SegmentAddOns returnAddOns) {

        String bookingCode = "FS" + UUID.randomUUID().toString().substring(0, 6);

        Booking booking = Booking.builder()
                .bookingId(bookingCode)
                .userId(userId)
                .totalAmount(totalAmount)
                .contactEmail(contactEmail)
                .contactPhone(contactPhone)
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        booking = bookingRepository.save(booking);

        // Load outbound flight
        Flight outboundFlight = flightService.getFlightById(outboundFlightId);

        BookingSegment outboundSegment = BookingSegment.builder()
                .booking(booking)
                .segmentNo(1)
                .flight(outboundFlight)
                .cabinType(outboundCabinClass)
                .build();

        // Attach segment to booking
        booking.setSegments(new java.util.ArrayList<>());
        booking.getSegments().add(outboundSegment);

        // Save return segment if exists
        if (returnFlightId != null) {

            Flight returnFlight = flightService.getFlightById(returnFlightId);

            String returnClassToUse = returnCabinClass != null
                    ? returnCabinClass
                    : outboundCabinClass;

            BookingSegment returnSegment = BookingSegment.builder()
                    .booking(booking)
                    .segmentNo(2)
                    .flight(returnFlight)
                    .cabinType(returnClassToUse)
                    .build();

            booking.getSegments().add(returnSegment);
        }

        // Save booking with all segments (cascade)
        booking = bookingRepository.save(booking);

        int seatsToBook = passengers.size();

        // Decrement seats separately for each segment
        flightService.decrementSeats(outboundFlightId, outboundCabinClass, seatsToBook);

        if (returnFlightId != null) {
            String returnClassToUse = returnCabinClass != null ? returnCabinClass : outboundCabinClass;
            flightService.decrementSeats(returnFlightId, returnClassToUse, seatsToBook);
        }

        // Save passengers
        for (Passenger passenger : passengers) {
            passenger.setBooking(booking);
            passengerRepository.save(passenger);
        }

        return booking;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public void cancelBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingId(bookingCode)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public Booking getBookingByCode(String bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}
