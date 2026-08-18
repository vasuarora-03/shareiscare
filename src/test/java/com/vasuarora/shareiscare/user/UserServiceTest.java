package com.vasuarora.shareiscare.user;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.dto.UpdateProfileRequest;
import com.vasuarora.shareiscare.user.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @TempDir
    Path tempDir;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User").phone("9000000001").build();
        ReflectionTestUtils.setField(userService, "uploadDir", tempDir.toString());
    }

    @Test
    void getMyProfile_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getMyProfile(1L);

        assertThat(response.name()).isEqualTo("Test User");
    }

    @Test
    void getMyProfile_notFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_success_overwritesNameAndEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.updateProfile(1L, new UpdateProfileRequest("New Name", "new@example.com"));

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.email()).isEqualTo("new@example.com");
    }

    @Test
    void updateProfile_notFound_throws404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(1L, new UpdateProfileRequest("New Name", "new@example.com")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void uploadProfilePicture_success_storesFileAndSetsUrl() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        UserProfileResponse response = userService.uploadProfilePicture(1L, file);

        assertThat(response.profilePictureUrl()).isNotBlank();
        assertThat(Files.exists(Path.of(response.profilePictureUrl()))).isTrue();
    }

    @Test
    void uploadProfilePicture_emptyFile_throws400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MockMultipartFile emptyFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> userService.uploadProfilePicture(1L, emptyFile))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("File is required");
    }

    @Test
    void uploadProfilePicture_nullFile_throws400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.uploadProfilePicture(1L, null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("File is required");
    }

    @Test
    void uploadLicense_success_marksLicenseUploadedAndSetsUrl() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "fake-pdf-bytes".getBytes());

        UserProfileResponse response = userService.uploadLicense(1L, file);

        assertThat(response.licenseUploaded()).isTrue();
        assertThat(user.getLicenseDocumentUrl()).isNotBlank();
    }
}
