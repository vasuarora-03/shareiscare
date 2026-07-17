package com.vasuarora.shareiscare.chat.dto;

import com.vasuarora.shareiscare.chat.Message;
import com.vasuarora.shareiscare.user.User;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        SenderInfo sender,
        String content,
        LocalDateTime createdAt
) {

    public record SenderInfo(Long id, String name) {
    }

    public static MessageResponse from(Message message) {
        User sender = message.getSender();
        return new MessageResponse(
                message.getId(),
                new SenderInfo(sender.getId(), sender.getName()),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
