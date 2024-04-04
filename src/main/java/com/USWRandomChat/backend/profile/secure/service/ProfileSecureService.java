package com.USWRandomChat.backend.profile.secure.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.security.jwt.service.AuthenticationService;
import com.USWRandomChat.backend.member.domain.Member;
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
    private static final int NICKNAME_CHANGE_LIMIT_MINUTES = 1; //1분으로 지정(테스트 용도)
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
        Profile profile = getProfileById(member.getId());

        //닉네임 변경 로직 검증 및 실행
        if (!profile.getNickname().equals(profileRequest.getNickname())) {
            checkNicknameChangeAllowed(profile);
            checkDuplicateNicknameExceptSelf(profileRequest.getNickname(), member.getId());
            profile.setNickname(profileRequest.getNickname());
            profile.setNicknameChangeDate(LocalDateTime.now());
        }

        //MBTI와 소개 변경
        profile.setMbti(profileRequest.getMbti());
        profile.setIntro(profileRequest.getIntro());
        profileRepository.save(profile);

        return new ProfileResponse(profile);
    }

    //이미 가입된 사용자의 닉네임 중복 확인, 닉네임 30일 제한 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNickname(HttpServletRequest request, String nickname) {
        Member member = authenticationService.getAuthenticatedMember(request);
        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        //닉네임 중복 검사 (현재 사용자의 닉네임 제외)
        profileRepository.findByNickname(nickname)
                .ifPresent(existingProfile -> {
                    if (!existingProfile.getMember().getId().equals(member.getId())) {
                        throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                    }
                });

        //닉네임 변경이 요청된 경우에만 30분 변경 제한 검사 실행
        if (!profile.getNickname().equals(nickname)) {
            checkNicknameChangeAllowed(profile);
        }
    }

    //닉네임 변경 가능 여부 확인
    private void checkNicknameChangeAllowed(Profile profile) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeTime = Optional.ofNullable(profile.getNicknameChangeDate()).orElse(LocalDateTime.MIN);

        if (ChronoUnit.MINUTES.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_MINUTES) {
            throw new AccountException(ExceptionType.NICKNAME_EXPIRATION_TIME);
        }
    }

    //현재 사용자를 제외한 닉네임 중복 확인
    private void checkDuplicateNicknameExceptSelf(String nickname, Long memberId) {
        profileRepository.findByNickname(nickname)
                .ifPresent(existingProfile -> {
                    if (!existingProfile.getMember().getId().equals(memberId)) {
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