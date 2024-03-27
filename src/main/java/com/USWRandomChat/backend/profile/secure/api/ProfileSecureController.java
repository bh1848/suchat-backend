package com.USWRandomChat.backend.profile.secure.api;

import com.USWRandomChat.backend.global.response.ApiResponse;
import com.USWRandomChat.backend.member.dto.MemberDto;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.secure.service.ProfileSecureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/secure/profile")
@RequiredArgsConstructor
public class ProfileSecureController {

    private final ProfileSecureService profileSecureService;

    //자신의 프로필 조회
    @GetMapping("/get-my-profile")
    public ResponseEntity<ApiResponse> getMyProfile(HttpServletRequest request) {
        ProfileResponse profileResponse = profileSecureService.getMyProfile(request);
        return ResponseEntity.ok(new ApiResponse("프로필 조회 성공.", profileResponse));
    }

    //다른 사용자의 프로필 조회
    @GetMapping("/get-other-profile")
    public ResponseEntity<ApiResponse> getOtherProfile(HttpServletRequest request, @RequestParam String targetAccount) {
        ProfileResponse profileResponse = profileSecureService.getOtherProfile(request, targetAccount);
        return ResponseEntity.ok(new ApiResponse("프로필 조회 성공.", profileResponse));
    }

    //자신의 프로필 업데이트
    @PatchMapping("/update-my-profile")
    public ResponseEntity<ApiResponse> updateMyProfile(HttpServletRequest request, @RequestBody ProfileRequest profileRequest) {
        ProfileResponse updatedProfile = profileSecureService.updateMyProfile(request, profileRequest);
        return ResponseEntity.ok(new ApiResponse("프로필 업데이트 성공.", updatedProfile));
    }

    //이미 가입된 사용자의 닉네임 중복 확인
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<ApiResponse> checkDuplicateNickname(HttpServletRequest request, @RequestBody MemberDto memberDTO) {
        profileSecureService.checkDuplicateNickname(request, memberDTO);
        return ResponseEntity.ok(new ApiResponse("사용 가능한 닉네임입니다."));
    }
}