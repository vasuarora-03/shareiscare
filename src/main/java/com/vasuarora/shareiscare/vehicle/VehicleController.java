package com.vasuarora.shareiscare.vehicle;

import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import com.vasuarora.shareiscare.vehicle.dto.VehicleRequest;
import com.vasuarora.shareiscare.vehicle.dto.VehicleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.addVehicle(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle added successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMyVehicles() {
        List<VehicleResponse> response = vehicleService.getMyVehicles(CurrentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Vehicles fetched successfully.", response));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(@PathVariable Long vehicleId,
                                                                       @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(CurrentUser.id(), vehicleId, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully.", response));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(CurrentUser.id(), vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully.", null));
    }
}
