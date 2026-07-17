package com.vasuarora.shareiscare.booking;

import com.vasuarora.shareiscare.booking.dto.BookingResponse;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.common.util.CancellationClassifier;
import com.vasuarora.shareiscare.ride.Ride;
import com.vasuarora.shareiscare.ride.RideRepository;
import com.vasuarora.shareiscare.ride.RideStatus;
import com.vasuarora.shareiscare.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse bookRide(Long passengerId, Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ride not found."));

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This ride is not available for booking.");
        }

        if (ride.getDriver().getId().equals(passengerId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot book your own ride.");
        }

        if (ride.getAvailableSeats() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ride is already full.");
        }

        if (bookingRepository.existsByRideIdAndPassengerIdAndStatus(rideId, passengerId, BookingStatus.CONFIRMED)) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already booked this ride.");
        }

        Booking booking = Booking.builder()
                .passenger(userRepository.getReferenceById(passengerId))
                .ride(ride)
                .status(BookingStatus.CONFIRMED)
                .build();

        ride.setAvailableSeats(ride.getAvailableSeats() - 1);

        return BookingResponse.from(bookingRepository.save(booking));
    }

    public List<BookingResponse> getMyBookings(Long passengerId) {
        return bookingRepository.findByPassengerId(passengerId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long passengerId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found."));

        if (!booking.getPassenger().getId().equals(passengerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This booking cannot be cancelled.");
        }

        Ride ride = booking.getRide();
        booking.setCancellationType(CancellationClassifier.classify(ride.getDepartureTime()));
        booking.setStatus(BookingStatus.CANCELLED);
        ride.setAvailableSeats(ride.getAvailableSeats() + 1);

        return BookingResponse.from(booking);
    }
}
