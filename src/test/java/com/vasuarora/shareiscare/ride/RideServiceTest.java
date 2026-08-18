package com.vasuarora.shareiscare.ride;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.ride.dto.RideRequest;
import com.vasuarora.shareiscare.ride.dto.RideResponse;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import com.vasuarora.shareiscare.vehicle.Vehicle;
import com.vasuarora.shareiscare.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private RideService rideService;

    private User driver;
    private Vehicle vehicle;
    private RideRequest request;

    @BeforeEach
    void setUp() {
        driver = User.builder().id(1L).name("Driver").phone("9000000001").licenseUploaded(true).build();
        vehicle = Vehicle.builder().id(5L).owner(driver).make("Maruti").model("Swift")
                .registrationNumber("DL01AB1234").seatCapacity(4).build();
        request = new RideRequest("Delhi", "Noida",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                5L);
    }

    private Ride existingRide() {
        return Ride.builder()
                .id(10L)
                .driver(driver)
                .vehicle(vehicle)
                .source("Delhi")
                .destination("Noida")
                .departureTime(LocalDateTime.now().minusHours(1))
                .estimatedArrival(LocalDateTime.now().plusHours(1))
                .availableSeats(4)
                .status(RideStatus.SCHEDULED)
                .build();
    }

    @Test
    void createRide_success_seedsAvailableSeatsFromVehicle() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(rideRepository.save(org.mockito.ArgumentMatchers.any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse response = rideService.createRide(1L, request);

        assertThat(response.status()).isEqualTo(RideStatus.SCHEDULED);
        assertThat(response.availableSeats()).isEqualTo(4);
    }

    @Test
    void createRide_userNotFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createRide_noLicense_throws403() {
        driver.setLicenseUploaded(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("upload your driving license");
    }

    @Test
    void createRide_vehicleNotFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void createRide_notOwnVehicle_throws403() {
        User someoneElse = User.builder().id(99L).name("Other").phone("9000000099").licenseUploaded(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        vehicle.setOwner(someoneElse);
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> rideService.createRide(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("your own vehicle");
    }

    @Test
    void createRide_arrivalBeforeDeparture_throws400() {
        RideRequest badRequest = new RideRequest("Delhi", "Noida",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusHours(1),
                5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> rideService.createRide(1L, badRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Estimated arrival must be after");
    }

    @Test
    void updateRide_success_reseedsAvailableSeats() {
        Ride ride = existingRide();
        vehicle.setSeatCapacity(3);
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));

        RideResponse response = rideService.updateRide(1L, 10L, request);

        assertThat(response.availableSeats()).isEqualTo(3);
    }

    @Test
    void updateRide_notFound_throws404() {
        when(rideRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.updateRide(1L, 10L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ride not found");
    }

    @Test
    void updateRide_notDriver_throws403() {
        Ride ride = existingRide();
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.updateRide(99L, 10L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void cancelRide_success_marksCancelledWithClassification() {
        Ride ride = existingRide();
        ride.setDepartureTime(LocalDateTime.now().plusHours(2));
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        RideResponse response = rideService.cancelRide(1L, 10L);

        assertThat(response.status()).isEqualTo(RideStatus.CANCELLED);
        assertThat(response.cancellationType()).isNotNull();
    }

    @Test
    void cancelRide_notScheduled_throws400() {
        Ride ride = existingRide();
        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.cancelRide(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void completeRide_success_afterDeparture() {
        Ride ride = existingRide();
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        RideResponse response = rideService.completeRide(1L, 10L);

        assertThat(response.status()).isEqualTo(RideStatus.COMPLETED);
    }

    @Test
    void completeRide_beforeDeparture_throws400() {
        Ride ride = existingRide();
        ride.setDepartureTime(LocalDateTime.now().plusHours(3));
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.completeRide(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("before its departure time");
    }

    @Test
    void completeRide_notScheduled_throws400() {
        Ride ride = existingRide();
        ride.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.completeRide(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be marked as completed");
    }

    @Test
    void completeRide_notDriver_throws403() {
        Ride ride = existingRide();
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.completeRide(99L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");
    }
}
