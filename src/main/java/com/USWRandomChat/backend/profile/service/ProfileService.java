package com.USWRandomChat.backend.profile.service;

import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.exception.errortype.ProfileException;
import com.USWRandomChat.backend.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.dto.ProfileRequest;
import com.USWRandomChat.backend.profile.dto.ProfileResponse;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
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
    private final JwtProvider jwtProvider;

    // 프로필 조회
    public ProfileResponse getProfile(String targetAccount) {
        ensureAuthenticatedUser();

        Member member = memberRepository.findByAccount(targetAccount)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findById(member.getId())
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
        return new ProfileResponse(profile);
    }

    //프로필 업데이트
    public ProfileResponse updateProfile(String accessToken, ProfileRequest profileRequest) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        //account로 회원 조회
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

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

    // 닉네임 빈칸 확인
    private void checkNicknameEmpty(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.NICKNAME_ERROR);
        }
    }

    //인증된 사용자 확인
    private void ensureAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("프로필 조회 권한이 없습니다.");
        }
    }
}