package com.vasuarora.shareiscare.chat;

import com.vasuarora.shareiscare.chat.dto.MessageRequest;
import com.vasuarora.shareiscare.chat.dto.MessageResponse;
import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{bookingId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@PathVariable Long bookingId,
                                                                     @Valid @RequestBody MessageRequest request) {
        MessageResponse response = chatService.sendMessage(CurrentUser.id(), bookingId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully.", response));
    }

    @GetMapping("/{bookingId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@PathVariable Long bookingId) {
        List<MessageResponse> response = chatService.getMessages(CurrentUser.id(), bookingId);
        return ResponseEntity.ok(ApiResponse.success("Messages fetched successfully.", response));
    }
}
