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

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtProvider {

    private final JpaUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;

    public static final String COOKIE_NAME = "refreshToken";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600000L; //1시간
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1209600000L; //2주

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    //엑세스 토큰 발급
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

    //리프레시 토큰 발급
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //리프레시 토큰을 쿠키에 추가하고 Redis에 저장하는 로직
    public void addCookieAndSaveTokenInRedis(HttpServletResponse response, String refreshToken, String account) {
        Cookie cookie = createCookie(refreshToken); //쿠키 유효시간 설정
        response.addCookie(cookie);
        redisTemplate.opsForValue().set("RT:" + refreshToken, account, REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
    }


    //엑세스 토큰을 HTTP 응답 헤더에 추가하는 로직
    public void addAccessTokenToHeader(HttpServletResponse response, String accessToken) {
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }

    //권한 확인
    @Transactional(readOnly = true) //LAZY 에러 해결하기 위해 필요
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getAccount(accessToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //계정 추출
    public String getAccount(String accessToken) throws TokenException {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            log.error("엑세스 토큰 만료", e);
            throw new TokenException(ExceptionType.ACCESS_TOKEN_REQUIRED);
        } catch (Exception e) {
            log.error("엑세스 토큰 오류", e);
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }
    }

    //헤더 해석
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //엑세스 토큰 해석
    public boolean validateAccessToken(String accessToken) throws TokenException {
        // 엑세스 토큰이 null이거나 공백인 경우에 대한 검증 추가
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("엑세스 토큰이 비어 있습니다.");
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

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
        //리프레시 토큰이 null이거나 공백인 경우 검증
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.error("리프레시 토큰이 null이거나 비어 있습니다.");
            throw new TokenException(ExceptionType.INVALID_REFRESH_TOKEN);
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey("RT:" + refreshToken));
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