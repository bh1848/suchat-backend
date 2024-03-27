package com.USWRandomChat.backend.global.security.jwt.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 엑세스 토큰에서 회원 정보를 가져오는 클래스
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberRepository memberRepository;

    public Member getAuthenticatedMember(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //인증여부 확인
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new AccountException(ExceptionType.USER_NOT_AUTHENTICATION);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String account = userDetails.getUsername();
        
        //사용자 정보 조회
        return memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
    }
}