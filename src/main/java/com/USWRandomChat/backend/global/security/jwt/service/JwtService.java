package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    //토큰 재발급
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) throws TokenException, AccountException {
        //리프레시 토큰 유효성 검사
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        validateRefreshToken(refreshToken);

        //리프레시 토큰과 연관된 계정 조회
        String account = fetchAccountFromRefreshToken(refreshToken);
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        //사용자 권한 정보 추출
        List<String> roleNames = extractRoleNames(member);

        //새로운 엑세스 토큰과 리프레시 토큰 발급(보안을 높이기 위해 토큰을 둘 다 계속 갱신)
        String newAccessToken = jwtProvider.createAccessToken(member.getAccount(), roleNames);
        String newRefreshToken = replaceRefreshToken(response, refreshToken, member.getAccount());

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    //로그아웃 시 리프레시 토큰 삭제
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTemplate.delete("RT:" + refreshToken);
            jwtProvider.deleteCookie(response);
        } else {
            log.info("Attempt to sign out without a refresh token.");
        }
    }

    //리프레시 토큰 유효성 검사
    private void validateRefreshToken(String refreshToken) throws TokenException {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new TokenException(ExceptionType.REFRESH_TOKEN_EXPIRED);
        }
    }

    //리프레시 토큰으로 연관된 계정 정보 조회
    private String fetchAccountFromRefreshToken(String refreshToken) throws TokenException {
        return redisTemplate.opsForValue().get("RT:" + refreshToken);
    }

    private List<String> extractRoleNames(Member member) {
        return member.getRoles().stream()
                .map(Authority::getName)
                .collect(Collectors.toList());
    }

    private String replaceRefreshToken(HttpServletResponse response, String oldRefreshToken, String account) {
        String newRefreshToken = jwtProvider.createRefreshToken();
        redisTemplate.delete("RT:" + oldRefreshToken);
        jwtProvider.addCookieAndSaveTokenInRedis(response, newRefreshToken, account);
        return newRefreshToken;
    }
}