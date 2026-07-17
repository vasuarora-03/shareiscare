package com.vasuarora.shareiscare.booking;

import com.vasuarora.shareiscare.booking.dto.BookingResponse;
import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/rides/{rideId}/book")
    public ResponseEntity<ApiResponse<BookingResponse>> bookRide(@PathVariable Long rideId) {
        BookingResponse response = bookingService.bookRide(CurrentUser.id(), rideId);
        return ResponseEntity.ok(ApiResponse.success("Ride booked successfully.", response));
    }

    @GetMapping("/bookings/me")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        List<BookingResponse> response = bookingService.getMyBookings(CurrentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched successfully.", response));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.cancelBooking(CurrentUser.id(), bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully.", response));
    }
}
