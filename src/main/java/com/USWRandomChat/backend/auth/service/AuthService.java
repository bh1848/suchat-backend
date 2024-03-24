package com.USWRandomChat.backend.auth.service;

import com.USWRandomChat.backend.emailAuth.domain.EmailToken;
import com.USWRandomChat.backend.emailAuth.repository.EmailTokenRepository;
import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.CodeException;
import com.USWRandomChat.backend.global.security.domain.Authority;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import com.USWRandomChat.backend.global.security.jwt.dto.TokenDto;
import com.USWRandomChat.backend.global.security.jwt.repository.JwtRepository;
import com.USWRandomChat.backend.global.security.jwt.service.JwtService;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.domain.MemberTemp;
import com.USWRandomChat.backend.member.memberDTO.SignInRequest;
import com.USWRandomChat.backend.member.memberDTO.SignUpRequest;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.member.repository.MemberTempRepository;
import com.USWRandomChat.backend.profile.domain.Profile;
import com.USWRandomChat.backend.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    //Redis 키 접두사 상수 정의
    private static final String REDIS_KEY_PREFIX_VERIFICATION = "verification-code:";
    private static final String REDIS_KEY_PREFIX_UUID = "password-reset:";
    private static final String REDIS_KEY_PREFIX_STATUS = "status:";
    private static final String CHARACTERS = "0123456789"; //인증번호 생성에 사용될 문자열
    private static final int CODE_LENGTH = 6; //인증번호 길이
    private static final long EXPIRATION_TIME_VERIFICATION = 3; //인증번호 유효 시간 (분)
    private static final long EXPIRATION_TIME_UUID = 15; //UUID 유효 시간 (분)
    private final MemberRepository memberRepository;
    private final MemberTempRepository memberTempRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final RedisTemplate<String, String> verificationRedisTemplate;
    private final JavaMailSender mailSender;

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

    //인증번호 생성 및 전송
    public String sendVerificationCode(String account, String email) {
        // 사용자 계정과 이메일이 일치하는지 확인
        memberRepository.findByAccountAndEmail(account, email)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        // 인증번호 및 UUID 생성
        String verificationCode = generateRandomCode();
        String uuid = UUID.randomUUID().toString();

        // Redis에 인증번호와 사용자 계정 정보 저장
        saveVerificationCode(uuid, verificationCode, account);

        // 이메일 전송
        sendEmail(email + "@suwon.ac.kr", "인증번호", "인증번호는 " + verificationCode + "입니다.");

        // 클라이언트에게 UUID 반환
        return uuid;
    }

    //인증번호 확인
    public boolean verifyCode(String uuid, String verificationCode) {
        //Redis에서 인증번호 조회
        String key = buildRedisKey(REDIS_KEY_PREFIX_VERIFICATION, uuid);
        String storedCode = Optional.ofNullable(verificationRedisTemplate.opsForValue().get(key))
                .orElseThrow(() -> new CodeException(ExceptionType.CODE_ERROR));

        //인증번호 일치 확인
        if (verificationCode.equals(storedCode)) {
            verificationRedisTemplate.delete(key); //일치할 경우 Redis에서 인증번호 삭제
            saveVerificationStatus(uuid); //인증 상태를 "verified"로 저장
            return true;
        } else {
            throw new CodeException(ExceptionType.CODE_ERROR);
        }
    }

    //비밀번호 변경
    public void updatePassword(String uuid, String newPassword, String confirmNewPassword) {
        //새 비밀번호와 확인 비밀번호가 일치하는지 검사
        if (!newPassword.equals(confirmNewPassword)) {
            throw new AccountException(ExceptionType.PASSWORD_ERROR);
        }

        //인증 상태 확인
        String verificationStatusKey = buildRedisKey(REDIS_KEY_PREFIX_VERIFICATION + REDIS_KEY_PREFIX_STATUS, uuid);
        verifyStatus(verificationStatusKey);

        //UUID를 통해 사용자 계정 정보 조회
        String account = getAccountByUuid(uuid);
        //비밀번호 업데이트
        updateMemberPassword(account, newPassword);

        //비밀번호 변경 후 인증번호와 UUID 삭제
        verificationRedisTemplate.delete(verificationStatusKey);
        verificationRedisTemplate.delete(buildRedisKey(REDIS_KEY_PREFIX_UUID, uuid));
    }

    //인증번호 생성
    private String generateRandomCode() {
        return new Random().ints(CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    //이메일 전송
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    //Redis에 인증번호 및 사용자 계정 정보 저장
    private void saveVerificationCode(String uuid, String code, String account) {
        verificationRedisTemplate.opsForValue().set(buildRedisKey(REDIS_KEY_PREFIX_VERIFICATION, uuid), code, EXPIRATION_TIME_VERIFICATION, TimeUnit.MINUTES);
        verificationRedisTemplate.opsForValue().set(buildRedisKey(REDIS_KEY_PREFIX_UUID, uuid), account, EXPIRATION_TIME_UUID, TimeUnit.MINUTES);
    }

    //인증 상태 저장
    private void saveVerificationStatus(String uuid) {
        verificationRedisTemplate.opsForValue().set(buildRedisKey(REDIS_KEY_PREFIX_VERIFICATION + REDIS_KEY_PREFIX_STATUS, uuid), "verified", EXPIRATION_TIME_UUID, TimeUnit.MINUTES);
    }

    //인증 상태 확인
    private void verifyStatus(String verificationStatusKey) {
        String verificationStatus = verificationRedisTemplate.opsForValue().get(verificationStatusKey);
        if (!"verified".equals(verificationStatus)) {
            throw new AccountException(ExceptionType.VERIFICATION_NOT_COMPLETED);
        }
    }

    //UUID를 통해 사용자 계정 정보 조회
    private String getAccountByUuid(String uuid) {
        return Optional.ofNullable(verificationRedisTemplate.opsForValue().get(buildRedisKey(REDIS_KEY_PREFIX_UUID, uuid)))
                .orElseThrow(() -> new AccountException(ExceptionType.UUID_NOT_FOUND));
    }

    //비밀번호 업데이트
    private void updateMemberPassword(String account, String newPassword) {
        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    //Redis 키 생성
    private String buildRedisKey(String prefix, String uuid) {
        return prefix + uuid;
    }
}
