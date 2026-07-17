package com.vasuarora.shareiscare.chat;

import com.vasuarora.shareiscare.booking.Booking;
import com.vasuarora.shareiscare.booking.BookingRepository;
import com.vasuarora.shareiscare.booking.BookingStatus;
import com.vasuarora.shareiscare.chat.dto.MessageRequest;
import com.vasuarora.shareiscare.chat.dto.MessageResponse;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, Long bookingId, MessageRequest request) {
        Booking booking = findBookingOrThrow(bookingId);
        assertParticipant(booking, senderId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chat is not available for a cancelled booking.");
        }

        Message message = Message.builder()
                .booking(booking)
                .sender(userRepository.getReferenceById(senderId))
                .content(request.content())
                .build();

        return MessageResponse.from(messageRepository.save(message));
    }

    public List<MessageResponse> getMessages(Long userId, Long bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        assertParticipant(booking, userId);

        return messageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found."));
    }

    private void assertParticipant(Booking booking, Long userId) {
        boolean isPassenger = booking.getPassenger().getId().equals(userId);
        boolean isDriver = booking.getRide().getDriver().getId().equals(userId);

        if (!isPassenger && !isDriver) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not authorized to access this chat.");
        }
    }
}
