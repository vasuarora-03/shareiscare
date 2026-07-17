package com.vasuarora.shareiscare.user;

import com.vasuarora.shareiscare.common.exception.ApiException;
import com.vasuarora.shareiscare.user.dto.UpdateProfileRequest;
import com.vasuarora.shareiscare.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public UserProfileResponse getMyProfile(Long userId) {
        return UserProfileResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);
        user.setName(request.name());
        user.setEmail(request.email());
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse uploadProfilePicture(Long userId, MultipartFile file) {
        User user = findUserOrThrow(userId);
        user.setProfilePictureUrl(storeFile(file, "profile-pictures", userId));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse uploadLicense(Long userId, MultipartFile file) {
        User user = findUserOrThrow(userId);
        user.setLicenseDocumentUrl(storeFile(file, "licenses", userId));
        user.setLicenseUploaded(true);
        return UserProfileResponse.from(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private String storeFile(MultipartFile file, String subDir, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File is required.");
        }

        try {
            Path targetDir = Path.of(uploadDir, subDir);
            Files.createDirectories(targetDir);

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = userId + "_" + UUID.randomUUID() + (extension != null ? "." + extension : "");
            Path targetPath = targetDir.resolve(filename);

            file.transferTo(targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file.");
        }
    }
}
