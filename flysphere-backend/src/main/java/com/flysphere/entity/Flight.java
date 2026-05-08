package com.flysphere.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "flightmgtable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flightid")
    private Long id;

    @Column(name = "airlinename")
    private String airlineName;

    @Column(name = "flighttype")
    private String flightType;

    @Column(name = "departureairport")
    private String departureAirport;

    @Column(name = "arrivalairport")
    private String arrivalAirport;

    @Column(name = "departuredate")
    private LocalDate departureDate;

    @Column(name = "arrivaldate")
    private LocalDate arrivalDate;

    @Column(name = "departuretime")
    private LocalTime departureTime;

    @Column(name = "arrivaltime")
    private LocalTime arrivalTime;

    @Column(name = "flightno")
    private String flightNo;

    @Column(name = "flightstatus")
    private String flightStatus;

    @Column(name = "totaleconomyseats")
    private Integer totalEconomySeats;

    @Column(name = "totalbusinessseats")
    private Integer totalBusinessSeats;

    @Column(name = "totalfirstclassseats")
    private Integer totalFirstClassSeats;

    @Column(name = "economyadultfare")
    private Integer economyAdultFare;

    @Column(name = "economychildfare")
    private Integer economyChildFare;

    @Column(name = "businessadultfare")
    private Integer businessAdultFare;

    @Column(name = "businesschildfare")
    private Integer businessChildFare;

    @Column(name = "firstadultfare")
    private Integer firstAdultFare;

    @Column(name = "firstchildfare")
    private Integer firstChildFare;

    @Column(name = "aircraft_type")
    private String aircraftType;
}
