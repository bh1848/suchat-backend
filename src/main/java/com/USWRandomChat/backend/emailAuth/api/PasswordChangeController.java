package com.USWRandomChat.backend.emailAuth.api;

import com.USWRandomChat.backend.emailAuth.service.PasswordChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/password-reset")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService passwordService;

    // 비밀번호 재설정을 위한 인증번호 생성 및 이메일 전송 API
    @PostMapping("/send-verification-code")
    public void sendVerificationCode(@RequestParam String memberId, @RequestParam String email) {
        passwordService.sendRandomCode(memberId, email);
    }
}
