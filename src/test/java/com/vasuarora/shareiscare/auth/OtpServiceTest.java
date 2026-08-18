package com.vasuarora.shareiscare.auth;

import com.vasuarora.shareiscare.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 5);
    }

    // The generated OTP is a random 6-digit code that's only ever logged, never returned by
    // the service. To assert on it, read it straight out of the in-memory store via reflection.
    @SuppressWarnings("unchecked")
    private String storedCodeFor(String phone) throws Exception {
        Map<String, Object> store = (Map<String, Object>) ReflectionTestUtils.getField(otpService, "otpStore");
        Object entry = store.get(phone);
        Method codeMethod = entry.getClass().getDeclaredMethod("code");
        codeMethod.setAccessible(true);
        return (String) codeMethod.invoke(entry);
    }

    @Test
    void verify_succeeds_withTheCodeThatWasGenerated() throws Exception {
        otpService.generateAndSend("9000000001");
        String realCode = storedCodeFor("9000000001");

        otpService.verify("9000000001", realCode);
        // no exception = success
    }

    @Test
    void verify_isOneTimeUse_secondAttemptFailsEvenWithCorrectCode() throws Exception {
        otpService.generateAndSend("9000000001");
        String realCode = storedCodeFor("9000000001");
        otpService.verify("9000000001", realCode);

        assertThatThrownBy(() -> otpService.verify("9000000001", realCode))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verify_wrongCode_throws400() throws Exception {
        otpService.generateAndSend("9000000001");
        String realCode = storedCodeFor("9000000001");
        int wrongCode = (Integer.parseInt(realCode) + 1) % 1_000_000;
        String wrongCodeStr = String.format("%06d", wrongCode);

        assertThatThrownBy(() -> otpService.verify("9000000001", wrongCodeStr))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verify_noOtpEverSent_throws400() {
        assertThatThrownBy(() -> otpService.verify("9000000099", "123456"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verify_expiredOtp_throws400_regardlessOfCode() {
        ReflectionTestUtils.setField(otpService, "expiryMinutes", -1);
        otpService.generateAndSend("9000000001");

        assertThatThrownBy(() -> otpService.verify("9000000001", "000000"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }
}
