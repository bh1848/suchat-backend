package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private static final int NICKNAME_CHANGE_LIMIT_DAYS = 30;

    private final MemberRepository memberRepository;
    private final MemberTempRepository memberTempRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final ProfileRepository profileRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final JwtRepository jwtRepository;



//    //회원 탈퇴
//    public void withdraw(String accessToken) {
//        //엑세스 토큰의 유효성 검사
//        if (!jwtProvider.validateAccessToken(accessToken)) {
//            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
//        }
//
//        //토큰이 유효한 경우, 계정 정보를 추출
//        String account = jwtProvider.getAccount(accessToken);
//
//        Member member = memberRepository.findByAccount(account)
//                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
//
//        //저장된 Refresh Token을 찾아 삭제
//        Optional<Token> refreshToken = jwtRepository.findById(member.getId());
//        refreshToken.ifPresent(jwtRepository::delete);
//
//        //회원 삭제
//        memberRepository.deleteById(member.getId());
//        log.info("회원 탈퇴 완료: memberId={}", account);
//    }
//
//    //전체 조회
//    @Transactional(readOnly = true)
//    public List<Member> findAll() {
//        return memberRepository.findAll();
//    }
//
//    //계정 중복 확인
//    @Transactional(readOnly = true)
//    public void checkDuplicateAccount(MemberDTO memberDTO) {
//        Optional<Member> byAccount = memberRepository.findByAccount(memberDTO.getAccount());
//        if (byAccount.isPresent()) {
//            throw new AccountException(ExceptionType.ACCOUNT_OVERLAP);
//        }
//    }
//
//    //이메일 중복 확인
//    @Transactional(readOnly = true)
//    public void checkDuplicateEmail(MemberDTO memberDTO) {
//        Member member = memberRepository.findByEmail(memberDTO.getEmail());
//
//        if (member != null) {
//            throw new AccountException(ExceptionType.EMAIL_OVERLAP);
//        }
//    }
//
//    //회원가입 시의 닉네임 중복 확인
//    @Transactional(readOnly = true)
//    public void checkDuplicateNicknameSignUp(MemberDTO memberDTO) {
//        profileRepository.findByNickname(memberDTO.getNickname())
//                .ifPresent(profile -> {
//                    throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
//                });
//    }
//
//    //이미 가입된 사용자의 닉네임 중복 확인, 닉네임 30일 제한 확인
//    @Transactional(readOnly = true)
//    public void checkDuplicateNickname(String accessToken, MemberDTO memberDTO) {
//        //엑세스 토큰의 유효성 검사
//        if (!jwtProvider.validateAccessToken(accessToken)) {
//            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
//        }
//
//        //토큰이 유효한 경우, 계정 정보를 추출
//        String account = jwtProvider.getAccount(accessToken);
//
//        Member member = memberRepository.findByAccount(account)
//                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
//
//        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
//                new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime lastChangeTime = Optional.ofNullable(profile.getNicknameChangeDate())
//                .orElse(LocalDateTime.MIN);
//
//        //30일 이내에 변경한 경우 예외 발생
//        if (ChronoUnit.DAYS.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_DAYS) {
//            throw new AccountException(ExceptionType.NICKNAME_EXPIRATION_TIME);
//        }
//
//        //닉네임 중복 확인 (현재 사용자의 닉네임 제외)
//        profileRepository.findByNickname(memberDTO.getNickname())
//                .ifPresent(existingProfile -> {
//                    if (!existingProfile.getMember().getId().equals(member.getId())) {
//                        throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
//                    }
//                });
//    }
}