package com.USWRandomChat.backend.security.jwt;

import com.USWRandomChat.backend.domain.Authority;
import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.repository.JwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtProvider {

    //access 토큰 만료시간 30분
    private final long ACCESS_EXPIRATION = 1000L * 60 * 30;

    //refresh 토큰 만료시간 2주
    private final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 14;

    private final JpaUserDetailsService userDetailsService;
    private final JwtRepository jwtRepository;
    @Value("${jwt.secret.key}")
    private String salt;
    private Key secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    //엑세스 토큰 생성
    public String createAccessToken(String memberId, List<Authority> roles) {
        Claims claims = Jwts.claims().setSubject(memberId);
        claims.put("role", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //리프레시 토큰 생성
    public Jwt createRefreshToken(Member member) {
        long currentMillis = System.currentTimeMillis();
        long refreshTokenExpiration = currentMillis + REFRESH_EXPIRATION;

        return jwtRepository.save(
                Jwt.builder()
                        .id(member.getId())
                        .refreshToken(UUID.randomUUID().toString())
                        .expiration(REFRESH_EXPIRATION)
                        .refreshTokenExpiration(refreshTokenExpiration)
                        .build()
        );
    }

    //권한정보 획득
    //Spring Security 인증과정에서 권한확인을 위한 기능
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getMemberId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getMemberId(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    //Authorization Header를 통해 인증을 한다.
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    //엑세스 토큰검증
    public boolean validateAccessToken(String token) {
        try {
            //Bearer 검증
            if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
                return false;
            } else {
                token = token.split(" ")[1].trim();
            }
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            //만료되었을 시 false
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    //리프레시 토큰 유효성 검증 및 갱신
    public Jwt validateRefreshToken(Member member, String refreshToken) throws Exception {
        Jwt token = jwtRepository.findById(member.getId()).orElseThrow(() -> new Exception("만료된 계정입니다. 로그인을 다시 시도하세요"));

        // 토큰이 존재하지 않거나 만료된 경우
        if (token == null || isRefreshTokenExpired(token) || !token.getRefreshToken().equals(refreshToken)) {
            if (token != null) {
                // 해당 조건에 해당하는 경우에만 토큰을 null로 설정
                token.setRefreshToken(null);
                jwtRepository.save(token);
            }
            return null;
        }

        return token;
    }

    private boolean isRefreshTokenExpired(Jwt token) {
        return token.getRefreshTokenExpiration() != null && token.getRefreshTokenExpiration() < System.currentTimeMillis();
    }
}
