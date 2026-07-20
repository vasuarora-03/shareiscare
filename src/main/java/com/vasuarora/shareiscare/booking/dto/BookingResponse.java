package com.vasuarora.shareiscare.booking.dto;

import com.vasuarora.shareiscare.booking.Booking;
import com.vasuarora.shareiscare.booking.BookingStatus;
import com.vasuarora.shareiscare.common.enums.CancellationType;
import com.vasuarora.shareiscare.ride.Ride;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        BookingStatus status,
        CancellationType cancellationType,
        BigDecimal pricePaid,
        LocalDateTime createdAt,
        RideInfo ride
) {

    public record RideInfo(
            Long id,
            String source,
            String destination,
            LocalDateTime departureTime,
            LocalDateTime estimatedArrival
    ) {
    }

    public static BookingResponse from(Booking booking) {
        Ride ride = booking.getRide();

        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getCancellationType(),
                booking.getPricePaid(),
                booking.getCreatedAt(),
                new RideInfo(ride.getId(), ride.getSource(), ride.getDestination(),
                        ride.getDepartureTime(), ride.getEstimatedArrival())
        );
    }
}
