package com.USWRandomChat.backend.emailAuth.service;


import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private static final String REDIS_KEY_PREFIX = "verification-code:";
    private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 4;
    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> verificationRedisTemplate;

    //랜덤 인증번호 생성
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
    
    //랜덤 인증번호 전송
    public void sendRandomCode(String memberId, String email) {
        try {
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
                        throw new RuntimeException("Invalid memberId or email");
                    });
        } catch (Exception e) {
            log.error("인증번호 전송 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("예외가 발생했습니다.", e);
        }
    }

    private void saveCodeToRedis(String memberId, String code) {
        ValueOperations<String, String> valueOperations = verificationRedisTemplate.opsForValue();
        String redisKey = REDIS_KEY_PREFIX + memberId;
        valueOperations.set(redisKey, code);
    }

    //간단한 텍스트 이메일 전송 메서드
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
}