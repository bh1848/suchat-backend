package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.CodeException;
import com.USWRandomChat.backend.global.exception.errortype.TokenException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeRequest;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeResponse;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeRequest;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeResponse;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private static final String REDIS_KEY_PREFIX = "verification-code:";
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRATION_TIME_VERIFICATION_CODE = 3;//제한 시간 3분
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> verificationRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    //랜덤 인증번호 생성
    private String generateRandomCode() {
        return new Random().ints(CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(i -> String.valueOf(CHARACTERS.charAt(i)))
                .collect(Collectors.joining());
    }

    //랜덤 인증번호 전송
    public SendRandomCodeResponse sendRandomCode(String accessToken, SendRandomCodeRequest request) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        Member member = memberRepository.findByAccountAndEmail(request.getAccount(), request.getEmail())
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        String randomCode = generateRandomCode();
        saveCodeToRedis(member.getAccount(), randomCode);
        sendEmail(member.getEmail() + "@suwon.ac.kr", "인증번호", "인증번호: " + randomCode);

        return new SendRandomCodeResponse(member.getAccount(), member.getEmail());
    }

    //랜덤 인증번호 검증
    public boolean verifyRandomCode(String accessToken, String verificationCode) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        String storedCode = Optional.ofNullable(verificationRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + account))
                .orElseThrow(() -> new CodeException(ExceptionType.CODE_ERROR));

        boolean isValid = verificationCode.equals(storedCode);
        if (isValid) {
            verificationRedisTemplate.delete(REDIS_KEY_PREFIX + account);
        }

        return isValid;
    }

    //비밀번호 변경
    public PasswordChangeResponse updatePassword(String accessToken, PasswordChangeRequest request) {
        //엑세스 토큰의 유효성 검사
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new TokenException(ExceptionType.INVALID_ACCESS_TOKEN);
        }

        //토큰이 유효한 경우, 계정 정보를 추출
        String account = jwtProvider.getAccount(accessToken);

        Member member = memberRepository.findByAccount(account)
                .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_EXISTS));

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);

        log.info("비밀번호 변경 완료: account={}", account);
        return new PasswordChangeResponse(member);
    }

    //Redis에 인증 번호 저장
    private void saveCodeToRedis(String account, String verificationCode) {
        verificationRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + account, verificationCode, EXPIRATION_TIME_VERIFICATION_CODE, TimeUnit.MINUTES);
    }

    //이메일 전송
    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패", e);
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }
}
