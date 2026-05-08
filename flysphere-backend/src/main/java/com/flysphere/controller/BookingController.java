package com.flysphere.controller;

import com.flysphere.entity.Booking;
import com.flysphere.entity.Passenger;
import com.flysphere.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {

        Booking booking = bookingService.createBooking(
                request.getUserId(),
                request.getOutboundFlightId(),
                request.getReturnFlightId(),
                request.getPassengers(),
                request.getTotalAmount(),
                request.getOutboundCabinClass(),
                request.getReturnCabinClass(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getOutboundAddOns(),
                request.getReturnAddOns()
        );

        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @PutMapping("/{bookingCode}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingCode) {
        bookingService.cancelBooking(bookingCode);
        return ResponseEntity.ok("Booking cancelled successfully");
    }

    @GetMapping("/{bookingCode}")
    public ResponseEntity<Booking> getBooking(@PathVariable String bookingCode) {
        return ResponseEntity.ok(bookingService.getBookingByCode(bookingCode));
    }

    @Data
    public static class BookingRequest {
        private Long userId;
        private Long outboundFlightId;
        private Long returnFlightId;
        private List<Passenger> passengers;
        private Double totalAmount;
        private String outboundCabinClass;
        private String returnCabinClass;

        // ✅ Contact Information
        private String contactEmail;
        private String contactPhone;

        // ✅ Segment-level add-ons
        private SegmentAddOns outboundAddOns;
        private SegmentAddOns returnAddOns;
    }

    @Data
    public static class SegmentAddOns {
        private String seatPreference;
        private String mealPreference;
        private Boolean extraBaggage;
        private Boolean travelProtection;
    }
}
