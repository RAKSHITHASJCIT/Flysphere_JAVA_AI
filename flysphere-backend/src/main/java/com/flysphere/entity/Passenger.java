package com.flysphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "passengers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private Integer age;

    @Column(name = "dob")
    private String dob;

    private String type;

    // ✅ Outbound Preferences
    @Column(name = "outbound_seat")
    private String outboundSeat;

    @Column(name = "outbound_meal")
    private String outboundMeal;

    // ✅ Return Preferences
    @Column(name = "return_seat")
    private String returnSeat;

    @Column(name = "return_meal")
    private String returnMeal;

    private Boolean baggage;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Booking booking;
}
