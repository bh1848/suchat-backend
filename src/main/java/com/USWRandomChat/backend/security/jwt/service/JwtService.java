package com.USWRandomChat.backend.security.jwt.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.domain.Token;
import com.USWRandomChat.backend.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.security.jwt.repository.JwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final JwtRepository jwtRepository;

    // 만료시간 2주
    private final int REFRESH_TOKEN_EXPIRATION = 14 * 24 * 60;

    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:memberId : tokenValue
     * 형태로 저장한다.
     */
    public String createRefreshToken(Member member) {
        Token token = jwtRepository.save(
                Token.builder()
                        .id(member.getId().toString())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(REFRESH_TOKEN_EXPIRATION)
                        .build()
        );
        return token.getRefresh_token();
    }

    public Token validRefreshToken(Member member, String refreshToken) throws Exception {
        Token token = jwtRepository.findById(member.getId()).orElse(null);

        // 해당 유저의 Refresh 토큰이 없거나 토큰이 만료된 경우
        if (token == null || token.getExpiration() <= System.currentTimeMillis()) {
            return null;
        }

        // 토큰이 같은지 비교
        if (!token.getRefresh_token().equals(refreshToken)) {
            return null;
        } else {
            return token;
        }
    }

    // 자동로그인
    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        String memberId = jwtProvider.getMemberId(token.getAccess_token());
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BadCredentialsException("잘못된 계정정보입니다."));

        Token refreshToken = validRefreshToken(member, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(memberId, member.getRoles()))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }

    // 로그아웃
    public void signOut(String memberId) throws Exception {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BadCredentialsException("잘못된 계정정보입니다."));

        Token savedRefreshToken = jwtRepository.findById(member.getId()).orElse(null);

        // 저장된 Refresh Token이 있으면 삭제
        if (savedRefreshToken != null) {
            jwtRepository.delete(savedRefreshToken);
            log.info("로그아웃 성공: memberId={}", memberId);
        } else {
            throw new Exception("로그아웃 실패: 토큰이 없습니다.");
        }
    }
}