package com.USWRandomChat.backend.member.api;

import com.USWRandomChat.backend.member.memberDTO.PasswordChangeRequest;
import com.USWRandomChat.backend.member.memberDTO.PasswordChangeResponse;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeRequest;
import com.USWRandomChat.backend.member.memberDTO.SendRandomCodeResponse;
import com.USWRandomChat.backend.member.exception.VerificationCodeException;
import com.USWRandomChat.backend.member.service.PasswordChangeService;
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

    private final PasswordChangeService passwordChangeService;

    // 비밀번호 재설정을 위한 인증번호 생성 및 이메일 전송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String account, @RequestBody SendRandomCodeRequest sendRandomCodeRequest) {
        try {
            SendRandomCodeResponse response = passwordChangeService.sendRandomCode(account, sendRandomCodeRequest);
            return new ResponseEntity<>("인증번호가 전송됐습니다. account: " + response.getAccount() + ", email: " + response.getEmail(), HttpStatus.OK);
        } catch (VerificationCodeException e) {
            return new ResponseEntity<>("올바르지 않은 아이디 혹은 이메일입니다. " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 인증번호 검증
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String account, @RequestParam String verificationCode) {
        boolean isValid = passwordChangeService.verifyRandomCode(account, verificationCode);
        if (isValid) {
            return new ResponseEntity<>("인증번호가 확인됐습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증번호가 맞지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 변경
    @PostMapping("/update-password")
    public ResponseEntity<Object> updatePassword(@RequestParam String account, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            PasswordChangeResponse response = passwordChangeService.updatePassword(account, passwordChangeRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (VerificationCodeException e) {
            return new ResponseEntity<>((e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}