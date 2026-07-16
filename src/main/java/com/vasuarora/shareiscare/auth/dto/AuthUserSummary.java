package com.vasuarora.shareiscare.auth.dto;

import com.vasuarora.shareiscare.user.User;

public record AuthUserSummary(Long id, String name, String phone, String email) {

    public static AuthUserSummary from(User user) {
        return new AuthUserSummary(user.getId(), user.getName(), user.getPhone(), user.getEmail());
    }
}
