package com.USWRandomChat.backend.profile.api;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 프로필 조회
    @GetMapping("/get-profile")
    public ResponseEntity<ApiResponse> getProfile(@RequestHeader("Authorization") String accessToken, @RequestParam String targetAccount) {
        try {
            ProfileResponse profileResponse = profileService.getProfile(accessToken, targetAccount);
            return ResponseEntity.ok(new ApiResponse("프로필 조회 성공", profileResponse));
        } catch (ProfileException e) {
            throw new ProfileException(ExceptionType.PROFILE_GET_FAIL);
        }
    }

    // 프로필 업데이트
    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponse> updateProfile(@RequestHeader("Authorization") String accessToken, @RequestBody ProfileRequest profileRequest) {
        try {
            ProfileResponse updatedProfile = profileService.updateProfile(accessToken, profileRequest);
            return ResponseEntity.ok(new ApiResponse("프로필 업데이트 성공", updatedProfile));
        } catch (ProfileException e) {
            throw new ProfileException(ExceptionType.PROFILE_UPDATE_FAIL);
        }
    }
}