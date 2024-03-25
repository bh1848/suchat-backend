package com.USWRandomChat.backend.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Jwt가 유효성을 검증하는 Filter
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isSkippedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = resolveAccessToken(request);
        if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("엑세스 토큰 검증 오류");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    //토큰 필요 없는 api
    private boolean isSkippedPath(HttpServletRequest request) {
        return pathMatcher.match("/auth/**", request.getRequestURI());
    }
    
    //엑세스 토큰 해석
    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtProvider.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtProvider.BEARER_PREFIX)) {
            return bearerToken.substring(JwtProvider.BEARER_PREFIX.length());
        }
        return null;
    }
}