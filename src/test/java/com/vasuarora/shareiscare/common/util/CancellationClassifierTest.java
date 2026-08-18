package com.vasuarora.shareiscare.common.util;

import com.vasuarora.shareiscare.common.enums.CancellationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationClassifierTest {

    @Test
    void classify_returnsNormal_whenDepartureIsWellInTheFuture() {
        LocalDateTime departure = LocalDateTime.now().plusMinutes(45);

        assertThat(CancellationClassifier.classify(departure)).isEqualTo(CancellationType.NORMAL);
    }

    @Test
    void classify_returnsLate_whenDepartureIsSoon() {
        LocalDateTime departure = LocalDateTime.now().plusMinutes(10);

        assertThat(CancellationClassifier.classify(departure)).isEqualTo(CancellationType.LATE);
    }

    @Test
    void classify_returnsLate_whenDepartureHasAlreadyPassed() {
        LocalDateTime departure = LocalDateTime.now().minusMinutes(5);

        assertThat(CancellationClassifier.classify(departure)).isEqualTo(CancellationType.LATE);
    }

    @Test
    void classify_returnsNormal_justOverTheThreshold() {
        LocalDateTime departure = LocalDateTime.now().plusMinutes(31);

        assertThat(CancellationClassifier.classify(departure)).isEqualTo(CancellationType.NORMAL);
    }
}
