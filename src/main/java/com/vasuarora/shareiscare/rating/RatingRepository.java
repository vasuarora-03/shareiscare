package com.vasuarora.shareiscare.rating;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByBookingIdAndRatedById(Long bookingId, Long ratedById);
}
