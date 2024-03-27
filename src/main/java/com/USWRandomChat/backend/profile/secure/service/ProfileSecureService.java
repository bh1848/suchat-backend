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

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileSecureService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final AuthenticationService authenticationService;

    //프로필 조회
    public ProfileResponse getProfile(HttpServletRequest request, String targetAccount) {
        authenticationService.getAuthenticatedMember(request);

        //대상 사용자의 프로필 조회
        Member targetMember = memberRepository.findByAccount(targetAccount)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findById(targetMember.getId())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        return new ProfileResponse(profile);
    }

    //프로필 업데이트
    public ProfileResponse updateProfile(HttpServletRequest request, ProfileRequest profileRequest) {
        Member member = authenticationService.getAuthenticatedMember(request);

        Profile profile = updateProfileDetails(member, profileRequest);

        return new ProfileResponse(profile);
    }

    //닉네임 빈칸 확인 및 프로필 업데이트 로직
    private Profile updateProfileDetails(Member member, ProfileRequest profileRequest) {
        Profile profile = profileRepository.findById(member.getId())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        checkNicknameEmpty(profileRequest.getNickname());

        //닉네임이 변경되었을 때만 업데이트
        if (!profile.getNickname().equals(profileRequest.getNickname())) {
            profile.setNickname(profileRequest.getNickname());
            profile.setNicknameChangeDate(LocalDateTime.now());
        }

        profile.setMbti(profileRequest.getMbti());
        profile.setIntro(profileRequest.getIntro());

        return profileRepository.save(profile);
    }

    //닉네임 빈칸 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.NICKNAME_ERROR);
        }
    }
}