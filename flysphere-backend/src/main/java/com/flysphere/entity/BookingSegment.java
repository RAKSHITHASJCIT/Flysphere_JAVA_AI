package com.flysphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_segments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer segmentNo;

    @Column(name = "cabin_type")
    private String cabinType;

    // ✅ Add-ons stored per segment (Outbound / Return)
    @Column(name = "seat_preference")
    private String seatPreference;

    @Column(name = "meal_preference")
    private String mealPreference;

    @Column(name = "extra_baggage")
    private Boolean extraBaggage;

    @Column(name = "travel_protection")
    private Boolean travelProtection;

    @ManyToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;
}
