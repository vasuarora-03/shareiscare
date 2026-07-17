package com.vasuarora.shareiscare.rating;

import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.rating.dto.RatingRequest;
import com.vasuarora.shareiscare.rating.dto.RatingResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/driver")
    public ResponseEntity<ApiResponse<RatingResponse>> rateDriver(@Valid @RequestBody RatingRequest request) {
        RatingResponse response = ratingService.rateDriver(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Driver rated successfully.", response));
    }

    @PostMapping("/passenger")
    public ResponseEntity<ApiResponse<RatingResponse>> ratePassenger(@Valid @RequestBody RatingRequest request) {
        RatingResponse response = ratingService.ratePassenger(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Passenger rated successfully.", response));
    }
}
