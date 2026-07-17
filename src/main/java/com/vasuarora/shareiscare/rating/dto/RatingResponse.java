package com.vasuarora.shareiscare.rating.dto;

import com.vasuarora.shareiscare.rating.Rating;
import com.vasuarora.shareiscare.user.User;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        Long bookingId,
        RatedUserInfo ratedUser,
        Integer score,
        String comment,
        boolean isAutoAssigned,
        LocalDateTime createdAt
) {

    public record RatedUserInfo(Long id, String name) {
    }

    public static RatingResponse from(Rating rating) {
        User ratedUser = rating.getRatedUser();

        return new RatingResponse(
                rating.getId(),
                rating.getBooking().getId(),
                new RatedUserInfo(ratedUser.getId(), ratedUser.getName()),
                rating.getScore(),
                rating.getComment(),
                rating.isAutoAssigned(),
                rating.getCreatedAt()
        );
    }
}
