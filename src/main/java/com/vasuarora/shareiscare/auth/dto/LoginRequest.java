package com.vasuarora.shareiscare.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits.")
        String phone
) {
}
