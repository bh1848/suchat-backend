package com.USWRandomChat.backend.global.security.jwt;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccessTokenException;
import com.USWRandomChat.backend.global.exception.errortype.RefreshTokenException;
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
    public static final String COOKIE_NAME = "refreshToken";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600000L; //1시간
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1209600000L; //2주
    private final JpaUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
    
    //엑세스 토큰 생성
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
    
    //리프레시 토큰 생성
    public String createRefreshToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    //엑세스 토큰 헤더 추가
    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }
    
    //리프레시 토큰 쿠키 및 레디스 저장
    public void addCookieAndSaveTokenInRedis(HttpServletResponse response, String refreshToken, String account) {
        Cookie cookie = createCookie(refreshToken);
        response.addCookie(cookie);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + refreshToken, account, REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }
    
    //엑세스 토큰에서 계정 추출
    public String getAccount(String accessToken) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
    }
    
    //권한 확인
    @Transactional(readOnly = true)
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getAccount(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    //엑세스 토큰 검증
    public boolean validateAccessToken(String accessToken) throws AccessTokenException {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("엑세스 토큰 오류", e);
            throw new AccessTokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }
    }
    
    //리프레시 토큰 검증
    public boolean validateRefreshToken(String refreshToken) throws RefreshTokenException {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RefreshTokenException(ExceptionType.REFRESH_TOKEN_EXPIRED);
        }
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(refreshToken);
            Date expirationDate = claims.getBody().getExpiration();
            if (expirationDate.before(new Date())) {
                throw new RefreshTokenException(ExceptionType.REFRESH_TOKEN_EXPIRED);
            }
            String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            if (!Boolean.TRUE.equals(hasKey)) {
                throw new RefreshTokenException(ExceptionType.INVALID_REFRESH_TOKEN);
            }
            return true;
        } catch (JwtException e) {
            log.error("리프레시 토큰 오류", e);
            throw new RefreshTokenException(ExceptionType.INVALID_REFRESH_TOKEN);
        }
    }
    
    //헤더에서 엑세스 토큰 추출
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    //쿠키에서 리프레시 토큰 추출
    public String resolveRefreshToken(HttpServletRequest request) {
        return getCookieValue(request);
    }
    
    //쿠키 생성
    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        int maxAge = (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
    
    //쿠키 삭제
    public void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    
    //요청에 쿠키 포함
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