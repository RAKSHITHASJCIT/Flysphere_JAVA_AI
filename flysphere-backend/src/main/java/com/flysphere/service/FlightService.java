package com.flysphere.service;

import com.flysphere.entity.Flight;
import com.flysphere.repository.FlightRepository;
import com.flysphere.repository.BookingSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final BookingSegmentRepository bookingSegmentRepository;

    public Flight createFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {

        List<Flight> flights = flightRepository.findAll();

        LocalDate today = LocalDate.now();

        for (Flight flight : flights) {
            if (flight.getDepartureDate() != null
                    && flight.getDepartureDate().isBefore(today)
                    && !"Cancelled".equalsIgnoreCase(flight.getFlightStatus())
                    && !"Completed".equalsIgnoreCase(flight.getFlightStatus())) {

                flight.setFlightStatus("Completed");
                flightRepository.save(flight);
            }
        }

        return flights;
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
    }

    public Flight updateFlight(Long id, Flight updatedFlight) {
        Flight flight = getFlightById(id);

        if (updatedFlight.getAirlineName() != null)
            flight.setAirlineName(updatedFlight.getAirlineName());

        if (updatedFlight.getFlightType() != null)
            flight.setFlightType(updatedFlight.getFlightType());

        if (updatedFlight.getDepartureAirport() != null)
            flight.setDepartureAirport(updatedFlight.getDepartureAirport());

        if (updatedFlight.getArrivalAirport() != null)
            flight.setArrivalAirport(updatedFlight.getArrivalAirport());

        if (updatedFlight.getDepartureDate() != null)
            flight.setDepartureDate(updatedFlight.getDepartureDate());

        if (updatedFlight.getArrivalDate() != null)
            flight.setArrivalDate(updatedFlight.getArrivalDate());

        if (updatedFlight.getDepartureTime() != null)
            flight.setDepartureTime(updatedFlight.getDepartureTime());

        if (updatedFlight.getArrivalTime() != null)
            flight.setArrivalTime(updatedFlight.getArrivalTime());

        if (updatedFlight.getFlightNo() != null)
            flight.setFlightNo(updatedFlight.getFlightNo());

        if (updatedFlight.getTotalEconomySeats() != null)
            flight.setTotalEconomySeats(updatedFlight.getTotalEconomySeats());

        if (updatedFlight.getTotalBusinessSeats() != null)
            flight.setTotalBusinessSeats(updatedFlight.getTotalBusinessSeats());

        if (updatedFlight.getTotalFirstClassSeats() != null)
            flight.setTotalFirstClassSeats(updatedFlight.getTotalFirstClassSeats());

        if (updatedFlight.getEconomyAdultFare() != null)
            flight.setEconomyAdultFare(updatedFlight.getEconomyAdultFare());

        if (updatedFlight.getEconomyChildFare() != null)
            flight.setEconomyChildFare(updatedFlight.getEconomyChildFare());

        if (updatedFlight.getBusinessAdultFare() != null)
            flight.setBusinessAdultFare(updatedFlight.getBusinessAdultFare());

        if (updatedFlight.getBusinessChildFare() != null)
            flight.setBusinessChildFare(updatedFlight.getBusinessChildFare());

        if (updatedFlight.getFirstAdultFare() != null)
            flight.setFirstAdultFare(updatedFlight.getFirstAdultFare());

        if (updatedFlight.getFirstChildFare() != null)
            flight.setFirstChildFare(updatedFlight.getFirstChildFare());

        if (updatedFlight.getAircraftType() != null)
            flight.setAircraftType(updatedFlight.getAircraftType());

        if (updatedFlight.getFlightStatus() != null)
            flight.setFlightStatus(updatedFlight.getFlightStatus());

        return flightRepository.save(flight);
    }

    public Flight cancelFlight(Long id) {
        Flight flight = getFlightById(id);

        if ("Cancelled".equalsIgnoreCase(flight.getFlightStatus())) {
            throw new RuntimeException("Flight is already cancelled");
        }

        flight.setFlightStatus("Cancelled");

        return flightRepository.save(flight);
    }

    public void deleteFlight(Long id) {

        if (!flightRepository.existsById(id)) {
            throw new RuntimeException("Flight not found");
        }

        if (bookingSegmentRepository.existsByFlight_Id(id)) {
            throw new RuntimeException("Cannot delete flight with existing bookings");
        }

        flightRepository.deleteById(id);
    }

    public List<Flight> searchFlights(String from, String to, LocalDate date) {

        List<Flight> flights = flightRepository
                .findByDepartureAirportIgnoreCaseAndArrivalAirportIgnoreCaseAndDepartureDate(
                        from.trim(),
                        to.trim(),
                        date
                );

        LocalDate today = LocalDate.now();

        for (Flight flight : flights) {
            if (flight.getDepartureDate() != null
                    && flight.getDepartureDate().isBefore(today)
                    && !"Cancelled".equalsIgnoreCase(flight.getFlightStatus())
                    && !"Completed".equalsIgnoreCase(flight.getFlightStatus())) {

                flight.setFlightStatus("Completed");
                flightRepository.save(flight);
            }
        }

        return flights;
    }

    @Transactional
    public void decrementSeats(Long flightId, String cabinClass, int seatsToBook) {

        Flight flight = flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        switch (cabinClass.toLowerCase()) {
            case "economy":
                if (flight.getTotalEconomySeats() < seatsToBook)
                    throw new RuntimeException("Not enough Economy seats");
                flight.setTotalEconomySeats(flight.getTotalEconomySeats() - seatsToBook);
                break;

            case "business":
                if (flight.getTotalBusinessSeats() < seatsToBook)
                    throw new RuntimeException("Not enough Business seats");
                flight.setTotalBusinessSeats(flight.getTotalBusinessSeats() - seatsToBook);
                break;

            case "first":
            case "first class":
                if (flight.getTotalFirstClassSeats() < seatsToBook)
                    throw new RuntimeException("Not enough First Class seats");
                flight.setTotalFirstClassSeats(flight.getTotalFirstClassSeats() - seatsToBook);
                break;

            default:
                throw new RuntimeException("Invalid cabin class");
        }

        // Explicitly save updated seat counts
        flightRepository.save(flight);
    }
}
