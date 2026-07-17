package com.vasuarora.shareiscare.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(

        @NotBlank(message = "Message content is required.")
        String content
) {
}
