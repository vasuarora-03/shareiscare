package com.vasuarora.shareiscare.auth;

import com.vasuarora.shareiscare.auth.dto.AuthResponse;
import com.vasuarora.shareiscare.auth.dto.AuthUserSummary;
import com.vasuarora.shareiscare.auth.dto.LoginRequest;
import com.vasuarora.shareiscare.auth.dto.SignupRequest;
import com.vasuarora.shareiscare.auth.dto.VerifyOtpRequest;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone number already registered.");
        }

        User user = User.builder()
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .build();

        userRepository.save(user);
        otpService.generateAndSend(request.phone());
    }

    public void login(LoginRequest request) {
        userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No account found for this phone number."));

        otpService.generateAndSend(request.phone());
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verify(request.phone(), request.otp());

        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No account found for this phone number."));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, AuthUserSummary.from(user));
    }
}
