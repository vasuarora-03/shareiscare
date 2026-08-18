package com.vasuarora.shareiscare.booking;

import com.vasuarora.shareiscare.booking.dto.BookingResponse;
import com.vasuarora.shareiscare.common.enums.CancellationType;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.ride.Ride;
import com.vasuarora.shareiscare.ride.RideRepository;
import com.vasuarora.shareiscare.ride.RideStatus;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User driver;
    private User passenger;
    private Ride ride;

    @BeforeEach
    void setUp() {
        driver = User.builder().id(1L).name("Driver").phone("9000000001").build();
        passenger = User.builder().id(2L).name("Passenger").phone("9000000002").build();
        ride = Ride.builder()
                .id(10L)
                .driver(driver)
                .vehicle(com.vasuarora.shareiscare.vehicle.Vehicle.builder().id(5L).make("Maruti").model("Swift")
                        .registrationNumber("DL01AB1234").seatCapacity(4).build())
                .source("Delhi")
                .destination("Noida")
                .status(RideStatus.SCHEDULED)
                .availableSeats(2)
                .departureTime(LocalDateTime.now().plusHours(2))
                .estimatedArrival(LocalDateTime.now().plusHours(3))
                .build();
    }

    @Test
    void bookRide_success_decrementsSeatsAndSavesConfirmedBooking() {
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));
        when(bookingRepository.existsByRideIdAndPassengerIdAndStatus(10L, 2L, BookingStatus.CONFIRMED)).thenReturn(false);
        when(userRepository.getReferenceById(2L)).thenReturn(passenger);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.bookRide(2L, 10L);

        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(ride.getAvailableSeats()).isEqualTo(1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookRide_rideNotFound_throws404() {
        when(rideRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookRide(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ride not found");
    }

    @Test
    void bookRide_rideNotScheduled_throws400() {
        ride.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.bookRide(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not available for booking");
    }

    @Test
    void bookRide_ownRide_throws400() {
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.bookRide(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot book your own ride");
    }

    @Test
    void bookRide_rideFull_throws400() {
        ride.setAvailableSeats(0);
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.bookRide(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already full");
    }

    @Test
    void bookRide_alreadyBooked_throws409() {
        when(rideRepository.findById(10L)).thenReturn(Optional.of(ride));
        when(bookingRepository.existsByRideIdAndPassengerIdAndStatus(10L, 2L, BookingStatus.CONFIRMED)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.bookRide(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void cancelBooking_success_normalWhenFarFromDeparture() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancelBooking(2L, 100L);

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(response.cancellationType()).isEqualTo(CancellationType.NORMAL);
        assertThat(ride.getAvailableSeats()).isEqualTo(3);
    }

    @Test
    void cancelBooking_success_lateWhenCloseToDeparture() {
        ride.setDepartureTime(LocalDateTime.now().plusMinutes(10));
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancelBooking(2L, 100L);

        assertThat(response.cancellationType()).isEqualTo(CancellationType.LATE);
    }

    @Test
    void cancelBooking_notFound_throws404() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(2L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void cancelBooking_wrongPassenger_throws403() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(99L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void cancelBooking_alreadyCancelled_throws400() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CANCELLED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(2L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void confirmCompletion_success_marksBookingCompleted() {
        ride.setStatus(RideStatus.COMPLETED);
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.confirmCompletion(2L, 100L);

        assertThat(response.status()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void confirmCompletion_wrongPassenger_throws403() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCompletion(99L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void confirmCompletion_bookingNotConfirmed_throws400() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CANCELLED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCompletion(2L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cannot be confirmed");
    }

    @Test
    void confirmCompletion_rideNotYetCompleted_throws400() {
        Booking booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmCompletion(2L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("driver must mark");
    }
}
