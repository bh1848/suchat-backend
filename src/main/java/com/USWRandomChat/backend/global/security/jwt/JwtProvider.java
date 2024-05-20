package com.USWRandomChat.backend.global.security.jwt;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccessTokenException;
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
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600000L; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1209600000L; // 2주

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

    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }

    public void addCookieAndSaveTokenInRedis(HttpServletResponse response, String refreshToken, String account) {
        Cookie cookie = createCookie(refreshToken);
        response.addCookie(cookie);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + refreshToken, account, REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }

    @Transactional(readOnly = true)
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getAccount(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getAccount(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
    }

    private void validateTokenNotEmpty(String token, ExceptionType exceptionType) throws AccessTokenException {
        if (token == null || token.trim().isEmpty()) {
            log.error("{}가 비어 있습니다.", exceptionType == ExceptionType.ACCESS_TOKEN_EXPIRED ? "엑세스 토큰" : "리프레시 토큰");
            throw new AccessTokenException(exceptionType);
        }
    }

    //엑세스 토큰 유효성 검사
    public boolean validateAccessToken(String accessToken) throws AccessTokenException {
        validateTokenNotEmpty(accessToken, ExceptionType.INVALID_ACCESS_TOKEN);
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("엑세스 토큰 오류", e);
            throw new AccessTokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }
    }

    public void validateRefreshToken(String refreshToken) {
        validateTokenNotEmpty(refreshToken, ExceptionType.INVALID_REFRESH_TOKEN);
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(refreshToken);
            if (!claims.getBody().getExpiration().before(new Date())) {
                redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refreshToken);
            }
        } catch (JwtException e) {
            log.error("리프레시 토큰 오류", e);
        }
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return getCookieValue(request);
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000));
        return cookie;
    }

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