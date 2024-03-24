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

    //임시 회원가입
    public MemberTemp signUpMemberTemp(SignUpRequest request) {
        MemberTemp tempMember = MemberTemp.builder()
                .account(request.getAccount())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .nicknameChangeDate(LocalDateTime.now())
                //회원가입 정보 주입으로 인증여부 받음
                .isEmailVerified(request.getIsEmailVerified())
                .build();

        memberTempRepository.save(tempMember);
        log.info("임시 회원가입 완료: {}", tempMember.getAccount());
        //이메일 인증
        MemberTemp tempMemberEmail = memberTempRepository.findByEmail(tempMember.getEmail());

        return tempMemberEmail;
    }

    //인증 후 회원가입
    public void signUpMember(MemberTemp memberTemp) {
        Member member = Member.builder()
                .account(memberTemp.getAccount())
                .password(memberTemp.getPassword())
                .email(memberTemp.getEmail())
                .build();

        // Profile 객체 생성 및 저장
        Profile profile = Profile.builder()
                .member(member)
                .nickname(memberTemp.getNickname())
                .nicknameChangeDate(memberTemp.getNicknameChangeDate())
                .build();

        // 권한 설정 및 Member 저장
        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);
        profileRepository.save(profile);
        log.info("회원가입 완료: {}", member.getAccount());

        //임시 회원 테이블 삭제
        log.info("임시 회원 삭제 완료: {}", member.getAccount());
        memberTempRepository.delete(memberTemp);
    }

    //회원가입 할 때 이메일 인증 유무 확인
    @Transactional(readOnly = true)
    public Boolean signUpFinish(String uuid) {

        Optional<EmailToken> findEmailToken = emailTokenRepository.findByUuid(uuid);
        EmailToken emailToken = findEmailToken.orElseThrow(() -> new AccountException(ExceptionType.Email_Token_Not_Found));

        MemberTemp memberTemp = emailToken.getMemberTemp();

        if (!memberTemp.isEmailVerified()) {
            throw new AccountException(ExceptionType.EMAIL_NOT_VERIFIED);
        }
        return true;
    }

    //로그인
    public TokenDto signIn(SignInRequest request, HttpServletResponse response) throws AccountException {
        // 계정으로 멤버 조회
        Member member = memberRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        // 비밀번호 일치 여부 검사
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        // 멤버의 권한 정보 추출
        List<String> roleNames = member.getRoles().stream()
                .map(Authority::getName)
                .collect(Collectors.toList());

        // 엑세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtProvider.createAccessToken(member.getAccount(), roleNames);
        String refreshToken = jwtProvider.createRefreshToken();

        // 엑세스 토큰을 HTTP 응답 헤더에 추가
        jwtProvider.addAccessTokenToHeader(response, accessToken);

        // 리프레시 토큰을 쿠키에 추가하고 Redis에 저장
        jwtProvider.addCookieAndSaveTokenInRedis(response, refreshToken, member.getAccount());

        log.info("로그인 성공: {}", member.getAccount());
        return new TokenDto(accessToken, refreshToken);
    }

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