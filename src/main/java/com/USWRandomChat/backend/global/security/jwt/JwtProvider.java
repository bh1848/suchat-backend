package com.USWRandomChat.backend.global.security.jwt;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.service.JpaUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {
    private final JpaUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;
    public static final String COOKIE_NAME = "refreshToken";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600000L; //1시간
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1209600000L; //2주

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //엑세스 토큰을 헤더에 삽입
    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }

    //리프레시 토큰을 쿠키에 추가하고 Redis에 저장
    public void addCookieAndSaveTokenInRedis(HttpServletResponse response, String refreshToken, String account) {
        Cookie cookie = createCookie(refreshToken); //쿠키 유효시간 설정
        response.addCookie(cookie);
        redisTemplate.opsForValue().set("RT:" + refreshToken, account, REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }
    
    //권한 확인
    @Transactional(readOnly = true)
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getAccount(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    //계정 확인
    public String getAccount(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
    }

    //토큰 검증 공통 로직
    private void validateTokenNotEmpty(String token, ExceptionType exceptionType) throws TokenException {
        if (token == null || token.trim().isEmpty()) {
            log.error("{}가 비어 있습니다.", exceptionType == ExceptionType.ACCESS_TOKEN_REQUIRED ? "엑세스 토큰" : "리프레시 토큰");
            throw new TokenException(exceptionType);
        }
    }

    //엑세스 토큰 유효성 검사
    public boolean validateAccessToken(String accessToken) throws TokenException {
        validateTokenNotEmpty(accessToken, ExceptionType.INVALID_ACCESS_TOKEN);
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("엑세스 토큰 오류", e);
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }
    }

    //리프레시 토큰 유효성 검사
    public boolean validateRefreshToken(String refreshToken) throws TokenException {
        validateTokenNotEmpty(refreshToken, ExceptionType.INVALID_REFRESH_TOKEN);
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refreshToken));
    }

    //요청 쿠키에서 리프레시 토큰 추출
    public String resolveRefreshToken(HttpServletRequest request) {
        return getCookieValue(request);
    }

    //리프레시 토큰 쿠키 포함
    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie(JwtProvider.COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(1209600);
        return cookie;
    }

    //리프레시 토큰 쿠키 제거
    public void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}