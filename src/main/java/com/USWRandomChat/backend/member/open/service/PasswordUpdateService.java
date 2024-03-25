package com.USWRandomChat.backend.member.open.service;

import com.USWRandomChat.backend.global.exception.ExceptionType;
import com.USWRandomChat.backend.global.exception.errortype.AccountException;
import com.USWRandomChat.backend.global.exception.errortype.CodeException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PasswordUpdateService {
    private static final String REDIS_KEY_PREFIX_VERIFICATION = "verification-code:";
    private static final String REDIS_KEY_PREFIX_UUID = "password-reset:";
    private static final String REDIS_KEY_PREFIX_STATUS = "status:";
    private static final String CHARACTERS = "0123456789"; //인증번호 생성에 사용될 문자열
    private static final int CODE_LENGTH = 6; //인증번호 길이
    private static final long EXPIRATION_TIME_VERIFICATION = 3; //인증번호 유효 시간 (분)
    private static final long EXPIRATION_TIME_UUID = 15; //UUID 유효 시간 (분)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> verificationRedisTemplate;
    private final JavaMailSender mailSender;

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
        sendEmail(email + "@suwon.ac.kr", "인증번호는 " + verificationCode + "입니다.");

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
    private void sendEmail(String to, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("인증번호");
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