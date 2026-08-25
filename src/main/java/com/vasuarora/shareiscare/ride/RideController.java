package com.vasuarora.shareiscare.ride;

import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.ride.dto.RideRequest;
import com.vasuarora.shareiscare.ride.dto.RideResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public ResponseEntity<ApiResponse<RideResponse>> createRide(@Valid @RequestBody RideRequest request) {
        RideResponse response = rideService.createRide(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Ride created successfully.", response));
    }

    // Spring MVC matches routes top-to-bottom; if {rideId} came first, "me" would be parsed as a rideId.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getMyRides() {
        List<RideResponse> response = rideService.getMyRides(CurrentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Rides fetched successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RideResponse>>> searchRides(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<RideResponse> response = rideService.searchRides(source, destination, date);
        return ResponseEntity.ok(ApiResponse.success("Rides fetched successfully.", response));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponse>> getRideDetails(@PathVariable Long rideId) {
        RideResponse response = rideService.getRideDetails(rideId);
        return ResponseEntity.ok(ApiResponse.success("Ride details fetched successfully.", response));
    }

    @PutMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponse>> updateRide(@PathVariable Long rideId,
                                                                 @Valid @RequestBody RideRequest request) {
        RideResponse response = rideService.updateRide(CurrentUser.id(), rideId, request);
        return ResponseEntity.ok(ApiResponse.success("Ride updated successfully.", response));
    }

    @DeleteMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponse>> cancelRide(@PathVariable Long rideId) {
        RideResponse response = rideService.cancelRide(CurrentUser.id(), rideId);
        return ResponseEntity.ok(ApiResponse.success("Ride cancelled successfully.", response));
    }

    @PatchMapping("/{rideId}/complete")
    public ResponseEntity<ApiResponse<RideResponse>> completeRide(@PathVariable Long rideId) {
        RideResponse response = rideService.completeRide(CurrentUser.id(), rideId);
        return ResponseEntity.ok(ApiResponse.success("Ride marked as completed.", response));
    }
}
