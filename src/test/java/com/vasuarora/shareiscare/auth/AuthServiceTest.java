package com.vasuarora.shareiscare.auth;

import com.vasuarora.shareiscare.auth.dto.AuthResponse;
import com.vasuarora.shareiscare.auth.dto.LoginRequest;
import com.vasuarora.shareiscare.auth.dto.SignupRequest;
import com.vasuarora.shareiscare.auth.dto.VerifyOtpRequest;
import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.User;
import com.vasuarora.shareiscare.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_success_savesUserAndSendsOtp() {
        SignupRequest request = new SignupRequest("Test User", "9000000001", "test@example.com");
        when(userRepository.existsByPhone("9000000001")).thenReturn(false);

        authService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPhone()).isEqualTo("9000000001");
        verify(otpService).generateAndSend("9000000001");
    }

    @Test
    void signup_duplicatePhone_throws409_andDoesNotSaveOrSendOtp() {
        SignupRequest request = new SignupRequest("Test User", "9000000001", "test@example.com");
        when(userRepository.existsByPhone("9000000001")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
        verify(otpService, never()).generateAndSend(any());
    }

    @Test
    void login_success_sendsOtpForExistingUser() {
        LoginRequest request = new LoginRequest("9000000001");
        when(userRepository.findByPhone("9000000001"))
                .thenReturn(Optional.of(User.builder().id(1L).phone("9000000001").build()));

        authService.login(request);

        verify(otpService).generateAndSend("9000000001");
    }

    @Test
    void login_unknownPhone_throws404_andDoesNotSendOtp() {
        LoginRequest request = new LoginRequest("9000000001");
        when(userRepository.findByPhone("9000000001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("No account found");

        verify(otpService, never()).generateAndSend(any());
    }

    @Test
    void verifyOtp_success_returnsTokenAfterVerification() {
        VerifyOtpRequest request = new VerifyOtpRequest("9000000001", "123456");
        User user = User.builder().id(1L).name("Test User").phone("9000000001").build();
        when(userRepository.findByPhone("9000000001")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.verifyOtp(request);

        verify(otpService).verify("9000000001", "123456");
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.user().id()).isEqualTo(1L);
    }
}
