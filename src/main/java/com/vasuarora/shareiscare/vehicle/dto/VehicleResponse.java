package com.vasuarora.shareiscare.vehicle.dto;

import com.vasuarora.shareiscare.vehicle.Vehicle;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String make,
        String model,
        String registrationNumber,
        Integer seatCapacity,
        String color,
        LocalDateTime createdAt
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getRegistrationNumber(),
                vehicle.getSeatCapacity(),
                vehicle.getColor(),
                vehicle.getCreatedAt()
        );
    }
}
