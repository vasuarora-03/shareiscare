package com.vasuarora.shareiscare.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(

        @NotBlank(message = "Phone number is required.")
        String phone,

        @NotBlank(message = "OTP is required.")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits.")
        String otp
) {
}
