package com.flysphere.repository;

import com.flysphere.entity.BookingSegment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSegmentRepository extends JpaRepository<BookingSegment, Long> {

    boolean existsByFlight_Id(Long flightId);
}
