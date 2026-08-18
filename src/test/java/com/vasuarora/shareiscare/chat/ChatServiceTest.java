package com.vasuarora.shareiscare.chat;

import com.vasuarora.shareiscare.booking.Booking;
import com.vasuarora.shareiscare.booking.BookingRepository;
import com.vasuarora.shareiscare.booking.BookingStatus;
import com.vasuarora.shareiscare.chat.dto.MessageRequest;
import com.vasuarora.shareiscare.chat.dto.MessageResponse;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.ride.Ride;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User driver;
    private User passenger;
    private Booking booking;

    @BeforeEach
    void setUp() {
        driver = User.builder().id(1L).name("Driver").phone("9000000001").build();
        passenger = User.builder().id(2L).name("Passenger").phone("9000000002").build();
        Ride ride = Ride.builder().id(10L).driver(driver).build();
        booking = Booking.builder().id(100L).passenger(passenger).ride(ride).status(BookingStatus.CONFIRMED).build();
    }

    @Test
    void sendMessage_success_asPassenger() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.getReferenceById(2L)).thenReturn(passenger);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = chatService.sendMessage(2L, 100L, new MessageRequest("Running late"));

        assertThat(response.content()).isEqualTo("Running late");
    }

    @Test
    void sendMessage_success_asDriver() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userRepository.getReferenceById(1L)).thenReturn(driver);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = chatService.sendMessage(1L, 100L, new MessageRequest("On my way"));

        assertThat(response.content()).isEqualTo("On my way");
    }

    @Test
    void sendMessage_notParticipant_throws403() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> chatService.sendMessage(99L, 100L, new MessageRequest("Hi")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized to access this chat");
    }

    @Test
    void sendMessage_cancelledBooking_throws400() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> chatService.sendMessage(2L, 100L, new MessageRequest("Hi")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("cancelled booking");
    }

    @Test
    void sendMessage_bookingNotFound_throws404() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage(2L, 100L, new MessageRequest("Hi")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void getMessages_success_returnsHistory() {
        Message message = Message.builder().id(1L).booking(booking).sender(passenger).content("Hi").build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(messageRepository.findByBookingIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(message));

        List<MessageResponse> responses = chatService.getMessages(2L, 100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).content()).isEqualTo("Hi");
    }

    @Test
    void getMessages_notParticipant_throws403() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> chatService.getMessages(99L, 100L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("not authorized to access this chat");
    }
}
