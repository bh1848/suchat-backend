package com.USWRandomChat.backend.security.jwt.service;

import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        return new CustomUserDetails(member);
    }
}