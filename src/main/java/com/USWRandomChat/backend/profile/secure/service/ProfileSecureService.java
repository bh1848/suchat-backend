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
        checkNicknameEmpty(profileRequest.getNickname());
        boolean isNicknameChangeAllowed = checkNicknameChangeAllowed(profile);
        if (!profile.getNickname().equals(profileRequest.getNickname()) && isNicknameChangeAllowed) {
            profile.setNickname(profileRequest.getNickname());
            profile.setNicknameChangeDate(LocalDateTime.now());
        }
        profile.setMbti(profileRequest.getMbti());
        profile.setIntro(profileRequest.getIntro());
        return profileRepository.save(profile);
    }


    //닉네임 변경 가능 여부 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.NICKNAME_ERROR);
        }
    }

    //해당 사용자의 프로필 존재 확인
    private Profile getProfileById(Long memberId) {
        return profileRepository.findById(memberId)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
    }

    //닉네임 변경 후 30일이 지났는지 확인하는 메소드
    private boolean checkNicknameChangeAllowed(Profile profile) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAfterLastChange = profile.getNicknameChangeDate().plusDays(30);
        if (now.isAfter(thirtyDaysAfterLastChange)) {
            //30일이 지난 경우, 변경 가능
            return true;
        } else {
            //30일이 지나지 않았을 경우, 변경 불가능
            throw new ProfileException(ExceptionType.NICKNAME_EXPIRATION_TIME);
        }
    }
}