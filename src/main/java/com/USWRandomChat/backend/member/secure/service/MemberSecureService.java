package com.USWRandomChat.backend.member.secure.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.global.security.jwt.service.AuthenticationService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.dto.MemberDto;
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
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberSecureService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final JwtRepository jwtRepository;
    private final AuthenticationService authenticationService;

    //로그아웃
    public void signOut(HttpServletRequest request, HttpServletResponse response) throws TokenException{
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTemplate.delete(JwtProvider.REFRESH_TOKEN_PREFIX + refreshToken);
            jwtProvider.deleteCookie(response);
        } else {
            log.warn("리프레시 토큰이 없습니다.");
            throw new TokenException(ExceptionType.REFRESH_TOKEN_EXPIRED);
        }
    }

    //회원 탈퇴
    public void withdraw(HttpServletRequest request) {
        Member member = authenticationService.getAuthenticatedMember(request);
        jwtRepository.deleteById(member.getAccount());
        memberRepository.deleteById(member.getId());
        log.info("회원 탈퇴 완료: account={}", member.getAccount());
    }
}