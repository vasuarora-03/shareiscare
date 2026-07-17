package com.vasuarora.shareiscare.user;

import com.vasuarora.shareiscare.common.dto.ApiResponse;
import com.vasuarora.shareiscare.security.CurrentUser;
import com.vasuarora.shareiscare.user.dto.UpdateProfileRequest;
import com.vasuarora.shareiscare.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userService.getMyProfile(CurrentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully.", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(CurrentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", response));
    }

    @PostMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        UserProfileResponse response = userService.uploadProfilePicture(CurrentUser.id(), file);
        return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully.", response));
    }

    @PostMapping(value = "/me/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadLicense(@RequestParam("file") MultipartFile file) {
        UserProfileResponse response = userService.uploadLicense(CurrentUser.id(), file);
        return ResponseEntity.ok(ApiResponse.success("Driving license uploaded successfully.", response));
    }
}
