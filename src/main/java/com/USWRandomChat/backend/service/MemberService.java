package com.USWRandomChat.backend.service;

import com.USWRandomChat.backend.domain.Authority;
import com.USWRandomChat.backend.domain.Member;
import com.USWRandomChat.backend.memberDTO.MemberDTO;
import com.USWRandomChat.backend.memberDTO.SignRequest;
import com.USWRandomChat.backend.memberDTO.SignResponse;
import com.USWRandomChat.backend.repository.MemberRepository;
import com.USWRandomChat.backend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    //쓰기 발생 시 @Transactional 붙이기
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    //회원가입
    @Transactional
    public boolean signUp(SignRequest request) throws Exception {
        try {
            Member member = Member.builder()
                    .memberId(request.getMemberId())
                    //password 암호화
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .build();

            member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("잘못된 요청입니다.{}", e.getMessage());
        }
        return true;
    }

    //로그인
    public SignResponse signIn(SignRequest request) throws Exception {
        //ID 비교
        Member member = memberRepository.findByMemberId(request.getMemberId()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정 정보입니다."));

        //password 비교
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("잘못된 계정 정보입니다.");
        }

        return SignResponse.builder()
                .id(member.getId())
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .roles(member.getRoles())
                .token(jwtProvider.createToken(member.getMemberId(), member.getRoles()))
                .build();
    }

    public SignResponse getMember(String memberId) throws Exception {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new Exception("계정을 찾을 수 없습니다.")) ;
        return new SignResponse(member);
    }

    //전체 조회
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    //id 조회
    public Member findById(Long id) {
        Optional<Member> byId = memberRepository.findById(id);
        if (byId.isPresent()) {
            //조회 성공
            return byId.get();
        } else {
            //조회 실패
            return byId.orElse(null);
        }
    }

    //중복 검증 memberId
    public boolean validateDuplicateMemberId(MemberDTO memberDTO) {
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }

    //중복 검증 nickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        if (byNickname.isPresent()) {
            //중복
            return true;
        } else {
            //사용가능한 ID
            return false;
        }
    }
}
