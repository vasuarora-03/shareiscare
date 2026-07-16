package com.vasuarora.shareiscare.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(

        @NotBlank(message = "Name is required.")
        String name,

        @NotBlank(message = "Phone number is required.")
        @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits.")
        String phone,

        @Email(message = "Email must be valid.")
        String email
) {
}