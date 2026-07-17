package com.vasuarora.shareiscare.ride;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByDriverId(Long driverId);

    List<Ride> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenAndStatus(
            String source, String destination, LocalDateTime start, LocalDateTime end, RideStatus status);
}
