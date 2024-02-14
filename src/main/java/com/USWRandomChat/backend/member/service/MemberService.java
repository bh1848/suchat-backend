package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.USWRandomChat.backend.exception.ExceptionType.LOGIN_ID_OVERLAP;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    //회원가입
    public Member signUp(SignUpRequest request) {
        Member member = Member.builder().memberId(request.getMemberId())
                //password 암호화
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);

        //이메일 인증
        Member savedMemberEmail = memberRepository.findByEmail(member.getEmail());

        return savedMemberEmail;
    }

    //로그인
    public SignInResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByMemberId(request.getMemberId());
        if (member == null) {
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        member.setRefreshToken(jwtService.createRefreshToken(member));

        log.info("memberId: {}, pw: {} - 로그인 완료", request.getMemberId(), request.getPassword());
        return new SignInResponse(member, jwtProvider);
    }


    //user 인증
//    public SignInResponse getMember(String memberId) throws Exception {
//        Member member = memberRepository.findByMemberId(memberId)
//                .orElseThrow(() -> new Exception("계정을 찾을 수 없습니다."));
//        Jwt refreshToken = jwtRepository.findRefreshTokenByID(member.getId()).orElse(null);
//        return new SignInResponse(member,jwtProvider, ref);
//    }

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
        Member byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId != null) {
            throw new AccountException(ExceptionType.LOGIN_ID_OVERLAP);
        } else {
            return false;
        }
    }

    //중복 검증 nickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        if (byNickname.isPresent()){
            throw new AccountException(ExceptionType.LOGIN_NICKNAME_OVERLAP);
        } else {
            return false;
        }
    }

    //해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberRepository.deleteById(id);
    }
}