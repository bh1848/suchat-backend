package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.MemberDTO;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignInResponse;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.domain.Token;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
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

        Profile profile = Profile.builder()
                .member(member)
                .nickname(memberTemp.getNickname())
                .nicknameChangeDate(memberTemp.getNicknameChangeDate())
                .build();

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
    public SignInResponse signIn(SignInRequest request) {

        Member member = memberRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        //refreshToken 생성은 Member 엔티티에 설정하지 않고, JwtService에서 처리
        jwtService.createRefreshToken(member);
        log.info("memberId: {}, pw: {} - 로그인 완료", request.getAccount(), request.getPassword());

        //SignInResponse 생성 시 refreshToken을 전달
        return new SignInResponse(member, jwtProvider, jwtService);
    }

    //회원 탈퇴
    public void withdraw(String accessToken) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        //저장된 Refresh Token을 찾아 삭제
        Optional<Token> refreshToken = jwtRepository.findById(member.getId());
        refreshToken.ifPresent(jwtRepository::delete);

        //회원 삭제
        memberRepository.deleteById(member.getId());
        log.info("회원 탈퇴 완료: memberId={}", account);
    }

    //전체 조회
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public void validateDuplicateAccount(MemberDTO memberDTO) {
        Optional<Member> byAccount = memberRepository.findByAccount(memberDTO.getAccount());
        if (byAccount.isPresent()) {
            throw new AccountException(ExceptionType.ID_OVERLAP);
        }
}

    //이메일 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateEmail(MemberDTO memberDTO) {
        Member member = memberRepository.findByEmail(memberDTO.getEmail());

        if (member != null) {
            throw new AccountException(ExceptionType.EMAIL_OVERLAP);
        }
    }

    //회원가입 시의 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNicknameSignUp(MemberDTO memberDTO) {
        profileRepository.findByNickname(memberDTO.getNickname())
                .ifPresent(profile -> {
                    throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                });
    }

    //이미 가입된 사용자의 닉네임 중복 확인, 닉네임 30일 제한 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNickname(String accessToken, MemberDTO memberDTO) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        Profile profile = profileRepository.findById(member.getId()).orElseThrow(() ->
                new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastChangeTime = Optional.ofNullable(profile.getNicknameChangeDate())
                .orElse(LocalDateTime.MIN);

        //30일 이내에 변경한 경우 예외 발생
        if (ChronoUnit.DAYS.between(lastChangeTime, now) < NICKNAME_CHANGE_LIMIT_DAYS) {
            throw new AccountException(ExceptionType.NICKNAME_EXPIRATION_TIME);
        }

        //닉네임 중복 확인 (현재 사용자의 닉네임 제외)
        profileRepository.findByNickname(memberDTO.getNickname())
                .ifPresent(existingProfile -> {
                    if (!existingProfile.getMember().getId().equals(member.getId())) {
                        throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                    }
                });
    }

}