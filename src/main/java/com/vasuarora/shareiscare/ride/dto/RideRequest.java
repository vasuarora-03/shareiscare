package com.vasuarora.shareiscare.ride.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideRequest(

        @NotBlank(message = "Source is required.")
        String source,

        @NotBlank(message = "Destination is required.")
        String destination,

        @NotNull(message = "Departure time is required.")
        @Future(message = "Departure time must be in the future.")
        LocalDateTime departureTime,

        @NotNull(message = "Estimated arrival is required.")
        LocalDateTime estimatedArrival,

        @NotNull(message = "Vehicle is required.")
        Long vehicleId,

        @NotNull(message = "Price per seat is required.")
        @Positive(message = "Price per seat must be greater than zero.")
        BigDecimal pricePerSeat
) {
}
