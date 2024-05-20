package com.USWRandomChat.backend.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
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
        String accessToken = jwtProvider.resolveAccessToken(request);
        if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    //검증 필요 없는 API
    private boolean isSkippedPath(HttpServletRequest request) {
        // 검증하지 않을 경로 패턴들
        String[] skipPaths = {
                "/open/**",
                "/stomp/**",
                "/pub/**",
                "/sub/**",
                "/queue/match"
        };

        for (String path : skipPaths) {
            if (pathMatcher.match(path, request.getRequestURI())) {
                return true;
            }
        }
        return false;
    }
}