package com.vasuarora.shareiscare.ride;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.common.util.CancellationClassifier;
import com.vasuarora.shareiscare.ride.dto.RideRequest;
import com.vasuarora.shareiscare.ride.dto.RideResponse;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import com.vasuarora.shareiscare.vehicle.Vehicle;
import com.vasuarora.shareiscare.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public RideResponse createRide(Long driverId, RideRequest request) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        if (!driver.isLicenseUploaded()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Please upload your driving license before creating a ride.");
        }

        Vehicle vehicle = findVehicleOrThrow(request.vehicleId());
        assertVehicleOwnership(vehicle, driverId);
        assertArrivalAfterDeparture(request.departureTime(), request.estimatedArrival());

        Ride ride = Ride.builder()
                .driver(driver)
                .vehicle(vehicle)
                .source(request.source())
                .destination(request.destination())
                .departureTime(request.departureTime())
                .estimatedArrival(request.estimatedArrival())
                .availableSeats(vehicle.getSeatCapacity())
                .pricePerSeat(request.pricePerSeat())
                .status(RideStatus.SCHEDULED)
                .build();

        return RideResponse.from(rideRepository.save(ride));
    }

    public List<RideResponse> getMyRides(Long driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(RideResponse::from)
                .toList();
    }

    public List<RideResponse> searchRides(String source, String destination, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return rideRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenAndStatus(
                        source, destination, startOfDay, endOfDay, RideStatus.SCHEDULED)
                .stream()
                .map(RideResponse::from)
                .toList();
    }

    public RideResponse getRideDetails(Long rideId) {
        return RideResponse.from(findRideOrThrow(rideId));
    }

    @Transactional
    public RideResponse updateRide(Long driverId, Long rideId, RideRequest request) {
        Ride ride = findRideOrThrow(rideId);
        assertDriver(ride, driverId);

        Vehicle vehicle = findVehicleOrThrow(request.vehicleId());
        assertVehicleOwnership(vehicle, driverId);
        assertArrivalAfterDeparture(request.departureTime(), request.estimatedArrival());

        ride.setSource(request.source());
        ride.setDestination(request.destination());
        ride.setDepartureTime(request.departureTime());
        ride.setEstimatedArrival(request.estimatedArrival());
        ride.setVehicle(vehicle);
        ride.setAvailableSeats(vehicle.getSeatCapacity());
        ride.setPricePerSeat(request.pricePerSeat());

        return RideResponse.from(ride);
    }

    @Transactional
    public RideResponse cancelRide(Long driverId, Long rideId) {
        Ride ride = findRideOrThrow(rideId);
        assertDriver(ride, driverId);

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This ride cannot be cancelled.");
        }

        ride.setCancellationType(CancellationClassifier.classify(ride.getDepartureTime()));
        ride.setStatus(RideStatus.CANCELLED);

        return RideResponse.from(ride);
    }

    @Transactional
    public RideResponse completeRide(Long driverId, Long rideId) {
        Ride ride = findRideOrThrow(rideId);
        assertDriver(ride, driverId);

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This ride cannot be marked as completed.");
        }

        if (LocalDateTime.now().isBefore(ride.getDepartureTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ride cannot be marked as completed before its departure time.");
        }

        ride.setStatus(RideStatus.COMPLETED);

        return RideResponse.from(ride);
    }

    private Ride findRideOrThrow(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ride not found."));
    }

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found."));
    }

    private void assertDriver(Ride ride, Long driverId) {
        if (!ride.getDriver().getId().equals(driverId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to modify this ride.");
        }
    }

    private void assertVehicleOwnership(Vehicle vehicle, Long driverId) {
        if (!vehicle.getOwner().getId().equals(driverId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only create rides using your own vehicle.");
        }
    }

    private void assertArrivalAfterDeparture(LocalDateTime departureTime, LocalDateTime estimatedArrival) {
        if (!estimatedArrival.isAfter(departureTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Estimated arrival must be after departure time.");
        }
    }
}
