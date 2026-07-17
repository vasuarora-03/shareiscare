package com.vasuarora.shareiscare.vehicle;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.UserRepository;
import com.vasuarora.shareiscare.vehicle.dto.VehicleRequest;
import com.vasuarora.shareiscare.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public VehicleResponse addVehicle(Long userId, VehicleRequest request) {
        assertRegistrationNumberFree(request.registrationNumber());

        Vehicle vehicle = Vehicle.builder()
                .owner(userRepository.getReferenceById(userId))
                .make(request.make())
                .model(request.model())
                .registrationNumber(request.registrationNumber())
                .seatCapacity(request.seatCapacity())
                .color(request.color())
                .build();

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    public List<VehicleResponse> getMyVehicles(Long userId) {
        return vehicleRepository.findByOwnerId(userId).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional
    public VehicleResponse updateVehicle(Long userId, Long vehicleId, VehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        assertOwner(vehicle, userId);

        if (!vehicle.getRegistrationNumber().equals(request.registrationNumber())) {
            assertRegistrationNumberFree(request.registrationNumber());
        }

        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setRegistrationNumber(request.registrationNumber());
        vehicle.setSeatCapacity(request.seatCapacity());
        vehicle.setColor(request.color());

        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        Vehicle vehicle = findVehicleOrThrow(vehicleId);
        assertOwner(vehicle, userId);
        vehicleRepository.delete(vehicle);
    }

    private void assertRegistrationNumberFree(String registrationNumber) {
        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "A vehicle with this registration number already exists.");
        }
    }

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found."));
    }

    private void assertOwner(Vehicle vehicle, Long userId) {
        if (!vehicle.getOwner().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to modify this vehicle.");
        }
    }
}
