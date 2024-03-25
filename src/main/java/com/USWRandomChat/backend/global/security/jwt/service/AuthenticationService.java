package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtProvider jwtProvider;
    private final JpaUserDetailsService userDetailsService;

    /**
     * 요청으로부터 액세스 토큰을 추출하고 검증한 후,
     * 해당 사용자의 Authentication 객체를 반환합니다.
     *
     **/
    public Authentication authenticate(HttpServletRequest request) {
        String accessToken = jwtProvider.resolveAccessToken(request);
        if (accessToken == null || !jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        String username = jwtProvider.getAccount(accessToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }
    public UserDetails getUserDetails(HttpServletRequest request) {
        Authentication authentication = authenticate(request);
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new AccountException(ExceptionType.USER_NOT_AUTHENTICATION);
        }
        return (UserDetails) authentication.getPrincipal();
    }
}