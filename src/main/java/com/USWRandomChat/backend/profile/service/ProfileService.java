package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileDTO;
import com.USWRandomChat.backend.profile.exception.ProfileAlreadyExistsException;
import com.USWRandomChat.backend.profile.exception.ProfileNotFoundException;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final JwtProvider jwtProvider;

    //프로필 조회
    public ProfileDTO getProfile(String token) {
        // 토큰에서 memberId를 추출합니다.
        String memberId = jwtProvider.getMemberId(token);

        // memberId를 기반으로 프로필을 가져오는 메서드를 호출합니다.
        Optional<Member> memberOptional = memberRepository.findByMemberId(memberId);
        Member member = memberOptional.orElseThrow(() -> new ProfileNotFoundException("사용자를 찾을 수 없습니다."));

        Profile profile = member.getProfile();

        if (profile == null) {
            throw new ProfileNotFoundException("프로필을 찾을 수 없습니다.");
        }

        return ProfileDTO.fromEntity(profile);
    }
    
    //프로필 생성
    public void createProfile(String token, ProfileDTO request) {
        String memberId = jwtProvider.getMemberId(token);

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ProfileNotFoundException("사용자를 찾을 수 없습니다."));

        if (member.getProfile() != null) {
            throw new ProfileAlreadyExistsException("프로필이 이미 존재합니다.");
        }

        Profile profile = Profile.builder()
                .mbti(request.getMbti())
                .intro(request.getIntro())
                .nickname(request.getNickname())
                .build();

        profileRepository.save(profile);
    }
    
    //프로필 업데이트
    public void updateProfile(String token, ProfileDTO request) {
        String memberId = jwtProvider.getMemberId(token);

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ProfileNotFoundException("사용자를 찾을 수 없습니다."));

        Profile profile = member.getProfile();
        if (profile == null) {
            throw new ProfileNotFoundException("프로필을 찾을 수 없습니다.");
        }

        profile.update(request.getMbti(), request.getIntro(), request.getNickname());
    }
    
    //프로필 삭제
    public void deleteProfile(String token) {
        String memberId = jwtProvider.getMemberId(token);

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ProfileNotFoundException("사용자를 찾을 수 없습니다."));

        Profile profile = member.getProfile();
        if (profile == null) {
            throw new ProfileNotFoundException("프로필을 찾을 수 없습니다.");
        }

        profileRepository.delete(profile);
    }

}
