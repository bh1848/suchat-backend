package com.USWRandomChat.backend.profile.api;

import com.USWRandomChat.backend.profile.dto.ProfileDTO;
import com.USWRandomChat.backend.profile.exception.ProfileAlreadyExistsException;
import com.USWRandomChat.backend.profile.exception.ProfileNotFoundException;
import com.USWRandomChat.backend.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/get-profile")
    public ResponseEntity<ProfileDTO> getProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            ProfileDTO profile = profileService.getProfile(token);
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } catch (ProfileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create-profile")
    public ResponseEntity<String> createProfile(HttpServletRequest request, @RequestBody ProfileDTO requestDTO) {
        String token = request.getHeader("Authorization");
        try {
            profileService.createProfile(token, requestDTO);
            return new ResponseEntity<>("프로필이 등록되었습니다.", HttpStatus.CREATED);
        } catch (ProfileAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<String> updateProfile(HttpServletRequest request, @RequestBody ProfileDTO requestDTO) {
        String token = request.getHeader("Authorization");
        try {
            profileService.updateProfile(token, requestDTO);
            return new ResponseEntity<>("프로필이 수정되었습니다.", HttpStatus.OK);
        } catch (ProfileNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete-profile")
    public ResponseEntity<String> deleteProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        try {
            profileService.deleteProfile(token);
            return new ResponseEntity<>("프로필이 삭제되었습니다.", HttpStatus.OK);
        } catch (ProfileNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
