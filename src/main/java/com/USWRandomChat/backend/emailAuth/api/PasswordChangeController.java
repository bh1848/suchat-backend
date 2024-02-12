package com.USWRandomChat.backend.emailAuth.api;

import com.USWRandomChat.backend.emailAuth.dto.PasswordChangeRequest;
import com.USWRandomChat.backend.emailAuth.dto.PasswordChangeResponse;
import com.USWRandomChat.backend.emailAuth.exception.VerificationCodeException;
import com.USWRandomChat.backend.emailAuth.service.PasswordChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService verificationCodeService;

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

    // 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String memberId, @RequestParam String verificationCode) {
        boolean isValid = verificationCodeService.verifyCode(memberId, verificationCode);
        if (isValid) {
            return new ResponseEntity<>("인증번호가 확인됐습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증번호가 맞지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 변경
    @PostMapping("/update-password")
    public ResponseEntity<Object> updatePassword(@RequestParam String memberId, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            PasswordChangeResponse response = verificationCodeService.updatePassword(memberId, passwordChangeRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (VerificationCodeException e) {
            return new ResponseEntity<>((e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}