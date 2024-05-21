package com.USWRandomChat.backend.member.open.service;

import com.USWRandomChat.backend.email.domain.EmailToken;
import com.USWRandomChat.backend.email.repository.EmailTokenRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.ProfileException;
import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.dto.MemberDto;
import com.USWRandomChat.backend.member.dto.SignInRequest;
import com.USWRandomChat.backend.member.dto.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
public class MemberOpenService {
    private final MemberRepository memberRepository;
    private final MemberTempRepository memberTempRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    //임시 회원가입
    public MemberTemp signUpMemberTemp(SignUpRequest request) {
        //회원가입 정보 주입
        MemberTemp tempMember = MemberTemp.builder()
                .account(request.getAccount())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .nicknameChangeDate(LocalDateTime.now())
                .isEmailVerified(request.getIsEmailVerified())
                .build();

        //임시 회원 데이터 저장
        memberTempRepository.save(tempMember);
        log.info("임시 회원가입 완료: {}", tempMember.getAccount());

        //이메일 인증
        return memberTempRepository.findByEmail(tempMember.getEmail());
    }

    //인증 후 회원가입
    @Transactional //전체 메서드를 하나의 트랜잭션으로 관리
    public void signUpMember(MemberTemp memberTemp) {
        //회원 정보 주입
        Member member = Member.builder()
                .account(memberTemp.getAccount())
                .password(memberTemp.getPassword())
                .email(memberTemp.getEmail())
                .build();

        //Profile 객체 생성 및 저장
        Profile profile = Profile.builder()
                .member(member)
                .nickname(memberTemp.getNickname())
                .nicknameChangeDate(memberTemp.getNicknameChangeDate())
                .build();

        //권한 설정 및 Member 저장
        member.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
        memberRepository.save(member);
        profileRepository.save(profile);
        log.info("회원가입 완료: {}", member.getAccount());

        //임시 회원 데이터 삭제
        memberTempRepository.delete(memberTemp);
        log.info("임시 회원 삭제 완료: {}", member.getAccount());
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
        //계정으로 멤버 조회
        Member member = memberRepository.findByAccount(request.getAccount())
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        //비밀번호 일치 여부 검사
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        //멤버의 권한 정보 추출
        List<String> roleNames = member.getRoles().stream()
                .map(Authority::getName)
                .collect(Collectors.toList());

        //엑세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtProvider.createAccessToken(member.getAccount(), roleNames);
        String refreshToken = jwtProvider.createRefreshToken();

        //기존 리프레시 토큰 삭제
        deleteExistingRefreshToken(member.getAccount());

        //엑세스 토큰을 HTTP 응답 헤더에 추가
        jwtProvider.addAccessTokenToHeader(response, accessToken);

        //리프레시 토큰을 쿠키에 추가하고 Redis에 저장
        jwtProvider.addCookieAndSaveTokenInRedis(response, refreshToken, member.getAccount());

        log.info("로그인 성공: {}", member.getAccount());
        return new TokenDto(accessToken, refreshToken);
    }

    //기존 리프레시 토큰 삭제 로직
    private void deleteExistingRefreshToken(String account) {
        //기존 리프레시 토큰을 Redis에서 삭제하고 쿠키에서도 삭제
        String oldRefreshToken = redisTemplate.opsForValue().get(JwtProvider.REFRESH_TOKEN_PREFIX + account);
        if (oldRefreshToken != null) {
            redisTemplate.delete(JwtProvider.REFRESH_TOKEN_PREFIX + oldRefreshToken);
        }
    }

    //회원 가입 시의 계정 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateAccount(MemberDto memberDTO) {
        Optional<Member> byAccount = memberRepository.findByAccount(memberDTO.getAccount());
        if (byAccount.isPresent()) {
            throw new AccountException(ExceptionType.ACCOUNT_OVERLAP);
        }
    }

    //회원가입 시의 이메일 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateEmail(MemberDto memberDTO) {
        Member member = memberRepository.findByEmail(memberDTO.getEmail());

        if (member != null) {
            throw new AccountException(ExceptionType.EMAIL_OVERLAP);
        }
    }

    //회원가입 시의 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkDuplicateNicknameSignUp(MemberDto memberDTO) {
        profileRepository.findByNickname(memberDTO.getNickname())
                .ifPresent(profile -> {
                    throw new ProfileException(ExceptionType.NICKNAME_OVERLAP);
                });
    }

    //전체 조회(테스트 용도)
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}