package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.domain.Token;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenResponse;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    //리프레시 토큰 2주로 설정
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 14;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final JwtRepository jwtRepository;


    //리프레시 토큰 생성
    public String createRefreshToken(Member member) {
        long expiryTimeInMillis = Duration.ofDays(REFRESH_TOKEN_EXPIRY_DAYS).toMillis();
        Token token = jwtRepository.save(
                Token.builder()
                        .account(member.getAccount())
                        .refreshToken(UUID.randomUUID().toString())
                        .expiration(expiryTimeInMillis)
                        .build()
        );
        return token.getRefreshToken();
    }

    //자동로그인
    public TokenResponse refreshAccessToken(String accessToken) throws AccountException, TokenException {
        // 엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            // 엑세스 토큰이 만료되었다면, 리프레시 토큰으로 계정 정보를 추출합니다.
            String account = jwtProvider.getAccount(accessToken);

            // 해당 account의 회원 정보를 조회합니다.
            Member member = memberRepository.findByAccount(account)
                    .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

            // 해당 account의 리프레시 토큰 정보를 조회합니다.
            Token refreshToken = jwtRepository.findByAccount(account)
                    .orElseThrow(() -> new TokenException(ExceptionType.INVALID_REFRESH_TOKEN));

            // 리프레시 토큰의 만료 시간을 검증합니다.
            if (refreshToken.getExpiration() <= System.currentTimeMillis()) {
                throw new TokenException(ExceptionType.REFRESH_TOKEN_EXPIRED);
            }

            // 유효한 리프레시 토큰에 대해 새 엑세스 토큰을 발급합니다.
            String newAccessToken = jwtProvider.createAccessToken(account, member.getRoles());

            // 새 엑세스 토큰과 기존 리프레시 토큰을 반환합니다.
            return new TokenResponse(newAccessToken, refreshToken.getRefreshToken());
        } else {
            // 엑세스 토큰이 여전히 유효한 경우, 기존 토큰을 그대로 반환합니다.
            return new TokenResponse(accessToken, null);
        }
    }

    //로그아웃
    public void signOut(String accessToken) throws Exception {

        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            //토큰이 유효하지 않은 경우, 예외를 발생시킵니다.
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출합니다.
        String account = jwtProvider.getAccount(accessToken);

        //해당 account의 리프레시 토큰 조회 및 삭제
        jwtRepository.findByAccount(account).ifPresent(token -> {
            jwtRepository.delete(token); //리프레시 토큰 삭제
            log.info("로그아웃 성공: account={}", account);
        });
    }
}