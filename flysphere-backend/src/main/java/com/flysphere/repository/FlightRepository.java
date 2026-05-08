package com.flysphere.repository;

import com.flysphere.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.id = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);
    List<Flight> findByDepartureAirportIgnoreCaseAndArrivalAirportIgnoreCaseAndDepartureDate(
            String departureAirport,
            String arrivalAirport,
            java.time.LocalDate departureDate
    );
}
