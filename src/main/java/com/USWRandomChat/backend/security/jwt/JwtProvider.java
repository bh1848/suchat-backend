package com.USWRandomChat.backend.security.jwt;

import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.security.jwt.service.JpaUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtProvider {

    public static final String BEARER_PREFIX = "Bearer ";
    //30분 제한 설정
    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 30;
    private final JpaUserDetailsService userDetailsService;
    @Value("${jwt.secret.key}")
    private String salt;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String createAccessToken(String account, List<Authority> roles) {
        Claims claims = Jwts.claims().setSubject(account);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 권한정보 획득
    // Spring Security 인증과정에서 권한확인을 위한 기능
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getAccount(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에 담겨있는 memberId 획득
    public String getAccount(String accessToken) {
        // 만료된 토큰에 대해 parseClaimsJws를 수행하면 io.jsonwebtoken.ExpiredJwtException이 발생한다.
        try {

            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
        } catch (JwtException e) { //모든 엑세스 토큰 관련 예외 확인
            log.error("엑세스 토큰 처리 실패: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 엑세스 토큰");
        }
    }


    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            if (accessToken == null) {
                return false;
            } else {
                accessToken = accessToken.split(" ")[1].trim();
            }

            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException e) { //모든 엑세스 토큰 관련 예외 확인
            log.error("엑세스 토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }
}