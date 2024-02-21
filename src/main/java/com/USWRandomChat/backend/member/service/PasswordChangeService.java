package com.USWRandomChat.backend.member.service;

import com.USWRandomChat.backend.member.memberDTO.PasswordChangeRequest;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeResponse;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeRequest;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeResponse;
import com.USWRandomChat.backend.member.exception.VerificationCodeException;
import com.USWRandomChat.backend.exception.ExceptionType;
import com.USWRandomChat.backend.exception.errortype.AccountException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private static final String REDIS_KEY_PREFIX = "verification-code:";
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 4;
    // 만료 시간(3분)
    private static final long EXPIRATION_TIME_VERIFICATION_CODE = 3;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> verificationRedisTemplate;
    private final PasswordEncoder passwordEncoder;

    //랜덤 인증번호 생성
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    // 랜덤 인증번호 전송
    public SendRandomCodeResponse sendRandomCode(String account, SendRandomCodeRequest sendRandomCodeRequest) throws VerificationCodeException {
        // account 검증
        memberRepository.findByAccount(account);

        String codeRequestAccount = sendRandomCodeRequest.getAccount();
        String email = sendRandomCodeRequest.getEmail();

        // account와 email이 유효한지 확인
        Optional<Member> optionalMember = memberRepository.findByAccountAndEmail(codeRequestAccount, email);

        optionalMember.ifPresent(member -> {
            // 랜덤 인증번호 생성
            String randomCode = generateRandomCode();

            // Redis에 인증번호 저장
            saveCodeToRedis(codeRequestAccount, randomCode);

            // 이메일 전송
            sendEmail(email + "@suwon.ac.kr", "인증번호", "인증번호: " + randomCode);
        });

        // 성공한 경우 응답 생성
        return new SendRandomCodeResponse(codeRequestAccount, email);
    }

    // 랜덤 인증번호 검증
    public boolean verifyRandomCode(String account, String verificationCode) throws VerificationCodeException {

        // Redis에서 저장된 인증번호 가져오기
        ValueOperations<String, String> valueOperations = verificationRedisTemplate.opsForValue();
        String redisKey = REDIS_KEY_PREFIX + account;
        String storedCode = valueOperations.get(redisKey);

        // 사용자가 입력한 인증번호와 저장된 인증번호 비교
        boolean isValid = verificationCode.equals(storedCode);

        // 검증이 완료되면 Redis에서 인증번호 삭제
        if (isValid) {
            verificationRedisTemplate.delete(redisKey);
        }

        return isValid;
    }

    // 비밀번호 변경
    @Transactional
    public PasswordChangeResponse updatePassword(String account, PasswordChangeRequest passwordChangeRequest) {

        // 새로운 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmNewPassword())) {
            log.error("비밀번호가 일치하지 않습니다.");
            throw new VerificationCodeException("비밀번호가 일치하지 않습니다.");
        }

        // Redis에서 저장된 인증번호 제거
        String redisKey = REDIS_KEY_PREFIX + account;
        verificationRedisTemplate.delete(redisKey);

        // 비밀번호 변경
        Member member = memberRepository.findByAccount(account);
        if (member == null) {
            throw new AccountException(ExceptionType.USER_NOT_EXISTS);
        }

        // 새로운 비밀번호를 암호화
        String encryptedPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());

        // 암호화된 비밀번호로 변경
        member.updatePassword(encryptedPassword);

        // 변경된 비밀번호를 데이터베이스에 저장
        memberRepository.save(member);

        log.info("비밀번호 변경 완료: account={}", account);

        // 변경된 비밀번호를 포함한 응답 반환
        return new PasswordChangeResponse(member);
    }

    // redis에 인증 번호 저장
    private void saveCodeToRedis(String account, String verificationCode) {
        ValueOperations<String, String> valueOperations = verificationRedisTemplate.opsForValue();
        String redisKey = REDIS_KEY_PREFIX + account;
        valueOperations.set(redisKey, verificationCode, EXPIRATION_TIME_VERIFICATION_CODE, TimeUnit.MINUTES);
    }

    // 이메일 전송
    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }
}
