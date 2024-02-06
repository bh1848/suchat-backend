package com.USWRandomChat.backend.member.service;

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
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        //중복
        //사용 가능한 ID
        return byMemberId.isPresent();
    }

    //중복 검증 nickname
    public boolean validateDuplicateMemberNickname(MemberDTO memberDTO) {
        Optional<Member> byNickname = memberRepository.findByNickname(memberDTO.getNickname());
        //중복
        //사용 가능한 ID
        return byNickname.isPresent();
    }

    //해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberRepository.deleteById(id);
    }

    //비밀번호 변경
    public boolean changePassword(String token, String newPassword) {
        try {
            // 토큰에서 memberId 추출
            String memberId = jwtProvider.getMemberId(token);

            // memberId로 사용자 조회
            Member member = memberRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("해당하는 아이디가 존재하지 않습니다. " + memberId));

            // 새로운 비밀번호를 암호화
            String encryptedPassword = passwordEncoder.encode(newPassword);

            // 암호화된 비밀번호로 변경
            member.setPassword(encryptedPassword);

            // 변경된 비밀번호를 데이터베이스에 저장
            memberRepository.save(member);

            log.info("비밀번호 변경 완료: memberId={}, newPassword={}", memberId, newPassword);

            // 변경 성공한 경우 true 반환
            return true;
        } catch (Exception e) {
            // 변경 실패한 경우 false 반환
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return false;
        }
    }
}