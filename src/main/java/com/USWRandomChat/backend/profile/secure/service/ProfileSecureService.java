package com.USWRandomChat.backend.profile.secure.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.security.jwt.service.AuthenticationService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.dto.MemberDto;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileSecureService {
    private static final int NICKNAME_CHANGE_LIMIT_DAYS = 30;
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final AuthenticationService authenticationService;

    //자신의 프로필 조회
    public ProfileResponse getMyProfile(HttpServletRequest request) {
        Member member = authenticationService.getAuthenticatedMember(request);
        Profile myProfile = getProfileById(member.getId());
        return new ProfileResponse(myProfile);
    }

    //다른 사용자의 프로필 조회
    public ProfileResponse getOtherProfile(HttpServletRequest request, String targetAccount) {
        authenticationService.getAuthenticatedMember(request);
        Member targetMember = memberRepository.findByAccount(targetAccount)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        Profile profile = getProfileById(targetMember.getId());
        return new ProfileResponse(profile);
    }

    //자신의 프로필 업데이트
    public ProfileResponse updateMyProfile(HttpServletRequest request, ProfileRequest profileRequest) {
        Member member = authenticationService.getAuthenticatedMember(request);
        Profile profile = updateProfileDetails(member, profileRequest);
        return new ProfileResponse(profile);
    }

    //프로필 업데이트 검증 로직
    private Profile updateProfileDetails(Member member, ProfileRequest profileRequest) {
        Profile profile = getProfileById(member.getId());

        boolean isNicknameChanged = !profile.getNickname().equals(profileRequest.getNickname());
        if (isNicknameChanged) {
            profile.setNickname(profileRequest.getNickname());
            profile.setNicknameChangeDate(LocalDateTime.now());
        }

        if (isNicknameChanged || !profile.getMbti().equals(profileRequest.getMbti()) || !profile.getIntro().equals(profileRequest.getIntro())) {
            profile.setMbti(profileRequest.getMbti());
            profile.setIntro(profileRequest.getIntro());
            profileRepository.save(profile);
        }

        return profile;
    }

    //이미 가입된 사용자의 닉네임 중복 확인, 닉네임 30일 제한 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNickname(HttpServletRequest request, MemberDto memberDTO) {
        Member member = authenticationService.getAuthenticatedMember(request);

        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeTime = Optional.ofNullable(profile.getNicknameChangeDate())
                .orElse(LocalDateTime.MIN);

        //30일 이내에 변경한 경우 예외 발생
        if (ChronoUnit.DAYS.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_DAYS) {
            throw new AccountException(ExceptionType.NICKNAME_EXPIRATION_TIME);
        }

        //닉네임 중복 확인 (현재 사용자의 닉네임 제외)
        profileRepository.findByNickname(memberDTO.getNickname())
                .ifPresent(existingProfile -> {
                    if (!existingProfile.getMember().getId().equals(member.getId())) {
                        throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                    }
                });
    }

    //해당 사용자의 프로필 존재 확인
    private Profile getProfileById(Long memberId) {
        return profileRepository.findById(memberId)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
    }
}