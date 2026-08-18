package com.vasuarora.shareiscare.rating;

import com.vasuarora.shareiscare.booking.Booking;
import com.vasuarora.shareiscare.booking.BookingRepository;
import com.vasuarora.shareiscare.booking.BookingStatus;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.rating.dto.RatingRequest;
import com.vasuarora.shareiscare.rating.dto.RatingResponse;
import com.vasuarora.shareiscare.ride.Ride;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    private User driver;
    private User passenger;
    private Booking booking;
    private RatingRequest request;

    @BeforeEach
    void setUp() {
        driver = User.builder().id(1L).name("Driver").phone("9000000001").build();
        passenger = User.builder().id(2L).name("Passenger").phone("9000000002").build();
        Ride ride = Ride.builder().id(10L).driver(driver).build();
        booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.COMPLETED).build();
        request = new RatingRequest(100L, 5, "Great ride!");
    }

    @Test
    void rateDriver_success() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(ratingRepository.existsByBookingIdAndRatedById(100L, 2L)).thenReturn(false);
        when(userRepository.getReferenceById(2L)).thenReturn(passenger);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.rateDriver(2L, request);

        assertThat(response.score()).isEqualTo(5);
        assertThat(response.ratedUser().id()).isEqualTo(1L);
    }

    @Test
    void rateDriver_wrongPassenger_throws403() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> ratingService.rateDriver(99L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized to rate");
    }

    @Test
    void rateDriver_bookingNotFound_throws404() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rateDriver(2L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void rateDriver_bookingNotCompleted_throws400() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> ratingService.rateDriver(2L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("only available after ride completion");
    }

    @Test
    void rateDriver_alreadyRated_throws409() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(ratingRepository.existsByBookingIdAndRatedById(100L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.rateDriver(2L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already submitted a rating");
    }

    @Test
    void ratePassenger_success() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(ratingRepository.existsByBookingIdAndRatedById(100L, 1L)).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(driver);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingResponse response = ratingService.ratePassenger(1L, request);

        assertThat(response.ratedUser().id()).isEqualTo(2L);
    }

    @Test
    void ratePassenger_wrongDriver_throws403() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> ratingService.ratePassenger(99L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized to rate this passenger");
    }
}
