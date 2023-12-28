package com.USWRandomChat.backend.security;

import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.repository.MemberRepository;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByMemberId(username).orElseThrow(
                ()-> new UsernameNotFoundException("Invalid authentication")
        );

        return new CustomUserDetails(member);
    }
}
