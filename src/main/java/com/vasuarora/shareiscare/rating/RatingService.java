package com.vasuarora.shareiscare.rating;

import com.vasuarora.shareiscare.booking.Booking;
import com.vasuarora.shareiscare.booking.BookingRepository;
import com.vasuarora.shareiscare.booking.BookingStatus;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.rating.dto.RatingRequest;
import com.vasuarora.shareiscare.rating.dto.RatingResponse;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public RatingResponse rateDriver(Long passengerId, RatingRequest request) {
        Booking booking = findBookingOrThrow(request.bookingId());

        if (!booking.getPassenger().getId().equals(passengerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to rate this ride.");
        }

        Rating rating = createRating(booking, passengerId, booking.getRide().getDriver(), request);
        return RatingResponse.from(rating);
    }

    @Transactional
    public RatingResponse ratePassenger(Long driverId, RatingRequest request) {
        Booking booking = findBookingOrThrow(request.bookingId());

        if (!booking.getRide().getDriver().getId().equals(driverId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to rate this passenger.");
        }

        Rating rating = createRating(booking, driverId, booking.getPassenger(), request);
        return RatingResponse.from(rating);
    }

    private Rating createRating(Booking booking, Long raterId, User ratee, RatingRequest request) {
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rating is only available after ride completion.");
        }

        if (ratingRepository.existsByBookingIdAndRatedById(booking.getId(), raterId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already submitted a rating for this ride.");
        }

        Rating rating = Rating.builder()
                .booking(booking)
                .ratedBy(userRepository.getReferenceById(raterId))
                .ratedUser(ratee)
                .score(request.score())
                .comment(request.comment())
                .isAutoAssigned(false)
                .build();

        return ratingRepository.save(rating);
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found."));
    }
}
