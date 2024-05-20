package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.RefreshTokenException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    
    //토큰 재발급
    @Transactional(readOnly = true)
    public TokenDto renewToken(HttpServletRequest request, HttpServletResponse response) throws RefreshTokenException, AccountException {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        jwtProvider.validateRefreshToken(refreshToken);

        String account = fetchAccountFromRefreshToken(refreshToken);
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        List<String> roleNames = extractRoleNames(member);

        String newAccessToken = jwtProvider.createAccessToken(member.getAccount(), roleNames);
        jwtProvider.addAccessTokenToHeader(response, newAccessToken);

        String newRefreshToken = replaceRefreshToken(response, refreshToken, member.getAccount());

        return new TokenDto(newAccessToken, newRefreshToken);
    }
    
    //리프레시 토큰 검색
    private String fetchAccountFromRefreshToken(String refreshToken) throws RefreshTokenException {
        return redisTemplate.opsForValue().get(JwtProvider.REFRESH_TOKEN_PREFIX + refreshToken);
    }
    
    //역할 확인
    private List<String> extractRoleNames(Member member) {
        return member.getRoles().stream()
                .map(Authority::getName)
                .collect(Collectors.toList());
    }
    
    //리프레시 토큰 재발급
    private String replaceRefreshToken(HttpServletResponse response, String oldRefreshToken, String account) {
        String newRefreshToken = jwtProvider.createRefreshToken();
        redisTemplate.delete(JwtProvider.REFRESH_TOKEN_PREFIX + oldRefreshToken);
        jwtProvider.addCookieAndSaveTokenInRedis(response, newRefreshToken, account);
        return newRefreshToken;
    }
}