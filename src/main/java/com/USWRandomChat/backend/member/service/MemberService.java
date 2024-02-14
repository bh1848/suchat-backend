package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.exception.CheckDuplicateNicknameException;
import com.USWRandomChat.backend.member.exception.NicknameChangeNotAllowedException;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.security.domain.Authority;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
import com.USWRandomChat.backend.security.jwt.domain.Token;
import com.USWRandomChat.backend.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final int NICKNAME_CHANGE_LIMIT_DAYS = 30;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final JwtRepository jwtRepository;

    // 회원가입
    public Member signUp(SignUpRequest request) {
        Member member = Member.builder()
                .account(request.getAccount())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        Profile profile = Profile.builder()
                .member(member)
                .nickname(request.getNickname())
                .nicknameChangeDate(LocalDateTime.now())
                .build();

        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);
        profileRepository.save(profile);

        // 이메일 인증
        Member savedMemberEmail = memberRepository.findByEmail(member.getEmail());

        return savedMemberEmail;
    }


    //로그인
    public SignInResponse signIn(SignInRequest request) {
        Member member = memberRepository.findByAccount(request.getAccount());
        if (member == null) {
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        // refreshToken 생성은 Member 엔티티에 설정하지 않고, JwtService에서 처리
        String refreshToken = jwtService.createRefreshToken(member);
        log.info("memberId: {}, pw: {} - 로그인 완료", request.getAccount(), request.getPassword());

        // SignInResponse 생성 시 refreshToken을 전달
        return new SignInResponse(member, jwtProvider, jwtService);
    }

    // 회원 탈퇴
    public void withdraw(String account) {
        Member member = memberRepository.findByAccount(account);

        if (member == null) {
            throw new AccountException(ExceptionType.BAD_CREDENTIALS);
        }

        // EMAIL_TOKEN 테이블과 관련된 데이터 삭제
        emailTokenRepository.deleteByMemberId(member.getId());

        // 저장된 Refresh Token을 찾아 삭제
        Optional<Token> refreshToken = jwtRepository.findById(member.getId());
        refreshToken.ifPresent(jwtRepository::delete);

        // 회원 삭제
        memberRepository.deleteById(member.getId());
        log.info("회원 탈퇴 완료: memberId={}", account);
    }

    //전체 조회
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // id 조회
    public Member findById(Long id) {
        Optional<Member> byId = memberRepository.findById(id);
        if (byId.isPresent()) {
            // 조회 성공
            return byId.get();
        } else {
            // 조회 실패
            return byId.orElse(null);
        }
    }

//    // 아이디 중복 확인
//    public boolean validateDuplicateAccount(MemberDTO memberDTO) {
//        Optional<Member> byAccount = memberRepository.findByAccount(memberDTO.getAccount());
//        // 중복
//        // 사용 가능한 ID
//        return byAccount.isPresent();
//    }

    // 닉네임 중복 확인, 닉네임 30일 제한 확인
    public void checkDuplicateNickname(String account, MemberDTO memberDTO) {

        // 닉네임 변경 제한 확인
        Member member = memberRepository.findByAccount(account);

        if(member == null){
            throw new AccountException(ExceptionType.BAD_CREDENTIALS);
        }

        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new RuntimeException("프로필을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeTime = Optional.ofNullable(profile.getNicknameChangeDate())
                .orElse(LocalDateTime.MIN);

        // 30일 이내에 변경한 경우 예외 발생
        if (ChronoUnit.DAYS.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_DAYS) {
            throw new NicknameChangeNotAllowedException("닉네임 변경 후 30일이 지나야 변경이 가능합니다.");
        }

        // 닉네임 중복 확인
        Optional<Profile> byNickname = profileRepository.findByNickname(memberDTO.getNickname());
        if (byNickname.isPresent()) {
            throw new CheckDuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }
    }

    // 해당 토큰 유저 삭제
    public void deleteFromId(Long id) {
        memberRepository.deleteById(id);
    }
}