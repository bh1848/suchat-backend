package com.USWRandomChat.backend.member.verification;

import com.USWRandomChat.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;

    // 인증번호 전송
    public void sendVerificationCode(String username, String email) {
        memberRepository.findByAccountAndEmail(username, email).ifPresent(member -> {
            String verificationCode = UUID.randomUUID().toString().substring(0, 6); // 간단한 인증번호 생성
            redisTemplate.opsForValue().set(username, verificationCode, 3, TimeUnit.MINUTES); // Redis에 저장, 3분간 유효

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("인증번호");
            message.setText("인증번호는 " + verificationCode + "입니다.");
            mailSender.send(message); // 이메일 전송
        });
    }

    // 인증번호 확인
    public boolean verifyCode(String username, String code) {
        String storedCode = redisTemplate.opsForValue().get(username);
        return code.equals(storedCode);
    }
}
