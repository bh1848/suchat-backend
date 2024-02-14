package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.MemberNotFoundException;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.exception.ProfileUpdateException;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    // 프로필 조회
    public ProfileResponse getProfile(String targetAccount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("프로필 조회 권한이 없습니다.");
        }

        Member member = memberRepository.findByAccount(targetAccount)
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. targetaccount: " + targetAccount));

        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new RuntimeException("프로필을 찾을 수 없습니다."));

        return new ProfileResponse(profile);
    }

    // 프로필 업데이트
    public ProfileResponse updateProfile(String account, ProfileRequest profileRequest) {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. account: " + account));

        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new RuntimeException("프로필을 찾을 수 없습니다."));
        try {
            checkNicknameEmpty(profileRequest.getNickname());

            // 닉네임이 변경되었을 때만 업데이트
            if (!profile.getNickname().equals(profileRequest.getNickname())) {
                profile.setNickname(profileRequest.getNickname());
                profile.setNicknameChangeDate(LocalDateTime.now());
            }

            profile.setMbti(profileRequest.getMbti());
            profile.setIntro(profileRequest.getIntro());


            profileRepository.save(profile);

            return new ProfileResponse(profile);
        } catch (IllegalArgumentException e) {
            throw new ProfileUpdateException("닉네임을 설정해주세요.");
        } catch (Exception e) {
            throw new ProfileUpdateException("프로필 업데이트 중 오류가 발생했습니다.");
        }
    }

    // 닉네임 빈칸 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 설정해주세요.");
        }
    }
}