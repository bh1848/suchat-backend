package com.USWRandomChat.backend.profile.api;

import com.USWRandomChat.backend.member.exception.MemberNotFoundException;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.exception.ProfileUpdateException;
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

    //프로필 조회
    @GetMapping(value = "/get-profile")
    public ResponseEntity<ProfileResponse> getProfile(@RequestParam String targetMemberId) {
        try {
            return new ResponseEntity<>(profileService.getProfile(targetMemberId), HttpStatus.OK);
        } catch (MemberNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //프로필 업데이트
    @PostMapping(value = "/update-profile")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestParam String memberId, @RequestBody ProfileRequest profileRequest) {
        try {
            ProfileResponse updatedProfile = profileService.updateProfile(memberId, profileRequest);
            return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
        } catch (MemberNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ProfileUpdateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
