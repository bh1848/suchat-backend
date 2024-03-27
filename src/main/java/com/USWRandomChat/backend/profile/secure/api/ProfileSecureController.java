package com.USWRandomChat.backend.profile.secure.api;

import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.secure.service.ProfileSecureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileSecureController {

    private final ProfileSecureService profileSecureService;

    //프로필 조회
    @GetMapping("/get-profile")
    public ResponseEntity<ApiResponse> getProfile(HttpServletRequest request, @RequestParam String targetAccount) {
        ProfileResponse profileResponse = profileSecureService.getProfile(request, targetAccount);
        return ResponseEntity.ok(new ApiResponse("프로필 조회 성공했습니다.", profileResponse));
    }

    //프로필 업데이트
    @PatchMapping("/update-profile")
    public ResponseEntity<ApiResponse> updateProfile(HttpServletRequest request, @RequestBody ProfileRequest profileRequest) {
        ProfileResponse updatedProfile = profileSecureService.updateProfile(request, profileRequest);
        return ResponseEntity.ok(new ApiResponse("프로필 업데이트 성공", updatedProfile));
    }
}