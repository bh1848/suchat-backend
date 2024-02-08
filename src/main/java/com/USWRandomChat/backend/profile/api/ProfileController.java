package com.USWRandomChat.backend.profile.api;

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

    // 프로필 업데이트
    @PostMapping(value = "/update-profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestParam String memberId,
            @RequestBody ProfileRequest profileRequest) {
        return new ResponseEntity<>(profileService.updateProfile(memberId, profileRequest), HttpStatus.OK);
    }
}

