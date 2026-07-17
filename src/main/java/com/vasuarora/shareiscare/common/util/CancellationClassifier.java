package com.vasuarora.shareiscare.common.util;

import com.vasuarora.shareiscare.common.enums.CancellationType;

import java.time.Duration;
import java.time.LocalDateTime;

public final class CancellationClassifier {

    private static final long LATE_THRESHOLD_MINUTES = 30;

    private CancellationClassifier() {
    }

    public static CancellationType classify(LocalDateTime departureTime) {
        long minutesUntilDeparture = Duration.between(LocalDateTime.now(), departureTime).toMinutes();
        return minutesUntilDeparture < LATE_THRESHOLD_MINUTES ? CancellationType.LATE : CancellationType.NORMAL;
    }
}
