package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 인증된 사용자 정보를 가져오는 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final MemberRepository memberRepository;

    public Member getAuthenticatedMember(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.error("인증되지 않은 사용자 접근 시도");
            throw new AccountException(ExceptionType.USER_NOT_AUTHENTICATION);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String account = userDetails.getUsername();

        return memberRepository.findByAccount(account)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 사용자: {}", account);
                    return new AccountException(ExceptionType.USER_NOT_EXISTS);
                });
    }
}