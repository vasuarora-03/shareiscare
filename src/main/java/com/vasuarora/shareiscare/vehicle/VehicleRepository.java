package com.vasuarora.shareiscare.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    boolean existsByRegistrationNumber(String registrationNumber);
}
