package com.vasuarora.shareiscare.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(

        @NotBlank(message = "Make is required.")
        String make,

        @NotBlank(message = "Model is required.")
        String model,

        @NotBlank(message = "Registration number is required.")
        String registrationNumber,

        @NotNull(message = "Seat capacity is required.")
        @Min(value = 1, message = "Seat capacity must be at least 1.")
        Integer seatCapacity,

        String color
) {
}
