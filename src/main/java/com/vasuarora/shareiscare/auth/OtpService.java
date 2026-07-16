package com.vasuarora.shareiscare.auth;

import com.vasuarora.shareiscare.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {

    @Value("${otp.expiry-minutes}")
    private int expiryMinutes;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public void generateAndSend(String phone) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(phone, new OtpEntry(code, LocalDateTime.now().plusMinutes(expiryMinutes)));

        // V1 has no SMS gateway integration - log the OTP to simulate delivery.
        log.info("OTP for {}: {} (valid {} min)", phone, code, expiryMinutes);
    }

    public void verify(String phone, String code) {
        OtpEntry entry = otpStore.get(phone);

        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now()) || !entry.code().equals(code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }

        otpStore.remove(phone);
    }

    private record OtpEntry(String code, LocalDateTime expiresAt) {
    }
}
