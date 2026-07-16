package com.vasuarora.shareiscare.auth.dto;

public record AuthResponse(String token, AuthUserSummary user) {
}