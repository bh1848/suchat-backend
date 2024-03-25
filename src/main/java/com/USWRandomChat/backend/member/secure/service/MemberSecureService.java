package com.USWRandomChat.backend.member.secure.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.domain.Token;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.dto.MemberDTO;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberSecureService {
    private static final int NICKNAME_CHANGE_LIMIT_DAYS = 30;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;
    private final JwtRepository jwtRepository;
    //로그아웃
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTemplate.delete(JwtProvider.REFRESH_TOKEN_PREFIX + refreshToken);
            jwtProvider.deleteCookie(response);
        } else {
            log.warn("Attempt to sign out without a refresh token.");
        }
    }

    //회원 탈퇴
    public void withdraw(String accessToken) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        //저장된 Refresh Token을 찾아 삭제
        Optional<Token> refreshToken = jwtRepository.findById(member.getId());
        refreshToken.ifPresent(jwtRepository::delete);

        //회원 삭제
        memberRepository.deleteById(member.getId());
        log.info("회원 탈퇴 완료: memberId={}", account);
    }

    //전체 조회
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }



    //회원가입 시의 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNicknameSignUp(MemberDTO memberDTO) {
        profileRepository.findByNickname(memberDTO.getNickname())
                .ifPresent(profile -> {
                    throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                });
    }

    //이미 가입된 사용자의 닉네임 중복 확인, 닉네임 30일 제한 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNickname(String accessToken, MemberDTO memberDTO) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

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
}