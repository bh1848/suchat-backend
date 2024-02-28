package com.USWRandomChat.backend.profile.api;

import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
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
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String accessToken, @RequestParam String targetAccount) {
        try {
            ProfileResponse profileResponse = profileService.getProfile(accessToken, targetAccount);
            return ResponseEntity.ok(profileResponse);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 유효하지 않습니다.");
        } catch (AccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 계정을 찾을 수 없습니다.");
        } catch (ProfileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("프로필 정보가 존재하지 않습니다.");
        }
    }

    // 프로필 업데이트
    @PatchMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String accessToken, @RequestBody ProfileRequest profileRequest) {
        try {
            ProfileResponse updatedProfile = profileService.updateProfile(accessToken, profileRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (TokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 유효하지 않습니다.");
        } catch (AccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 계정을 찾을 수 없습니다.");
        } catch (ProfileException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("프로필 업데이트 중 오류가 발생했습니다.");
        }
    }
}