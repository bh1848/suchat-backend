package com.USWRandomChat.backend.emailAuth.service;

import com.USWRandomChat.backend.emailAuth.exception.VerificationCodeException;
import com.USWRandomChat.backend.member.domain.Member;
import com.USWRandomChat.backend.member.repository.MemberRepository;
import com.USWRandomChat.backend.security.jwt.JwtProvider;
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
public class VerificationCodeService {

    private static final String REDIS_KEY_PREFIX = "verification-code:";
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 4;
    // 만료 시간(3분)
    private static final long EXPIRATION_TIME_VERIFICATION_CODE = 3;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> verificationRedisTemplate;
    private final JwtProvider jwtProvider;
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
    public boolean sendRandomCode(String memberId, String email) throws VerificationCodeException {
        // memberId와 email이 유효한지 확인
        memberRepository.findByMemberIdAndEmail(memberId, email)
                .ifPresentOrElse(member -> {
                    // 랜덤 인증번호 생성
                    String randomCode = generateRandomCode();

                    // Redis에 인증번호 저장
                    saveCodeToRedis(memberId, randomCode);

                    // 이메일 전송
                    sendEmail(email + "@suwon.ac.kr", "인증번호", "인증번호: " + randomCode);
                }, () -> {
                    // memberId 또는 email이 유효하지 않을 경우 예외 처리
                    throw new VerificationCodeException("아이디 또는 이메일을 다시 확인해주세요");
                });

        // 성공한 경우 true 반환
        return true;
    }

    // 랜덤 인증번호 검증
    public boolean verifyCode(String memberId, String verificationCode) {
        // Redis에서 저장된 인증번호 가져오기
        ValueOperations<String, String> valueOperations = verificationRedisTemplate.opsForValue();
        String redisKey = REDIS_KEY_PREFIX + memberId;
        String storedCode = valueOperations.get(redisKey);

        // 사용자가 입력한 인증번호와 저장된 인증번호 비교
        return verificationCode.equals(storedCode);
    }

    @Transactional
    // 비밀번호 변경
    public boolean updatePassword(String token, String newPassword, String confirmNewPassword) {

        // 새로운 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmNewPassword)) {
            log.error("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            throw new VerificationCodeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        String memberId = jwtProvider.getMemberId(token);

        // Redis에서 저장된 인증번호 제거
        String redisKey = REDIS_KEY_PREFIX + memberId;
        verificationRedisTemplate.delete(redisKey);

        // 비밀번호 변경
        Optional<Member> optionalMember = memberRepository.findByMemberId(memberId);
        return optionalMember.map(member -> {

            // 새로운 비밀번호를 암호화
            String encryptedPassword = passwordEncoder.encode(newPassword);

            // 암호화된 비밀번호로 변경
            member.updatePassword(encryptedPassword);

            // 변경된 비밀번호를 데이터베이스에 저장
            memberRepository.save(member);

            log.info("비밀번호 변경 완료: memberId={}", memberId);

            return true;
        }).orElse(false);
    }

    // redis에 인증 번호 저장
    private void saveCodeToRedis(String memberId, String verificationCode) {
        ValueOperations<String, String> valueOperations = verificationRedisTemplate.opsForValue();
        String redisKey = REDIS_KEY_PREFIX + memberId;
        valueOperations.set(redisKey, verificationCode, EXPIRATION_TIME_VERIFICATION_CODE, TimeUnit.MINUTES);
    }

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
