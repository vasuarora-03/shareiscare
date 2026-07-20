package com.vasuarora.shareiscare.ride.dto;

import com.vasuarora.shareiscare.common.enums.CancellationType;
import com.vasuarora.shareiscare.ride.Ride;
import com.vasuarora.shareiscare.ride.RideStatus;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.vehicle.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Long id,
        String source,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime estimatedArrival,
        Integer availableSeats,
        BigDecimal pricePerSeat,
        RideStatus status,
        CancellationType cancellationType,
        DriverInfo driver,
        VehicleInfo vehicle,
        LocalDateTime createdAt
) {

    public record DriverInfo(Long id, String name, String phone) {
    }

    public record VehicleInfo(Long id, String make, String model, String registrationNumber) {
    }

    public static RideResponse from(Ride ride) {
        User driver = ride.getDriver();
        Vehicle vehicle = ride.getVehicle();

        return new RideResponse(
                ride.getId(),
                ride.getSource(),
                ride.getDestination(),
                ride.getDepartureTime(),
                ride.getEstimatedArrival(),
                ride.getAvailableSeats(),
                ride.getPricePerSeat(),
                ride.getStatus(),
                ride.getCancellationType(),
                new DriverInfo(driver.getId(), driver.getName(), driver.getPhone()),
                new VehicleInfo(vehicle.getId(), vehicle.getMake(), vehicle.getModel(), vehicle.getRegistrationNumber()),
                ride.getCreatedAt()
        );
    }
}
