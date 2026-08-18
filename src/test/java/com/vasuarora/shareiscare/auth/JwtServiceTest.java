package com.vasuarora.shareiscare.auth;

import com.vasuarora.shareiscare.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // @Value fields are normally injected by Spring; set them by hand since this is a plain unit test.
        ReflectionTestUtils.setField(jwtService, "secret", "unit-test-secret-key-must-be-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generateToken_thenExtractUserId_roundTripsToSameId() {
        User user = User.builder().id(42L).name("Test User").phone("9000000001").build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void isValid_returnsTrue_forFreshlyIssuedToken() {
        User user = User.builder().id(1L).name("Test User").phone("9000000001").build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalse_forGarbageToken() {
        assertThat(jwtService.isValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void isValid_returnsFalse_forExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        User user = User.builder().id(1L).name("Test User").phone("9000000001").build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token)).isFalse();
    }
}
