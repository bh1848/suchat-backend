package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.CheckDuplicateNicknameException;
import com.USWRandomChat.backend.member.exception.NicknameChangeNotAllowedException;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    private static final int NICKNAME_CHANGE_LIMIT_DAYS = 30;

    //회원가입
    public Member signUp(SignUpRequest request) {
        Member member = Member.builder().memberId(request.getMemberId())
                //password 암호화
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .nicknameSetDate(LocalDateTime.now())
                .build();

        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);

        //이메일 인증
        Member savedMemberEmail = memberRepository.findByEmail(member.getEmail());

        return savedMemberEmail;
    }

    //로그인
    public SignInResponse signIn(SignInRequest request) throws Exception {
        Member member = memberRepository.findByMemberId(request.getMemberId()).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정정보입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("잘못된 계정정보입니다.");
        }

        member.setRefreshToken(jwtService.createRefreshToken(member));

        log.info("memberId: {}, pw: {} - 로그인 완료", request.getMemberId(), request.getPassword());
        return new SignInResponse(member, jwtProvider);
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

    //아이디 중복 확인
    public boolean validateDuplicateMemberId(MemberDTO memberDTO) {
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        //중복
        //사용 가능한 ID
        return byMemberId.isPresent();
    }

    // 닉네임 중복 확인, 닉네임 30일 제한 확인
    public void checkDuplicateNickname(String memberId, MemberDTO memberDTO) {

        // 닉네임 변경 제한 확인
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() ->
                new RuntimeException("회원을 찾을 수 없습니다."));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeTime = Optional.ofNullable(member.getNicknameSetDate())
                .orElse(LocalDateTime.MIN);

        // 30일 이내에 변경한 경우 예외 발생
        if (ChronoUnit.DAYS.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_DAYS) {
            throw new NicknameChangeNotAllowedException("닉네임 변경 후 30일이 지나야 변경이 가능합니다.");
        }

        // 닉네임 중복 확인
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        if (byNickname.isPresent()) {
            throw new CheckDuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }
    }

    //해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberRepository.deleteById(id);
    }
}