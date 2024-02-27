package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final JwtProvider jwtProvider;

    // 프로필 조회
    public ProfileResponse getProfile(String accessToken, String targetAccount) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        Member member = memberRepository.findByAccount(targetAccount)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findById(member.getId())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
        return new ProfileResponse(profile);
    }

    //프로필 업데이트
    public ProfileResponse updateProfile(String accessToken, ProfileRequest request) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        //account로 회원 조회
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = updateProfileDetails(member, request);

        return new ProfileResponse(profile);
    }

    //닉네임 빈칸 확인 및 프로필 업데이트 로직
    private Profile updateProfileDetails(Member member, ProfileRequest request) {
        Profile profile = profileRepository.findById(member.getId())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        checkNicknameEmpty(request.getNickname());

        //닉네임이 변경되었을 때만 업데이트
        if (!profile.getNickname().equals(request.getNickname())) {
            profile.setNickname(request.getNickname());
            profile.setNicknameChangeDate(LocalDateTime.now());
        }

        profile.setMbti(request.getMbti());
        profile.setIntro(request.getIntro());

        return profileRepository.save(profile);
    }

    //닉네임 빈칸 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.NICKNAME_ERROR);
        }
    }
}