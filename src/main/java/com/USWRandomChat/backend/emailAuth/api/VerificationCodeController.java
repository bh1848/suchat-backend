package com.USWRandomChat.backend.emailAuth.api;

import com.USWRandomChat.backend.emailAuth.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    // 비밀번호 재설정을 위한 인증번호 생성 및 이메일 전송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String memberId, @RequestParam String email) {
        try {
            verificationCodeService.sendRandomCode(memberId, email);
            return new ResponseEntity<>("인증번호가 전송됐습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("인증번호 전송이 실패했습니다." + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 인증번호 검증 API
    @PostMapping("/verify-code")
    public ResponseEntity<Boolean> verifyCode(@RequestParam String memberId, @RequestParam String verificationCode) {
        try {
            boolean isVerified = verificationCodeService.verifyCode(memberId, verificationCode);
            return new ResponseEntity<>(isVerified, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}




