package com.vasuarora.shareiscare.vehicle;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.ride.RideRepository;
import com.vasuarora.shareiscare.ride.RideStatus;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import com.vasuarora.shareiscare.vehicle.dto.VehicleRequest;
import com.vasuarora.shareiscare.vehicle.dto.VehicleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private User owner;
    private VehicleRequest request;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").phone("9000000001").build();
        request = new VehicleRequest("Maruti", "Swift", "DL01AB1234", 4, "White");
    }

    @Test
    void addVehicle_success() {
        when(vehicleRepository.existsByRegistrationNumber("DL01AB1234")).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(owner);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse response = vehicleService.addVehicle(1L, request);

        assertThat(response.registrationNumber()).isEqualTo("DL01AB1234");
    }

    @Test
    void addVehicle_duplicateRegistration_throws409() {
        when(vehicleRepository.existsByRegistrationNumber("DL01AB1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.addVehicle(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateVehicle_success_sameRegistrationSkipsDuplicateCheck() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).make("Old").model("Old")
                .registrationNumber("DL01AB1234").seatCapacity(2).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));

        VehicleResponse response = vehicleService.updateVehicle(1L, 5L, request);

        assertThat(response.make()).isEqualTo("Maruti");
        verify(vehicleRepository, org.mockito.Mockito.never()).existsByRegistrationNumber(any());
    }

    @Test
    void updateVehicle_changedRegistration_checksAndAllowsIfFree() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).make("Old").model("Old")
                .registrationNumber("DL01OLD999").seatCapacity(2).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.existsByRegistrationNumber("DL01AB1234")).thenReturn(false);

        VehicleResponse response = vehicleService.updateVehicle(1L, 5L, request);

        assertThat(response.registrationNumber()).isEqualTo("DL01AB1234");
    }

    @Test
    void updateVehicle_changedRegistration_conflictsWithAnotherVehicle_throws409() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).make("Old").model("Old")
                .registrationNumber("DL01OLD999").seatCapacity(2).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.existsByRegistrationNumber("DL01AB1234")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle(1L, 5L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateVehicle_notFound_throws404() {
        when(vehicleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(1L, 5L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void updateVehicle_notOwner_throws403() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).make("Old").model("Old")
                .registrationNumber("DL01AB1234").seatCapacity(2).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, 5L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void deleteVehicle_success() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));

        vehicleService.deleteVehicle(1L, 5L);

        verify(vehicleRepository).delete(existing);
    }

    @Test
    void deleteVehicle_blockedByActiveScheduledRide_throws409() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(rideRepository.existsByVehicleIdAndStatus(5L, RideStatus.SCHEDULED)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(1L, 5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("active scheduled ride");

        verify(vehicleRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void deleteVehicle_notOwner_throws403() {
        Vehicle existing = Vehicle.builder().id(5L).owner(owner).build();
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L, 5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");

        verify(vehicleRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void deleteVehicle_notFound_throws404() {
        when(vehicleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(1L, 5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Vehicle not found");
    }
}
