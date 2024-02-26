package com.USWRandomChat.backend.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.domain.Token;
import com.USWRandomChat.backend.security.jwt.repository.JwtRepository;
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
    public String refreshAccessToken(String accessToken) throws AccountException, TokenException{
        //엑세스 토큰의 유효성 검사
        if (jwtProvider.validateAccessToken(accessToken)) {
            return accessToken; //유효한 토큰이면 재사용
        }

        String account = jwtProvider.getAccount(accessToken); //유효하지 않으면 계정 정보 추출

        //account로 회원 조회
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        //해당 account의 리프레시 토큰 조회
        Token refreshToken = jwtRepository.findByAccount(account)
                .orElseThrow(() -> new TokenException(ExceptionType.REFRESH_TOKEN_EXPIRED));

        //리프레시 토큰 검증
        if (refreshToken.getExpiration() <= System.currentTimeMillis()) {
            throw new TokenException(ExceptionType.INVALID_REFRESH_TOKEN);
        }

        //새 엑세스 토큰 발급
        return jwtProvider.createAccessToken(account, member.getRoles());
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