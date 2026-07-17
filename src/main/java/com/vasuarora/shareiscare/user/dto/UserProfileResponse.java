package com.vasuarora.shareiscare.user.dto;

import com.vasuarora.shareiscare.user.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String phone,
        String email,
        String profilePictureUrl,
        boolean licenseUploaded,
        LocalDateTime createdAt
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.isLicenseUploaded(),
                user.getCreatedAt()
        );
    }
}
