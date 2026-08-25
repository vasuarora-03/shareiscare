package com.vasuarora.shareiscare.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPassengerId(Long passengerId);

    boolean existsByRideIdAndPassengerIdAndStatus(Long rideId, Long passengerId, BookingStatus status);

    long countByRideIdAndStatus(Long rideId, BookingStatus status);

    List<Booking> findByRideIdAndStatus(Long rideId, BookingStatus status);
}
