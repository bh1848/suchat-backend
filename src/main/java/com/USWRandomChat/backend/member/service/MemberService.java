package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.security.repository.JwtRepository;
import com.USWRandomChat.backend.security.jwt.Jwt;
import com.USWRandomChat.backend.security.jwt.JwtDto;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
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
    private final JwtRepository jwtRepository;

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

        //로그인 시 리프레시 토큰 생성
        Jwt refreshToken = jwtProvider.createRefreshToken(member);

        //리프레시 토큰 테이블에 저장
        jwtRepository.save(refreshToken);

        log.info("memberId: {}, pw: {} - 로그인 완료", request.getMemberId(),request.getPassword());
        return new SignInResponse(member, jwtProvider, refreshToken);
    }

    //자동 로그인
    public JwtDto refreshAccessToken(JwtDto token) throws Exception {
        String memberId = jwtProvider.getMemberId(token.getAccess_token());
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(() ->
                new BadCredentialsException("잘못된 계정 정보입니다."));

        Jwt refreshToken = jwtProvider.validateRefreshToken(member, token.getRefresh_token());

        if (refreshToken != null) {
            //리프레시 토큰이 유효하면 기존 리프레시 토큰으로 갱신
            return JwtDto.builder()
                    .access_token(jwtProvider.createAccessToken(memberId, member.getRoles()))
                    .refresh_token(token.getRefresh_token()) //기존 리프레시 토큰 반환
                    .build();
        } else {
            //리프레시 토큰이 만료되면 예외
            throw new BadCredentialsException("리프레시 토큰 만료. 로그인을 해주세요");
        }
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
}